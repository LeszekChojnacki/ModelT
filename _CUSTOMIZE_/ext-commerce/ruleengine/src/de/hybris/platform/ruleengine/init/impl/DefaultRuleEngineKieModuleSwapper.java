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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.ruleengine.ExecutionContext;
import de.hybris.platform.ruleengine.InitializeMode;
import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.ResultItem;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleEngineCacheService;
import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;
import de.hybris.platform.ruleengine.concurrency.TaskResult;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.drools.KieModuleService;
import de.hybris.platform.ruleengine.exception.DroolsInitializationException;
import de.hybris.platform.ruleengine.impl.KieContainerListener;
import de.hybris.platform.ruleengine.init.ConcurrentMapFactory;
import de.hybris.platform.ruleengine.init.ContentMatchRulesFilter;
import de.hybris.platform.ruleengine.init.IncrementalRuleEngineUpdateStrategy;
import de.hybris.platform.ruleengine.init.RuleDeploymentTaskResult;
import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper;
import de.hybris.platform.ruleengine.init.RulePublishingFuture;
import de.hybris.platform.ruleengine.init.RulePublishingSpliterator;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.RuleEngineUtils;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.builder.IncrementalResults;
import org.kie.internal.builder.InternalKieBuilder;
import org.kie.internal.builder.KieBuilderSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static de.hybris.platform.ruleengine.InitializeMode.NEW;
import static de.hybris.platform.ruleengine.InitializeMode.RESTORE;
import static de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper.convertLevel;
import static de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper.getEqualityBehaviorOption;
import static de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper.getEventProcessingOption;
import static de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper.getSessionType;
import static de.hybris.platform.ruleengine.util.RuleEngineUtils.getNormalizedRulePath;
import static de.hybris.platform.ruleengine.util.RuleEngineUtils.getRulePath;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isNotTrue;
import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.fest.util.Collections.isEmpty;



/**
 * Default (drools-based) implementation of the {@link RuleEngineKieModuleSwapper} interface
 */
public class DefaultRuleEngineKieModuleSwapper implements RuleEngineKieModuleSwapper
{

