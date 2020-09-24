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
package de.hybris.platform.ruleengineservices.maintenance.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.partition;
import static com.google.common.collect.Sets.newHashSet;
import static de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy.getPartitionSize;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.assertj.core.util.Lists.emptyList;
import static org.assertj.core.util.Lists.newArrayList;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerService;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerFuture;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerSpliterator;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Default implementation of the {@link RuleCompilerSpliterator} for {@link SourceRuleModel} rules
 */
public class DefaultRuleCompilerSpliterator<T extends SourceRuleModel> implements RuleCompilerSpliterator<T>
{

	private static final String BASE_WORKER_NAME = "RuleCompiler";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleCompilerSpliterator.class);
	private static final int DEFAULT_NUMBER_OF_THREADS = 1;

	private RuleCompilationContext ruleCompilationContext;

	private DefaultRuleCompilerSpliterator(final RuleCompilationContext ruleCompilationContext)
	{
		this.ruleCompilationContext = ruleCompilationContext;
	}

	static <T extends SourceRuleModel> RuleCompilerSpliterator<T> withCompilationContext(
			@Nonnull final RuleCompilationContext ruleCompilationContext)
	{
		checkArgument(nonNull(ruleCompilationContext), "Valid RuleCompilationContext must be provided");
		return new DefaultRuleCompilerSpliterator<>(ruleCompilationContext);
	}

	@Override
	public RuleCompilerResult compileSingleRule(final T rule, final String moduleName)
	{
		final RuleCompilerService ruleCompilerService = ruleCompilationContext.getRuleCompilerService();
		checkArgument(nonNull(ruleCompilerService), "RuleCompilerService must be provided as part of RuleCompilationContext");
		ruleCompilationContext.resetRuleEngineRuleVersion(moduleName);
		return ruleCompilerService.compile(ruleCompilationContext, rule, moduleName);
	}

	@Override
	public RuleCompilerFuture compileRulesAsync(final List<T> rules, final String moduleName)
	{
		final Set<Thread> workers = newHashSet();
		final RuleCompilerFuture ruleCompilerFuture = createNewRuleCompilerFuture(workers);
		ruleCompilationContext.resetRuleEngineRuleVersion(moduleName);
		final List<List<T>> splitRulesChunks = splitRules(rules);
		for (List<T> rulesChunk : splitRulesChunks)
		{
			final List<RuleCompilerResult> ruleCompilerResults = newArrayList();
			ruleCompilerFuture.addRuleCompilerResults(ruleCompilerResults);
			workers.add(createNewWorker(rulesChunk, moduleName, ruleCompilerResults));
		}
		startWorkers(workers);
		return ruleCompilerFuture;
	}

	protected List<List<T>> splitRules(final List<T> rules)
	{
		int numberOfThreads = ruleCompilationContext.getNumberOfThreads();
		if (numberOfThreads <= 0)
		{
			LOGGER.warn(
					"Valid maximum number of threads (>0) must be provided as part of RuleCompilationContext. It was [{}]. The default value [{}] will be used",
					numberOfThreads, DEFAULT_NUMBER_OF_THREADS);
			numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
		}

		final int partitionSize = getPartitionSize(rules.size(), numberOfThreads);
		if (partitionSize == 0)
		{
			return emptyList();
		}
		return partition(rules, partitionSize);
	}

	protected Thread createNewWorker(final List<T> rules, final String moduleName,
			final List<RuleCompilerResult> ruleCompilerResults)
	{
		final Tenant currentTenant = ruleCompilationContext.getCurrentTenant();
		final ThreadFactory tenantAwareThreadFactory = ruleCompilationContext.getThreadFactory();
		final RuleCompilerService ruleCompilerService = ruleCompilationContext.getRuleCompilerService();

		checkArgument(nonNull(currentTenant), "Current Tenant must be provided as part of RuleCompilationContext");
		checkArgument(nonNull(tenantAwareThreadFactory), "ThreadFactory must be provided as part of RuleCompilationContext");
		checkArgument(nonNull(ruleCompilerService), "RuleCompilerService must be provided as part of RuleCompilationContext");

		return currentTenant
				.createAndRegisterBackgroundThread(JobProvider.getJob(ruleCompilationContext, rules, moduleName, ruleCompilerResults),
						tenantAwareThreadFactory);
	}

	protected void startWorkers(final Set<Thread> workers)
	{
		if (isNotEmpty(workers))
		{
			for (Thread worker : workers)
			{
				final String threadName = BASE_WORKER_NAME + "-" + worker.getName();
				worker.setName(threadName);
				ruleCompilationContext.getSuspendResumeTaskManager()
						.registerAsNonSuspendableTask(worker,
								String.format("Rule engine rules compilation task %s is currently in progress", worker.getName()));
				worker.start();
			}
		}
	}

	protected RuleCompilerFuture createNewRuleCompilerFuture(final Set<Thread> workers)
	{
		final Long preDestroyTimeout = ruleCompilationContext.getThreadTimeout();
		checkArgument(nonNull(preDestroyTimeout) && preDestroyTimeout.longValue() > 0,
				"Valid pre-destroy timeout (>0) must be provided as part of RuleCompilationContext: [" + preDestroyTimeout + "]");
		return new RuleCompilerFuture(workers, preDestroyTimeout);
	}

	public static class JobProvider
	{

		private JobProvider()
		{
		}

		protected static <T extends SourceRuleModel> Runnable getJob(RuleCompilationContext ruleCompilationContext,
				final List<T> rules, final String moduleName,
				final List<RuleCompilerResult> ruleCompilerResults)
		{
			final RuleCompilerService ruleCompilerService = ruleCompilationContext.getRuleCompilerService();
			return () ->
			{
				final List<RuleCompilerResult> compilerResults = rules.stream()
						.map(r -> ruleCompilerService.compile(ruleCompilationContext, r, moduleName))
						.collect(toList());
				ruleCompilerResults.addAll(compilerResults);
			};
		}
	}

}
