/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ruleengine.init.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Lists.partition;
import static com.google.common.collect.Sets.newHashSet;
import static de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy.getPartitionSize;
import static de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper.convertLevel;
import static de.hybris.platform.ruleengine.util.RuleEngineUtils.getRulePath;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isNotTrue;
import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.assertj.core.util.Lists.emptyList;
import static org.fest.util.Collections.isEmpty;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.ResultItem;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;
import de.hybris.platform.ruleengine.concurrency.TaskContext;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.init.RulePublishingFuture;
import de.hybris.platform.ruleengine.init.RulePublishingSpliterator;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;

import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RulePublishingSpliterator}
 */
public class DefaultRulePublishingSpliterator implements RulePublishingSpliterator
{
	private static final String BASE_WORKER_NAME = "RulePublisher";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRulePublishingSpliterator.class);

	private KieServices kieServices;
	private TaskContext taskContext;
	private EngineRuleDao engineRuleDao;
	private RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap;
	private SuspendResumeTaskManager suspendResumeTaskManager;

	@Override
	public RulePublishingFuture publishRulesAsync(final KieModuleModel kieModuleModel, final ReleaseId containerReleaseId,
			final List<String> ruleUuids, final KIEModuleCacheBuilder cache)
	{
		final List<KieBuilder> kieBuilders = new CopyOnWriteArrayList<>();

		final List<List<String>> partitionOfRulesUuids = splitListByThreads(ruleUuids, getTaskContext().getNumberOfThreads());

		final Set<Thread> builderWorkers = newHashSet();
		final List<RuleEngineActionResult> ruleEngineActionResults = newCopyOnWriteArrayList();
		for (final List<String> ruleUuidsChunk : partitionOfRulesUuids)
		{
			builderWorkers.add(createNewWorker(kieBuilders, kieModuleModel, containerReleaseId, ruleUuidsChunk,
					ruleEngineActionResults, cache));
		}
		startWorkers(builderWorkers);
		return new RulePublishingFuture(builderWorkers, ruleEngineActionResults, kieBuilders, getTaskContext().getThreadTimeout());
	}

	@PostConstruct
	protected void setUp()
	{
		kieServices = getRuleEngineBootstrap().getEngineServices();
	}

	public <T> List<List<T>> splitListByThreads(final List<T> list, final int numberOfThreads)
	{
		checkArgument(numberOfThreads > 0,
				"Valid maximum number of threads (>0) must be provided");

		final int partitionSize = getPartitionSize(list.size(), numberOfThreads);
		if (partitionSize == 0)
		{
			return emptyList();
		}
		return partition(list, partitionSize);
	}

	protected Thread createNewWorker(final List<KieBuilder> kieBuilders, final KieModuleModel kieModuleModel,
			final ReleaseId releaseId, final List<String> ruleUuids,
			final List<RuleEngineActionResult> ruleEngineActionResults, final KIEModuleCacheBuilder cache)
	{
		final Tenant currentTenant = getTaskContext().getCurrentTenant();
		final ThreadFactory tenantAwareThreadFactory = getTaskContext().getThreadFactory();

		checkArgument(nonNull(currentTenant), "Current Tenant must be provided as part of TaskContext");
		checkArgument(nonNull(tenantAwareThreadFactory), "ThreadFactory must be provided as part of TaskContext");

		return currentTenant.createAndRegisterBackgroundThread(
				() -> ruleEngineActionResults
						.add(addRulesBuilder(kieBuilders, kieModuleModel, releaseId, ruleUuids, cache)),
				tenantAwareThreadFactory);
	}

	protected void startWorkers(final Set<Thread> workers)
	{
		if (isNotEmpty(workers))
		{
			for (final Thread worker : workers)
			{
				worker.setName(BASE_WORKER_NAME + "-" + worker.getName());
				getSuspendResumeTaskManager().registerAsNonSuspendableTask(worker,
						String.format("Rule engine rules publishing task %s is currently in progress", worker.getName()));
				worker.start();
			}
		}
	}

	protected RuleEngineActionResult addRulesBuilder(final List<KieBuilder> kieBuilders, final KieModuleModel kieModuleModel,
			final ReleaseId releaseId, final List<String> ruleUuids, final KIEModuleCacheBuilder cache)
	{
		final Collection<DroolsRuleModel> droolRules = getEngineRuleDao().getRulesByUuids(ruleUuids);

		final KieFileSystem partialKieFileSystem = getKieServices().newKieFileSystem();
		writeKModuleXML(kieModuleModel, partialKieFileSystem);
		writePomXML(releaseId, partialKieFileSystem);

		final KieBuilder partialKieBuilder = getKieServices().newKieBuilder(partialKieFileSystem);
		for (final DroolsRuleModel rule : droolRules)
		{
			if (nonNull(rule.getRuleContent()) && isTrue(rule.getActive()) && isTrue(rule.getCurrentVersion()))
			{
				// add cache entities to cache
				cache.processRule(rule);
				try
				{
					partialKieFileSystem.write(getRulePath(rule), rule.getRuleContent());
				}
				catch (final Exception e)
				{
					return createNewResult(createKieBuilderErrorResult(rule, e));
				}
			}
			if (isNull(rule.getRuleContent()))
			{
				LOGGER.warn("ignoring rule {}. No ruleContent set!", rule.getCode());
			}
			else if (isNotTrue(rule.getActive()) || isNotTrue(rule.getCurrentVersion()))
			{
				LOGGER.debug("ignoring rule {}. Rule is not active.", rule.getCode());
			}
		}
		partialKieBuilder.buildAll();
		kieBuilders.add(partialKieBuilder);
		return createNewResult(partialKieBuilder.getResults());
	}

	protected Results createKieBuilderErrorResult(final DroolsRuleModel rule, final Exception e)
	{
		final ResultsImpl results = new ResultsImpl();
		results.addMessage(Level.ERROR, rule.getCode(),
				"exception encountered during writing of kie file system:" + e.getMessage());
		return results;
	}

	protected void writeKModuleXML(final KieModuleModel module, final KieFileSystem kfs)
	{
		kfs.writeKModuleXML(module.toXML());
	}

	public void writePomXML(final ReleaseId releaseId, final KieFileSystem kfs)
	{
		kfs.generateAndWritePomXML(releaseId);
	}

	protected RuleEngineActionResult createNewResult(final Results results)
	{
		final RuleEngineActionResult ruleEngineActionResult = new RuleEngineActionResult();
		for (final Message message : results.getMessages())
		{
			LOGGER.error("{} {} {}", message.getLevel(), message.getText(), message.getPath());
			final ResultItem item = addNewResultItemOf(ruleEngineActionResult, convertLevel(message.getLevel()), message.getText());
			item.setLine(message.getLine());
			item.setPath(message.getPath());
		}
		if (results.hasMessages(Message.Level.ERROR))
		{
			ruleEngineActionResult.setActionFailed(true);
		}
		return ruleEngineActionResult;
	}

	protected ResultItem addNewResultItemOf(final RuleEngineActionResult result, final MessageLevel messageLevel,
			final String message)
	{
		final ResultItem resultItem = new ResultItem();
		resultItem.setLevel(messageLevel);
		resultItem.setMessage(message);
		if (isEmpty(result.getResults()))
		{
			result.setResults(newCopyOnWriteArrayList());
		}
		result.getResults().add(resultItem);
		return resultItem;
	}

	protected KieServices getKieServices()
	{
		return kieServices;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	protected TaskContext getTaskContext()
	{
		return taskContext;
	}

	@Required
	public void setTaskContext(final TaskContext taskContext)
	{
		this.taskContext = taskContext;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> getRuleEngineBootstrap()
	{
		return ruleEngineBootstrap;
	}

	@Required
	public void setRuleEngineBootstrap(
			final RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap)
	{
		this.ruleEngineBootstrap = ruleEngineBootstrap;
	}

	protected SuspendResumeTaskManager getSuspendResumeTaskManager()
	{
		return suspendResumeTaskManager;
	}

	@Required
	public void setSuspendResumeTaskManager(final SuspendResumeTaskManager suspendResumeTaskManager)
	{
		this.suspendResumeTaskManager = suspendResumeTaskManager;
	}
}