	public static final String WORKER_PRE_DESTROY_TIMEOUT = "ruleengine.kiemodule.swapping.predestroytimeout";
	private static final String BASE_WORKER_NAME = "RuleEngine-module-swapping";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineKieModuleSwapper.class);

	private long workerPreDestroyTimeout;
	private ConfigurationService configurationService;
	private KieServices kieServices;
	private Tenant currentTenant;
	private ThreadFactory tenantAwareThreadFactory;
	private ModelService modelService;
	private ConcurrentMapFactory concurrentMapFactory;
	private RulesModuleDao rulesModuleDao;
	private RuleEngineCacheService ruleEngineCacheService;
	private RulePublishingSpliterator rulePublishingSpliterator;
	private ContentMatchRulesFilter contentMatchRulesFilter;
	private IncrementalRuleEngineUpdateStrategy incrementalRuleEngineUpdateStrategy;
	private RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap;
	private SuspendResumeTaskManager suspendResumeTaskManager;
	private KieModuleService kieModuleService;
	private EngineRuleDao engineRuleDao;

	private Map<String, Set<Thread>> asyncWorkers;

	@Override
	public List<Object> switchKieModule(final DroolsKIEModuleModel module,
			final KieContainerListener listener, final LinkedList<Supplier<Object>> postTaskList,        // NOSONAR
			final boolean enableIncrementalUpdate, final RuleEngineActionResult result)
	{
		LOGGER.debug("Starting swapping of Kie Module [{}] in {}incremental mode", module.getName(),
				enableIncrementalUpdate ? "non-" : "");
		final List<Object> resultsAccumulator = newArrayList();
		try
		{
			LOGGER.debug("Invoking initialization of a new module for [{}]", module.getName());
			initializeNewModule(module, listener, enableIncrementalUpdate, result);
		}
		finally
		{
			postTaskList.forEach(pt -> resultsAccumulator.add(pt.get()));
		}
		return resultsAccumulator;
	}

	@Override
	public void switchKieModuleAsync(final String moduleName,
			final KieContainerListener listener, final List<Object> resultsAccumulator, final Supplier<Object> resetFlagSupplier,
			final List<Supplier<Object>> postTaskList, final boolean enableIncrementalUpdate,
			final RuleEngineActionResult result)
	{
		waitForSwappingToFinish(moduleName);
		LOGGER.info("Starting swapping for rule module [{}]", moduleName);
		final Thread asyncWorker = getCurrentTenant().createAndRegisterBackgroundThread(switchKieModuleRunnableTask(moduleName,
				listener, resultsAccumulator, resetFlagSupplier, postTaskList, enableIncrementalUpdate, result),
				getTenantAwareThreadFactory());
		asyncWorker.setName(getNextWorkerName());
		if(getSuspendResumeTaskManager().isSystemRunning())
		{
			asyncWorker.start();
			registerWorker(moduleName, asyncWorker);
		}
		else
		{
			resetFlagSupplier.get();
			LOGGER.info("Cannot proceed with module swapping since the system is not in running state");
		}
	}

	@Override
	public void waitForSwappingToFinish()
	{
		asyncWorkers.entrySet().stream().flatMap(e -> e.getValue().stream()).forEach(this::waitWhileWorkerIsRunning);
	}

	protected void waitForSwappingToFinish(final String moduleName)
	{
		asyncWorkers.entrySet().stream().filter(e -> e.getKey().equals(moduleName)).flatMap(e -> e.getValue().stream())
				.forEach(this::waitWhileWorkerIsRunning);
	}

	protected String getNextWorkerName()
	{
		long nextActiveOrder = 0;
		if (isNotEmpty(asyncWorkers))
		{
			nextActiveOrder = asyncWorkers.entrySet().stream().flatMap(e -> e.getValue().stream())
					.filter(w -> nonNull(w) && w.isAlive()).count();
		}
		return BASE_WORKER_NAME + "-" + nextActiveOrder;
	}

	protected void waitWhileWorkerIsRunning(final Thread worker)
	{
		if (nonNull(worker) && worker.isAlive())
		{
			try
			{
				LOGGER.debug("Waiting for a currently running async worker to finish the job...");
				worker.join(getWorkerPreDestroyTimeout());
			}
			catch (final InterruptedException e)
			{
				Thread.currentThread().interrupt();
				LOGGER.error("Interrupted exception is caught during async Kie container swap: {}", e);
			}
		}
	}

	/**
	 * This method to be called by containers (like spring container) as destroy method
	 */
	public void beforeDestroy()
	{
		waitForSwappingToFinish();
	}

	@Override
	public void writeKModuleXML(final KieModuleModel module, final KieFileSystem kfs)
	{
		LOGGER.debug("Writing Kie Module [{}] to XML", module);
		kfs.writeKModuleXML(module.toXML());
	}

	@Override
	public void writePomXML(final DroolsKIEModuleModel module, final KieFileSystem kfs)
	{
		final ReleaseId releaseId = getReleaseId(module);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Writing POM for releaseId: {}", releaseId.toExternalForm());
		}
		kfs.generateAndWritePomXML(releaseId);
	}

	@Override
	public ReleaseId getReleaseId(final DroolsKIEModuleModel module)
	{
		final String moduleVersion = RuleEngineUtils.getDeployedRulesModuleVersion(module);
		return getKieServices().newReleaseId(module.getMvnGroupId(), module.getMvnArtifactId(), moduleVersion);
	}

	@Override
	public Pair<KieModule, KIEModuleCacheBuilder> createKieModule(final DroolsKIEModuleModel module,
			final RuleEngineActionResult result)
	{
		validateParameterNotNull(module, "module must not be null");
		LOGGER.debug("Starting creation of a Kie Module for module [{}]", module.getName());
		final KIEModuleCacheBuilder cache = getRuleEngineCacheService().createKIEModuleCacheBuilder(module);
		final Collection<DroolsKIEBaseModel> kieBases = module.getKieBases();
		LOGGER.debug("Module [{}] contains [{}] kie bases", module.getName(), CollectionUtils.size(kieBases));
		validateParameterNotNull(kieBases, "kieBases in the module must not be null");

		LOGGER.debug("Creating new Kie Module Model");
		final KieModuleModel kieModuleModel = getKieServices().newKieModuleModel();
		LOGGER.debug("Successfully created new Kie Module [{}]", kieModuleModel);
		final KieFileSystem kfs = getKieServices().newKieFileSystem();
		kieBases.forEach(base -> addKieBase(kieModuleModel, kfs, base, cache));

		// write the kmodule.xml
		writeKModuleXML(kieModuleModel, kfs);
		// create and write the pom.xml
		writePomXML(module, kfs);

		final KieBuilder kieBuilder = getKieServices().newKieBuilder(kfs);
		kieBuilder.buildAll();

		if (kieBuilder.getResults().hasMessages(Message.Level.ERROR, Message.Level.WARNING, Message.Level.INFO))
		{
			for (final Message message : kieBuilder.getResults().getMessages())
			{
				LOGGER.error("{} {} {}", message.getLevel(), message.getText(), message.getPath());
				final ResultItem item = addNewResultItemOf(result, convertLevel(message.getLevel()), message.getText());
				item.setLine(message.getLine());
				item.setPath(message.getPath());
				if (Message.Level.ERROR.equals(message.getLevel()))
				{
					result.setActionFailed(true);
				}
			}
			if (kieBuilder.getResults().hasMessages(Message.Level.ERROR))
			{
				throw new DroolsInitializationException(result.getResults(), "Drools rule engine initialization failed");
			}
		}
		return Pair.of(kieBuilder.getKieModule(), cache);
	}

	protected Pair<KieModule, KIEModuleCacheBuilder> createKieModule(final DroolsKIEModuleModel module,
			final RuleEngineActionResult result, final boolean enableIncrementalUpdate)
	{
		LOGGER.debug("Starting creation of a Kie Module for module [{}]", module.getName());
		validateParameterNotNull(module, "module must not be null");
		final Collection<DroolsKIEBaseModel> kieBases = module.getKieBases();
		validateParameterNotNull(kieBases, "kieBases in the module must not be null");
		LOGGER.debug("Module [{}] contains [{}] kie bases", module.getName(), CollectionUtils.size(kieBases));

		LOGGER.debug("Creating new Kie Module Model");
		final KieModuleModel kieModuleModel = getKieServices().newKieModuleModel();
		LOGGER.debug("Successfully created new Kie Module [{}]", kieModuleModel);
		kieBases.forEach(base -> addKieBase(kieModuleModel, base));
		LOGGER.debug("Creating KIEModuleCacheBuilder for modules [{}]", module.getName());
		final KIEModuleCacheBuilder cache = getRuleEngineCacheService().createKIEModuleCacheBuilder(module);

		LOGGER.debug("Obtaining new ReleaseId for module [{}]", module.getName());
		final ReleaseId newReleaseId = getReleaseId(module);
		LOGGER.debug("New ReleaseId obtained: [{}]", newReleaseId);
		final Optional<ReleaseId> deployedReleaseId = getDeployedReleaseId(module, null);
		// if the ruleengine is active and the releaseId is specified, we may try with incremental update
		if (enableIncrementalUpdate && deployedReleaseId.isPresent())
		{
			final ReleaseId currentReleaseId = deployedReleaseId.get();
			final KieModule deployedKieModule = getKieServices().getRepository().getKieModule(currentReleaseId);
			LOGGER.debug("Deployed KieModule for ReleaseId [{}] : [{}]", currentReleaseId, deployedKieModule);
			if (nonNull(deployedKieModule))
			{
				final List<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> rulesToUpdateList = kieBases.stream()
						.map(kBase -> prepareIncrementalUpdate(currentReleaseId, kBase)).filter(Optional::isPresent)
						.map(Optional::get).collect(toList());

				LOGGER.debug("Rules to deploy [{}]", CollectionUtils.size(rulesToUpdateList));

				final List<DroolsRuleModel> rulesToAdd = rulesToUpdateList.stream().flatMap(u -> u.getLeft().stream())
						.collect(toList());
				final List<DroolsRuleModel> rulesToRemove = rulesToUpdateList.stream().flatMap(u -> u.getRight().stream())
						.collect(toList());
				if (getIncrementalRuleEngineUpdateStrategy()
						.shouldUpdateIncrementally(currentReleaseId, module.getName(), rulesToAdd, rulesToRemove))
				{
					final KieModule cloneKieModule = cloneForIncrementalCompilation((MemoryKieModule) deployedKieModule,
							newReleaseId,
							kieModuleModel);

					final List<AbstractRuleEngineRuleModel> activeRules = getEngineRuleDao().getActiveRules(module);

					kieBases.forEach(kBase -> activeRules.forEach(cache::processRule));
					rulesToUpdateList
							.forEach(u -> deployRulesIncrementally(newReleaseId, kieModuleModel, cloneKieModule, u.getLeft(),
									u.getRight(), result));

					final KieModule kieModule = mergePartialKieModules(newReleaseId, kieModuleModel, cloneKieModule);
					return Pair.of(kieModule, cache);
				}
			}
		}

		final List<KieBuilder> kieBuilders = kieBases.stream()
				.flatMap(base -> deployRules(module, kieModuleModel, base, cache).stream()).collect(toList());

		final KieModule kieModule = mergePartialKieModules(newReleaseId, kieModuleModel, kieBuilders);

		return Pair.of(kieModule, cache);
	}

	protected void copyChanges(final MemoryKieModule trgKieModule, final MemoryKieModule srcKieModule)
	{
		final MemoryFileSystem trgMemoryFileSystem = trgKieModule.getMemoryFileSystem();
		final MemoryFileSystem srcMemoryFileSystem = srcKieModule.getMemoryFileSystem();
		final Map<String, byte[]> compiledCode = srcMemoryFileSystem.getMap();

		for (final Map.Entry<String, byte[]> entry : compiledCode.entrySet())
		{
			final String path = entry.getKey();
			if (!path.startsWith(RuleEngineConstants.DROOLS_BASE_PATH))
			{
				final String resoursePath = RuleEngineConstants.DROOLS_BASE_PATH + path;
				final String normalizedRulePath = getNormalizedRulePath(resoursePath);
				if (resoursePath.endsWith(".drl") && trgMemoryFileSystem.existsFile(normalizedRulePath))
				{
					LOGGER.debug("Removing file: {}", normalizedRulePath);
					trgMemoryFileSystem.remove(normalizedRulePath);
				}
				trgMemoryFileSystem.write(path, entry.getValue());
			}
		}
		trgMemoryFileSystem.mark();
	}

	/**
	 * Method creates the clone of the {@link KieModule}, having the specified {@link ReleaseId}. It creates the new instance of
	 * {@link MemoryKieModule} with given {@link ReleaseId}, {@link KieModuleModel} and new instance of {@link MemoryFileSystem},
	 * copying from original {@link KieModule} all the relevant information (including file system content). (The {@code
	 * #clone.mark();} is used to reset the map of modified files since last mark).
	 *
	 * @param origKieModule
	 * 		The instance of {@link MemoryKieModule} to clone
	 * @param releaseId
	 * 		The new {@link ReleaseId} for a clone {@link MemoryKieModule}
	 * @param kModuleModel
	 * 		instance of new {@link KieModuleModel} for a clone {@link KieModule}
	 * @return clone of {@link MemoryKieModule} with given {@link ReleaseId}
	 */
	protected MemoryKieModule cloneForIncrementalCompilation(final MemoryKieModule origKieModule, final ReleaseId releaseId,
			final KieModuleModel kModuleModel)
	{
		final MemoryFileSystem newMfs = new MemoryFileSystem();
		final MemoryKieModule clone = new MemoryKieModule(releaseId, kModuleModel, newMfs);
		final MemoryFileSystem origMemoryFileSystem = origKieModule.getMemoryFileSystem();
		final Map<String, byte[]> fileContents = origMemoryFileSystem.getMap();
		for (final Map.Entry<String, byte[]> entry : fileContents.entrySet())
		{
			newMfs.write(entry.getKey(), entry.getValue());
		}
		clone.mark();
		for (final InternalKieModule dep : origKieModule.getKieDependencies().values())
		{
			clone.addKieDependency(dep);
		}
		for (final KieBaseModel kBaseModel : origKieModule.getKieModuleModel().getKieBaseModels().values())
		{
			clone.cacheKnowledgeBuilderForKieBase(kBaseModel.getName(),
					origKieModule.getKnowledgeBuilderForKieBase(kBaseModel.getName()));
		}
		clone.setPomModel(origKieModule.getPomModel());
		for (final InternalKieModule dependency : origKieModule.getKieDependencies().values())
		{
			clone.addKieDependency(dependency);
		}
		clone.setUnresolvedDependencies(origKieModule.getUnresolvedDependencies());
		return clone;
	}

	/**
	 * Given the {@link ReleaseId} of the deployed {@link KieModule} the method returns the tuple consisting of rules to
	 * add/update
	 * and rules to delete. Before returning the tuple the method applies the strategy to decide whether the incremental update is
	 * applicable. If it is not applicable the empty {@link Optional} is returned
	 *
	 * @param releaseId
	 * 		{@link ReleaseId} of the currently deployed kie module
	 * @param kieBase
	 * 		{@link DroolsKIEBaseModel} containing the set of rules to deploy
	 * @return a {@link Pair} containing the rules to add/update (left-hand-side) and the rules to delete (right-hand-side)
	 */
	protected Optional<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> prepareIncrementalUpdate(
			final ReleaseId releaseId, final DroolsKIEBaseModel kieBase)                                      // NOSONAR
	{
		LOGGER.debug("Prepare for incremental update for KieBase [{}] and ReleaseId [{}]", kieBase.getName(), releaseId);
		final List<AbstractRuleEngineRuleModel> rules = getEngineRuleDao().getActiveRules(kieBase.getKieModule());
		final Long newModuleVersion = kieBase.getKieModule().getVersion();
		LOGGER.debug("New module version: [{}]", newModuleVersion);
		final Optional<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> rulesToDeploy = Optional.empty();
		if (isNotEmpty(rules))
		{
			final List<String> ruleUuids = rules.stream().filter(this::isRuleValid).map(AbstractRuleEngineRuleModel::getUuid)
					.collect(toList());
			if (!ruleUuids.isEmpty())
			{
				final Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> matchingRules = getContentMatchRulesFilter()
						.apply(ruleUuids, newModuleVersion);
				return Optional.of(matchingRules);
			}
		}
		return rulesToDeploy;
	}

	/**
	 * Deploy incrementally the rule engine updates. It accepts the new {@link ReleaseId}, new {@link KieModuleModel}, the clone
	 * {@link MemoryKieModule} and based on the collections of rules to add/update and delete, applies the incremental rule engine
	 * updates.
	 * In order to add/update the rule engine the new instance of {@link KieBuilder} is created. After the proper {@link
	 * KieBuilderSet} with updates is created, the {@code KieBuilderSet.#build()} method is called. if the update was successful,
	 * the updated information is copied from "incremental" {@link KieModule} to the clone {@link KieModule} (method {@link
	 * #copyChanges(MemoryKieModule, MemoryKieModule)})
	 *
	 * @param releaseId
	 * 		new {@link ReleaseId}
	 * @param kieModuleModel
	 * 		new {@link KieModuleModel}
	 * @param kieModule
	 * 		clone {@link KieModule}
	 * @param rulesToAdd
	 * 		a collection of {@link DroolsRuleModel} rules to add/update
	 * @param rulesToRemove
	 * 		a collection of {@link DroolsRuleModel} rules to remove
	 * @param result
	 * 		instance of {@link RuleEngineActionResult} accumulating the results of deploy
	 */
	protected void deployRulesIncrementally(final ReleaseId releaseId, final KieModuleModel kieModuleModel,
			final KieModule kieModule, final Collection<DroolsRuleModel> rulesToAdd,
			final Collection<DroolsRuleModel> rulesToRemove, final RuleEngineActionResult result)
	{
		if (isNotEmpty(rulesToRemove))
		{
			LOGGER.debug("Rules to remove: {}", rulesToRemove.size());
			deleteRulesFromKieModule((MemoryKieModule) kieModule, rulesToRemove);
		}
		if (isNotEmpty(rulesToAdd))
		{
			final MemoryFileSystem memoryFileSystem = ((MemoryKieModule) kieModule).getMemoryFileSystem();
			final KieFileSystem kfs = new KieFileSystemImpl(memoryFileSystem);
			writeKModuleXML(kieModuleModel, kfs);
			kfs.generateAndWritePomXML(releaseId);
			final KieBuilder kieBuilder = getKieServices().newKieBuilder(kfs);
			final String[] ruleToAddPaths = getRulePaths(rulesToAdd);
			if (isNotEmpty(ruleToAddPaths))
			{
				LOGGER.debug("Rules to add: {}", rulesToAdd.size());
				writeRulesToKieFileSystem(kfs, rulesToAdd);
				final KieBuilderSet kieBuilderSet = ((InternalKieBuilder) kieBuilder).createFileSet(ruleToAddPaths);
				final Results kieBuilderResults = kieBuilder.getResults();
				final List<Message> kieBuilderMessages = kieBuilderResults.getMessages(Message.Level.ERROR);
				verifyErrors(result, kieBuilderMessages);
				final IncrementalResults incrementalResults = kieBuilderSet.build();
				final List<Message> messages = incrementalResults.getAddedMessages();
				verifyErrors(result, messages);
			}
			final KieModule incrementalKieModule = kieBuilder.getKieModule();
			copyChanges((MemoryKieModule) kieModule, (MemoryKieModule) incrementalKieModule);
		}
	}

	protected void verifyErrors(final RuleEngineActionResult result, final List<Message> messages)
	{
		messages.stream().filter(m -> m.getLevel().equals(Message.Level.ERROR))
				.forEach(m -> addNewResultItemOf(result, MessageLevel.ERROR, m.getText()));
		if (messages.stream().anyMatch(m -> m.getLevel().equals(Message.Level.ERROR)))
		{
			throw new DroolsInitializationException(result.getResults(), "Drools rule engine initialization failed");
		}
	}

	protected KieModule mergePartialKieModules(final ReleaseId releaseId, final KieModuleModel kieModuleModel,
			final KieModule partialKieModule)
	{
		LOGGER.debug("Merging partial Kie Module [{}] with Kie Module [{}] for ReleaseId [{}]", partialKieModule, kieModuleModel,
				releaseId);
		final MemoryFileSystem mainMemoryFileSystem = new MemoryFileSystem();
		final InternalKieModule returnKieModule = new MemoryKieModule(releaseId, kieModuleModel, mainMemoryFileSystem);

		mergeFileSystemToKieModule((MemoryKieModule) partialKieModule, mainMemoryFileSystem);

		mainMemoryFileSystem.mark();
		LOGGER.debug("Main KIE module contains [{}] files", mainMemoryFileSystem.getFileNames().size());
		return returnKieModule;
	}

	protected void mergeFileSystemToKieModule(final MemoryKieModule partialKieModule, final MemoryFileSystem mainMemoryFileSystem)
	{
		final MemoryFileSystem partialMemoryFileSystem = partialKieModule.getMemoryFileSystem();
		final Map<String, byte[]> fileContents = partialMemoryFileSystem.getMap();
		for (final Map.Entry<String, byte[]> entry : fileContents.entrySet())
		{
			mainMemoryFileSystem.write(entry.getKey(), entry.getValue());
		}
	}

	protected KieModule mergePartialKieModules(final ReleaseId releaseId, final KieModuleModel kieModuleModel,
			final List<KieBuilder> kieBuilders)
	{
		LOGGER.debug("Merging Kie Module [{} for ReleaseId [{}]", kieModuleModel, releaseId);
		final MemoryFileSystem mainMemoryFileSystem = new MemoryFileSystem();
		final InternalKieModule returnKieModule = new MemoryKieModule(releaseId, kieModuleModel, mainMemoryFileSystem);
		if (isNotEmpty(kieBuilders))
		{
			for (final KieBuilder kieBuilder : kieBuilders)
			{
				final KieModule partialKieModule = kieBuilder.getKieModule();
				mergeFileSystemToKieModule((MemoryKieModule) partialKieModule, mainMemoryFileSystem);
			}
		}
		mainMemoryFileSystem.mark();
		LOGGER.debug("Main KIE module contains [{}] files", mainMemoryFileSystem.getFileNames().size());
		return returnKieModule;
	}

	@Override
	public void addKieBase(final KieModuleModel module, final KieFileSystem kfs, final DroolsKIEBaseModel base,
			final KIEModuleCacheBuilder cache)
	{
		addKieBase(module, base);
		addRules(kfs, base, cache);
	}

	@Override
	public void addKieBase(final KieModuleModel module, final DroolsKIEBaseModel base)
	{
		final KieBaseModel kieBaseModel = module.newKieBaseModel(base.getName());
		kieBaseModel.setEqualsBehavior(getEqualityBehaviorOption(base.getEqualityBehavior()));
		kieBaseModel.setEventProcessingMode(getEventProcessingOption(base.getEventProcessingMode()));
		if (base.equals(base.getKieModule().getDefaultKIEBase()))
		{
			kieBaseModel.setDefault(true);
		}
		base.getKieSessions().forEach(session -> addKieSession(kieBaseModel, session));
	}

	@Override
	public void addKieSession(final KieBaseModel base, final DroolsKIESessionModel session)
	{
		final KieSessionModel kieSession = base.newKieSessionModel(session.getName());
		final DroolsKIEBaseModel kieBase = session.getKieBase();
		final DroolsKIESessionModel defaultKIESession = kieBase.getDefaultKIESession();
		if (nonNull(defaultKIESession))
		{
			final PK pk = defaultKIESession.getPk();
			if (nonNull(pk))
			{
				kieSession.setDefault(pk.equals(session.getPk()));
			}
		}
		final KieSessionType sessionType = getSessionType(session.getSessionType());
		kieSession.setType(sessionType);
	}

	@Override
	public String activateKieModule(final DroolsKIEModuleModel module)
	{
		LOGGER.debug("Activating Kie Module [{}]", module.getName());
		getModelService().refresh(module);
		final String releaseIdVersion = getReleaseId(module).getVersion();
		module.setDeployedMvnVersion(releaseIdVersion);
		LOGGER.debug("Setting deployed mvn version [{}] for module [{}]", releaseIdVersion, module.getName());
		getModelService().save(module);
		return releaseIdVersion;
	}

	@Override
	public boolean removeKieModuleIfPresent(final ReleaseId releaseId, final RuleEngineActionResult result)
	{
		LOGGER.info("Removing old Kie module [{}]", releaseId);
		boolean moduleRemoved = false;
		final KieModule kieModule = getKieServices().getRepository().getKieModule(releaseId);
		if (nonNull(kieModule) && !isInitialEngineStartup(releaseId, result.getDeployedVersion()))
		{
			getKieServices().getRepository().removeKieModule(releaseId);
			moduleRemoved = true;
		}
		LOGGER.debug("Kie module [{}] has {} been removed", releaseId, moduleRemoved ? "" : "not");
		return moduleRemoved;
	}

	@Override
	public boolean removeOldKieModuleIfPresent(final RuleEngineActionResult result)
	{
		boolean moduleRemoved = false;
		LOGGER.info("Removing old Kie module with name {}", result.getModuleName());
		final DroolsKIEModuleModel rulesModule = getRulesModuleDao().findByName(result.getModuleName());
		if (nonNull(rulesModule))
		{
			final ReleaseId releaseId = new ReleaseIdImpl(rulesModule.getMvnGroupId(), rulesModule.getMvnArtifactId(),
					result.getOldVersion());
			moduleRemoved = removeKieModuleIfPresent(releaseId, result);
		}
		LOGGER.debug("Kie module has {} been removed", moduleRemoved ? "" : "not");
		return moduleRemoved;
	}

	@Override
	public void addRules(final KieFileSystem kfs, final DroolsKIEBaseModel base, final KIEModuleCacheBuilder cache)
	{
		LOGGER.debug("Drools Engine Service addRules triggered...");
		final Set<DroolsRuleModel> rules = filterByBiggestVersion(getEngineRuleDao().getActiveRules(base.getKieModule()));

		writeRulesToKieFileSystem(kfs, rules);
		rules.stream().filter(this::isRuleValid).forEach(cache::processRule);
	}

	protected <R extends D, D extends AbstractRuleEngineRuleModel> Set<R> filterByBiggestVersion(final Collection<D> rulesForVersion)
	{
		final Set<R> rules = Sets.newHashSet();
		if (isNotEmpty(rulesForVersion))
		{
			final Map<String, List<R>> twinRulesMap =	rulesForVersion.stream().map(r -> (R) r).collect(groupingBy(D::getCode));

			twinRulesMap.values().stream().map(List::stream).forEach(l -> l.max(comparing(D::getVersion)).ifPresent(rules::add));
		}
		return rules;
	}

	protected boolean isRuleValid(final AbstractRuleEngineRuleModel rule)
	{
		return nonNull(rule.getRuleContent()) && isTrue(rule.getActive());
	}

	protected void writeRulesToKieFileSystem(final KieFileSystem kfs, final Collection<DroolsRuleModel> rules)
	{
		for (final DroolsRuleModel rule : rules)
		{
			if (isRuleValid(rule))
			{
				final String rulePath = getRulePath(rule);
				final String drl = rule.getRuleContent();

				LOGGER.debug("{} {}", rule.getCode(), rulePath);
				LOGGER.debug("{}", drl);

				kfs.write(rulePath, drl);
			}
			if (isNotTrue(rule.getActive()))
			{
				LOGGER.debug("ignoring rule {}. Rule is not active.", rule.getCode());
			}
			else if (isNull(rule.getRuleContent()))
			{
				LOGGER.warn("ignoring rule {}. No ruleContent set!", rule.getCode());
			}
		}
	}

	/**
	 * Removes the specified collection of {@link DroolsRuleModel} rules from {@link KieModule}
	 *
	 * @param kieModule
	 * 		the instance of clone {@link KieModule} to remove rules from
	 * @param rules
	 * 		Collection of {@link DroolsRuleModel} rules to delete
	 */
	protected void deleteRulesFromKieModule(final MemoryKieModule kieModule, final Collection<DroolsRuleModel> rules)
	{
		LOGGER.debug("Delete rules [{}] from Kie Module [{}]", rules.size(), kieModule);
		final String[] rulePaths = getRulePaths(rules);
		if (isNotEmpty(rulePaths))
		{
			final MemoryFileSystem memoryFileSystem = kieModule.getMemoryFileSystem();
			stream(rulePaths).map(RuleEngineUtils::stripDroolsMainResources).forEach(p -> deleteFileIfExists(memoryFileSystem, p));
		}
	}

	private static void deleteFileIfExists(final MemoryFileSystem mfs, final String path)
	{
		if (mfs.existsFile(path))
		{
			mfs.remove(path);
		}
	}

	private String[] getRulePaths(final Collection<DroolsRuleModel> rules)
	{
		return rules.stream().filter(this::isRuleValid).map(RuleEngineUtils::getRulePath).collect(toList())
				.toArray(new String[] {});
	}

	@Override
	public Optional<ReleaseId> getDeployedReleaseId(final DroolsKIEModuleModel module, final String deployedMvnVersion)
	{
		LOGGER.debug("Getting deployed ReleaseId for module [{}] and mvn version [{}]", module.getName(), deployedMvnVersion);
		String deployedReleaseIdVersion = deployedMvnVersion;
		DroolsKIEModuleModel localModule = module;
		if (isNull(deployedReleaseIdVersion))
		{
			localModule = getRulesModuleDao().findByName(module.getName());
			if (nonNull(localModule))
			{
				deployedReleaseIdVersion = localModule.getDeployedMvnVersion();
			}
		}
		Optional<ReleaseId> deployedReleaseId = Optional.empty();
		if (nonNull(getKieServices()) && nonNull(deployedReleaseIdVersion))
		{
			deployedReleaseId = Optional
					.of(getKieServices()
							.newReleaseId(localModule.getMvnGroupId(), localModule.getMvnArtifactId(), deployedReleaseIdVersion));
		}
		LOGGER.debug("Obtained deployed ReleaseId: [{}]", deployedReleaseId.orElse(null));
		return deployedReleaseId;
	}

	@Override
	public void setUpKieServices()
	{
		if (isNull(getKieServices()))
		{
			this.kieServices = getRuleEngineBootstrap().getEngineServices();
		}
	}

	@PostConstruct
	public void setup()
	{
		workerPreDestroyTimeout = configurationService.getConfiguration().getLong(WORKER_PRE_DESTROY_TIMEOUT, 3600000L);
		asyncWorkers = getConcurrentMapFactory().createNew();
		setUpKieServices();
	}

	protected List<KieBuilder> deployRules(final DroolsKIEModuleModel module, final KieModuleModel kieModuleModel,
			final DroolsKIEBaseModel kieBase, final KIEModuleCacheBuilder cache)
	{
		final List<String> rulesUuids = getEngineRuleDao().getActiveRules(module).stream()
				.map(AbstractRuleEngineRuleModel::getUuid).collect(toList());
		final RulePublishingFuture rulePublishingFuture = getRulePublishingSpliterator()
				.publishRulesAsync(kieModuleModel, getReleaseId(module), rulesUuids, cache);
		final RuleDeploymentTaskResult ruleDeploymentResult = (RuleDeploymentTaskResult) rulePublishingFuture.getTaskResult();
		if (ruleDeploymentResult.getState().equals(TaskResult.State.FAILURE))
		{
			throw new DroolsInitializationException(
					ruleDeploymentResult.getRulePublishingResults().stream()
							.filter(result -> isNotEmpty(result.getResults()))
							.flatMap(result -> result.getResults().stream())
							.collect(toList()), "Initialization of rule engine failed during the deployment phase: ");
		}
		return rulePublishingFuture.getPartialKieBuilders();
	}

	@Override
	public KieContainer initializeNewKieContainer(final DroolsKIEModuleModel module, final KieModule kieModule,
			final RuleEngineActionResult result)
	{
		LOGGER.debug("Starting initialization of a new Kie Container for module [{}]", module.getName());
		final ReleaseId releaseId = getReleaseId(module);
		result.setModuleName(module.getName());
		final KieRepository kieRepository = getKieServices().getRepository();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(
					"Drools Engine Service initialization for '{}' module in tenant '{}' finished. ReleaseId of the new Kie Module: '{}'",
					module.getName(), module.getTenantId(), kieModule.getReleaseId().toExternalForm());
		}
		kieRepository.addKieModule(kieModule);
		final KieContainer kieContainer = getKieServices().newKieContainer(releaseId);
		getRuleEngineBootstrap().warmUpRuleEngineContainer(module, kieContainer);
		result.setDeployedVersion(releaseId.getVersion());
		LOGGER.debug("New container has been initialized successfully: [{}]", releaseId.getVersion());
		return kieContainer;
	}

	protected void initializeNewModule(final DroolsKIEModuleModel module,
			final KieContainerListener listener, final boolean enableIncrementalUpdates, final RuleEngineActionResult result)
	{
		try
		{
			LOGGER.debug("Starting initialization of module [{}]", module);
			final InitializeMode initializeMode = getInitializeMode(result.getExecutionContext());
			final boolean isRestoredKieModule = initializeMode == RESTORE && restoreKieModule(module, listener, result);
			if (!isRestoredKieModule)
			{
				newKieModule(module, listener, enableIncrementalUpdates, result);
			}
		}
		catch (final DroolsInitializationException e)
		{
			LOGGER.error("DroolsInitializationException occurred {}", e);
			result.setResults(e.getResults());
			completeWithFailure(getReleaseId(module), result, listener);
		}
		catch (final RuntimeException e)
		{
			LOGGER.error("Drools Engine Service initialization Exception occurred {}", e);
			addNewResultItemOf(result, MessageLevel.ERROR, e.getLocalizedMessage());
			completeWithFailure(getReleaseId(module), result, listener);
		}
	}

	protected InitializeMode getInitializeMode(final ExecutionContext executionContext)
	{
		return nonNull(executionContext) && nonNull(executionContext.getInitializeMode()) ? executionContext.getInitializeMode() : NEW;
	}

	protected boolean restoreKieModule(final DroolsKIEModuleModel module,
			final KieContainerListener listener, final RuleEngineActionResult result)
	{
		final Optional<KieModule> restoredKieModule = getKieModuleService().loadKieModule(module.getName(), getReleaseId(module).toExternalForm());
		if (restoredKieModule.isPresent())
		{
			LOGGER.debug("Serialized module [{}] is found and will be restored", module);
			final KIEModuleCacheBuilder cache = getRuleEngineCacheService().createKIEModuleCacheBuilder(module);
			final Collection<DroolsKIEBaseModel> kieBases = module.getKieBases();
			kieBases.forEach(kb -> addRulesToCache(kb, cache));
			final KieContainer kieContainer = initializeNewKieContainer(module, restoredKieModule.get(), result);
			listener.onSuccess(kieContainer, cache);
			return true;
		}
		return false;
	}

	protected void newKieModule(final DroolsKIEModuleModel module, final KieContainerListener listener,
			final boolean enableIncrementalUpdates, final RuleEngineActionResult result)
	{
		final InitializeMode initializeMode = getInitializeMode(result.getExecutionContext());
		final Pair<KieModule, KIEModuleCacheBuilder> moduleCacheBuilderPair = createKieModule(module, result,
				enableIncrementalUpdates);
		final KieModule newKieModule = moduleCacheBuilderPair.getLeft();
		final KIEModuleCacheBuilder cache = moduleCacheBuilderPair.getRight();

		final KieContainer kieContainer = initializeNewKieContainer(module, newKieModule, result);
		if (initializeMode == NEW)
		{
			getKieModuleService().saveKieModule(module.getName(), getReleaseId(module).toExternalForm(), newKieModule);
			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("KieModule {} / {} has been serialized", module.getName(), getReleaseId(module).toExternalForm());
			}
		}
		listener.onSuccess(kieContainer, cache);
	}

	@Override
	public void addRulesToCache(final DroolsKIEBaseModel base, final KIEModuleCacheBuilder cache)
	{
		LOGGER.debug("Drools Engine Service addRulesToCache triggered...");
		final Set<DroolsRuleModel> rules = filterByBiggestVersion(getEngineRuleDao().getActiveRules(base.getKieModule()));

		rules.stream().filter(this::isRuleValid).forEach(cache::processRule);
	}

	protected void completeWithFailure(final ReleaseId releaseId, final RuleEngineActionResult result,
			final KieContainerListener listener)
	{
		final KieRepository kieRepository = getKieServices().getRepository();
		final KieModule corruptedKieModule = kieRepository.getKieModule(releaseId);
		if (nonNull(corruptedKieModule))
		{
			kieRepository.removeKieModule(releaseId);
		}
		result.setActionFailed(true);
		listener.onFailure(result);
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

	protected void registerWorker(final String moduleName, final Thread worker)
	{
		getSuspendResumeTaskManager().registerAsNonSuspendableTask(worker, "Rule engine module deployment is in progress");
		final Set<Thread> workersForModule = asyncWorkers.get(moduleName);
		Set<Thread> updatedWorkersForModule;
		if (isNull(workersForModule))
		{
			updatedWorkersForModule = ImmutableSet.of(worker);
		}
		else
		{
			final Set<Thread> aliveWorkers = workersForModule.stream().filter(Thread::isAlive).collect(toSet());
			aliveWorkers.add(worker);
			updatedWorkersForModule = ImmutableSet.copyOf(aliveWorkers);
		}
		asyncWorkers.put(moduleName, updatedWorkersForModule);
	}

	protected Runnable switchKieModuleRunnableTask(final String moduleName,
			final KieContainerListener listener, final List<Object> resultsAccumulator, final Supplier<Object> resetFlagSupplier,
			final List<Supplier<Object>> postTaskList, final boolean enableIncrementalUpdate,
			final RuleEngineActionResult result)
	{
		checkArgument(nonNull(resultsAccumulator), "Results accumulator must be initialized upfront");

		return () ->
		{
			result.setModuleName(moduleName);

			try
			{
				final DroolsKIEModuleModel module = getRulesModuleDao().findByName(moduleName);
				resultsAccumulator.addAll(switchKieModule(module, listener, (LinkedList<Supplier<Object>>) postTaskList,
						enableIncrementalUpdate, result));
				return;
			}
			catch (final Exception e)
			{
				onSwapFailed(e, result, resetFlagSupplier);
			}

			result.setActionFailed(true);
			listener.onFailure(result);
		};
	}

	protected Object onSwapFailed(final Throwable t, final RuleEngineActionResult result, final Supplier<Object> resetFlagSupplier)
	{
		LOGGER.error("Exception caught: {}", t);
		addNewResultItemOf(result, MessageLevel.ERROR, t.getLocalizedMessage());
		if (nonNull(resetFlagSupplier))
		{
			return resetFlagSupplier.get();
		}
		return null;
	}

	protected boolean isInitialEngineStartup(final ReleaseId releaseId, final String newDeployedMvnVersion)
	{
		return releaseId.getVersion().equals(newDeployedMvnVersion);
	}

	protected KieServices getKieServices()
	{
		return kieServices;
	}

	protected void setKieServices(final KieServices kieServices)
	{
		this.kieServices = kieServices;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected Tenant getCurrentTenant()
	{
		return currentTenant;
	}

	@Required
	public void setCurrentTenant(final Tenant currentTenant)
	{
		this.currentTenant = currentTenant;
	}

	protected ThreadFactory getTenantAwareThreadFactory()
	{
		return tenantAwareThreadFactory;
	}

	@Required
	public void setTenantAwareThreadFactory(final ThreadFactory tenantAwareThreadFactory)
	{
		this.tenantAwareThreadFactory = tenantAwareThreadFactory;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected RuleEngineCacheService getRuleEngineCacheService()
	{
		return ruleEngineCacheService;
	}

	@Required
	public void setRuleEngineCacheService(final RuleEngineCacheService ruleEngineCacheService)
	{
		this.ruleEngineCacheService = ruleEngineCacheService;
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected ConcurrentMapFactory getConcurrentMapFactory()
	{
		return concurrentMapFactory;
	}

	@Required
	public void setConcurrentMapFactory(final ConcurrentMapFactory concurrentMapFactory)
	{
		this.concurrentMapFactory = concurrentMapFactory;
	}

	protected RulePublishingSpliterator getRulePublishingSpliterator()
	{
		return rulePublishingSpliterator;
	}

	@Required
	public void setRulePublishingSpliterator(final RulePublishingSpliterator rulePublishingSpliterator)
	{
		this.rulePublishingSpliterator = rulePublishingSpliterator;
	}

	protected ContentMatchRulesFilter getContentMatchRulesFilter()
	{
		return contentMatchRulesFilter;
	}

	@Required
	public void setContentMatchRulesFilter(final ContentMatchRulesFilter contentMatchRulesFilter)
	{
		this.contentMatchRulesFilter = contentMatchRulesFilter;
	}

	protected IncrementalRuleEngineUpdateStrategy getIncrementalRuleEngineUpdateStrategy()
	{
		return incrementalRuleEngineUpdateStrategy;
	}

	@Required
	public void setIncrementalRuleEngineUpdateStrategy(
			final IncrementalRuleEngineUpdateStrategy incrementalRuleEngineUpdateStrategy)
	{
		this.incrementalRuleEngineUpdateStrategy = incrementalRuleEngineUpdateStrategy;
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

	protected long getWorkerPreDestroyTimeout()
	{
		return workerPreDestroyTimeout;
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

	protected KieModuleService getKieModuleService()
	{
		return kieModuleService;
	}

	@Required
	public void setKieModuleService(final KieModuleService kieModuleService)
	{
		this.kieModuleService = kieModuleService;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}
}
