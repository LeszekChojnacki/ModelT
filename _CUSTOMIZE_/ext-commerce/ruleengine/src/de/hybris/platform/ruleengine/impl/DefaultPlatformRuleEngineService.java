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
package de.hybris.platform.ruleengine.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULE_ENGINE_ACTIVE;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kie.internal.command.CommandFactory.newInsertElements;

import de.hybris.platform.ruleengine.ExecutionContext;
import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.ResultItem;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleEvaluationResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleEngineCacheService;
import de.hybris.platform.ruleengine.concurrency.RuleEngineTaskProcessor;
import de.hybris.platform.ruleengine.concurrency.TaskExecutionFuture;
import de.hybris.platform.ruleengine.concurrency.TaskResult;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.drools.KieSessionHelper;
import de.hybris.platform.ruleengine.enums.DroolsSessionType;
import de.hybris.platform.ruleengine.event.KieModuleSwappingEvent;
import de.hybris.platform.ruleengine.event.RuleEngineInitializedEvent;
import de.hybris.platform.ruleengine.event.RuleEngineModuleSwapCompletedEvent;
import de.hybris.platform.ruleengine.exception.DroolsInitializationException;
import de.hybris.platform.ruleengine.init.ConcurrentMapFactory;
import de.hybris.platform.ruleengine.init.InitializationFuture;
import de.hybris.platform.ruleengine.init.MultiFlag;
import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.init.RuleEngineContainerRegistry;
import de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTasksProvider;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.strategies.DroolsKIEBaseFinderStrategy;
import de.hybris.platform.ruleengine.util.EngineRulePreconditions;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * The DefaultDroolsRuleEngineService allows initialization and evaluation of rules modules.
 */
public class DefaultPlatformRuleEngineService implements RuleEngineService
{

	public static final String MODULE_MVN_VERSION_NONE = "NONE";
	public static final String SWAPPING_IS_BLOCKING = "ruleengine.kiemodule.swapping.blocking";
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlatformRuleEngineService.class);
	private static final String ERROR_CANNOT_UPDATE_RULE = "Cannot update engine rule, given rule is null";
	private EventService eventService;
	private ConfigurationService configurationService;
	private ModelService modelService;
	private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;
	private EngineRuleDao engineRuleDao;
	private RulesModuleDao rulesModuleDao;
	private RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper;
	private ConcurrentMapFactory concurrentMapFactory;
	private RuleEngineCacheService ruleEngineCacheService;

	private KieSessionHelper kieSessionHelper;
	private RuleEngineTaskProcessor<AbstractRuleEngineRuleModel, TaskResult> engineRulesPersistingTaskProcessor;
	private RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel> ruleEngineBootstrap;
	private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;

	private EngineRulesRepository engineRulesRepository;

	private MultiFlag initialisationMultiFlag;

	private PostRulesModuleSwappingTasksProvider postRulesModuleSwappingTasksProvider;
	private Function<RuleEvaluationContext, Integer> maxRuleExecutionsFunction;



	@Override
	public RuleEvaluationResult evaluate(final RuleEvaluationContext context)
	{
		if (LOGGER.isDebugEnabled() && nonNull(context.getFacts()))
		{
			LOGGER.debug("Rule evaluation triggered with the facts: {}", context.getFacts());
		}
		try
		{
			getRuleEngineContainerRegistry().lockReadingRegistry();
			final RuleEvaluationResult result = new RuleEvaluationResult();
			final ReleaseId deployedReleaseId = getKieSessionHelper().getDeployedKieModuleReleaseId(context);
			KieContainer kContainer = getRuleEngineContainerRegistry().getActiveContainer(deployedReleaseId);
			if (isNull(kContainer))
			{
				LOGGER.warn("KieContainer with releaseId [{}] was not found. Trying to look up for closest matching version...",
						deployedReleaseId);

				final ReleaseId tryDeployedReleaseId = getRuleEngineContainerRegistry().lookupForDeployedRelease(
						deployedReleaseId.getGroupId(),
						deployedReleaseId.getArtifactId())
						.orElseThrow(() -> new DroolsInitializationException(
								"Cannot complete the evaluation: rule engine was not initialized for releaseId ["
										+ deployedReleaseId
										+ "]"));

				LOGGER.info("Found KieContainer with releaseId [{}]", tryDeployedReleaseId);
				kContainer = getRuleEngineContainerRegistry().getActiveContainer(tryDeployedReleaseId);
			}

			final AgendaFilter agendaFilter = (context.getFilter() instanceof AgendaFilter) ? (AgendaFilter) context.getFilter()
					: null;

			getRuleEngineCacheService().provideCachedEntities(context);

			final List<Command> commands = newArrayList();
			commands.add(newInsertElements(context.getFacts()));

			final FireAllRulesCommand fireAllRulesCommand = nonNull(agendaFilter) ? new FireAllRulesCommand(agendaFilter)
					: new FireAllRulesCommand();
			fireAllRulesCommand.setMax(getMaxRuleExecutionsFunction().apply(context));
			LOGGER.debug("Adding command [{}]", fireAllRulesCommand);
			commands.add(fireAllRulesCommand);

			final BatchExecutionCommand command = CommandFactory.newBatchExecution(commands);

			final DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();
			final DroolsSessionType sessionType = ruleEngineContext.getKieSession().getSessionType();
			final Supplier<ExecutionResults> executionResultsSupplier = DroolsSessionType.STATEFUL.equals(sessionType) ?
					executionResultsSupplierWithStatefulSession(kContainer, command, context) :
					executionResultsSupplierWithStatelessSession(kContainer, command, context);

			result.setExecutionResult(executionResultsSupplier.get());
			return result;
		}
		finally
		{
			// make sure to release the lock again
			getRuleEngineContainerRegistry().unlockReadingRegistry();
		}
	}

	protected Supplier<ExecutionResults> executionResultsSupplierWithStatefulSession(final KieContainer kContainer,
			final BatchExecutionCommand command, final RuleEvaluationContext context)
	{
		return () ->
		{

			final KieSession kieSession = (KieSession) getKieSessionHelper()
					.initializeSession(KieSession.class, context, kContainer);
			// execute drools command
			LOGGER.debug("Executing KieSession.execute for releaseId [{}]", kContainer.getReleaseId());
			final ExecutionResults executionResults;
			try
			{
				executionResults = kieSession.execute(command);
			}
			finally
			{
				final Collection<FactHandle> factHandles = kieSession.getFactHandles();
				if (isNotEmpty(factHandles))
				{
					factHandles.forEach(kieSession::delete);
				}
				LOGGER.debug("Disposing the session: {}", kieSession);
				kieSession.dispose();
			}
			return executionResults;
		};
	}

	protected Supplier<ExecutionResults> executionResultsSupplierWithStatelessSession(final KieContainer kContainer,
			final BatchExecutionCommand command, final RuleEvaluationContext context)
	{
		return () ->
		{
			final StatelessKieSession statelessKieSession = (StatelessKieSession) getKieSessionHelper()
					.initializeSession(StatelessKieSession.class, context, kContainer);
			// execute drools command
			LOGGER.debug("Executing StatelessKieSession.execute for releaseId [{}]", kContainer.getReleaseId());
			return statelessKieSession.execute(command);
		};
	}

	@Override
	public InitializationFuture initialize(final List<AbstractRulesModuleModel> modules, final boolean propagateToOtherNodes,
			final boolean enableIncrementalUpdate)
	{
		return initialize(modules, propagateToOtherNodes, enableIncrementalUpdate, new ExecutionContext());
	}

	@Override
	public InitializationFuture initialize(final List<AbstractRulesModuleModel> modules, final boolean propagateToOtherNodes,
			final boolean enableIncrementalUpdate, final ExecutionContext executionContext)
	{
		checkArgument(nonNull(modules), "The provided modules list cannot be NULL here");
		final InitializationFuture initializationFuture = InitializationFuture.of(ruleEngineKieModuleSwapper);
		for (final AbstractRulesModuleModel module : modules)
		{
			final RuleEngineActionResult result = new RuleEngineActionResult();
			result.setExecutionContext(executionContext);

			initialize(module, null, propagateToOtherNodes, enableIncrementalUpdate, result);
			initializationFuture.getResults().add(result);
		}
		return initializationFuture;
	}

	@Override
	public void initializeNonBlocking(final AbstractRulesModuleModel abstractModule,
			final String deployedMvnVersion,
			final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate, final RuleEngineActionResult result)
	{
		if (isRuleEngineActive())
		{
			checkArgument(nonNull(abstractModule), "module must not be null");
			checkState(abstractModule instanceof DroolsKIEModuleModel, "module %s is not a DroolsKIEModule. %s is not supported.",
					abstractModule.getName(), abstractModule.getItemtype());

			LOGGER.debug("Drools Engine Service initialization for '{}' module triggered...", abstractModule.getName());
			final DroolsKIEModuleModel moduleModel = (DroolsKIEModuleModel) abstractModule;

			final Optional<ReleaseId> oldDeployedReleaseId = getRuleEngineKieModuleSwapper()
					.getDeployedReleaseId(moduleModel, deployedMvnVersion);
			final String oldVersion = oldDeployedReleaseId.map(ReleaseId::getVersion).orElse(MODULE_MVN_VERSION_NONE);
			result.setOldVersion(oldVersion);
			switchKieModule(moduleModel, new KieContainerListener()
			{
				@Override
				public void onSuccess(final KieContainer kieContainer, final KIEModuleCacheBuilder cache)
				{
					doSwapKieContainers(kieContainer, cache, result, moduleModel, deployedMvnVersion, propagateToOtherNodes);
					final String oldVersion = oldDeployedReleaseId.map(ReleaseId::getVersion).orElse("NONE");
					getEventService().publishEvent(RuleEngineModuleSwapCompletedEvent.ofSuccess(result.getModuleName(), oldVersion,
							result.getDeployedVersion(), result.getResults()));
				}

				@Override
				public void onFailure(final RuleEngineActionResult result)
				{
					LOGGER.error("Kie Containers initialisation/swapping failed: {}", result.getMessagesAsString(MessageLevel.ERROR));
					result.setDeployedVersion(oldVersion);
					getEventService().publishEvent(RuleEngineModuleSwapCompletedEvent.ofFailure(result.getModuleName(), oldVersion,
							result.getMessagesAsString(MessageLevel.ERROR), result.getResults()));
				}
			}, propagateToOtherNodes, enableIncrementalUpdate, result, getPostRulesModuleSwappingTasksProvider().getTasks(result));
		}
		else
		{
			populateRuleEngineActionResult(
					result,
					format(
							"cannot activate rules module as the rule engine is not active. To activate it set the property %s to true.",
							RULE_ENGINE_ACTIVE),
					abstractModule != null ? abstractModule.getName() : null, false, MessageLevel.WARNING);
		}
	}

	@Override
	public void initialize(final AbstractRulesModuleModel abstractModule,
			final String deployedMvnVersion, final boolean propagateToOtherNodes,
			final boolean enableIncrementalUpdate, final RuleEngineActionResult result)
	{
		initialize(abstractModule, deployedMvnVersion, propagateToOtherNodes, enableIncrementalUpdate, result, null);
	}

	@Override
	public void initialize(final AbstractRulesModuleModel abstractModule,
			final String deployedMvnVersion, final boolean propagateToOtherNodes,
			final boolean enableIncrementalUpdate, final RuleEngineActionResult result, final ExecutionContext executionContext)
	{
		if (nonNull(executionContext))
		{
			result.setExecutionContext(executionContext);
		}
		initializeNonBlocking(abstractModule, deployedMvnVersion, propagateToOtherNodes, enableIncrementalUpdate, result);
		if (isBlocking())
		{
			getRuleEngineKieModuleSwapper().waitForSwappingToFinish();
		}
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> void deactivateRulesModuleEngineRules(final String moduleName,
			final Collection<T> engineRules)
	{
		if (isNotEmpty(engineRules))
		{
			final List<AbstractRuleEngineRuleModel> engineRulesForModule = engineRules.stream()
					.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r, moduleName)).collect(toList());
			if (isNotEmpty(engineRulesForModule))
			{
				final AbstractRulesModuleModel moduleModel = getRulesModuleDao().findByName(moduleName);
				LOGGER.debug("Resetting the module version to [{}]", moduleModel.getVersion());
				final AtomicLong initVal = new AtomicLong(moduleModel.getVersion() + 1);
				final TaskExecutionFuture<TaskResult> taskExecutionFuture = getEngineRulesPersistingTaskProcessor()
						.execute(engineRulesForModule, listPartition ->
						{
							listPartition.forEach(getModelService()::refresh);
							listPartition.forEach(r ->
							{
								r.setActive(false);
								r.setVersion(initVal.getAndAdd(1));
							});
							listPartition.forEach(getModelService()::save);
						});
				taskExecutionFuture.waitForTasksToFinish();
			}
		}
	}

	protected void doSwapKieContainers(final KieContainer kieContainer, final KIEModuleCacheBuilder cache,
			final RuleEngineActionResult ruleEngineActionResult, final DroolsKIEModuleModel module,
			final String deployedReleaseIdVersion,
			final boolean propagateToOtherNodes)
	{
		getRuleEngineContainerRegistry().lockWritingRegistry();
		try
		{
			getRuleEngineBootstrap().activateNewRuleEngineContainer(kieContainer, cache, ruleEngineActionResult, module,
					deployedReleaseIdVersion);
			if (propagateToOtherNodes)
			{
				notifyOtherNodes(ruleEngineActionResult);
			}
		}
		finally
		{
			getRuleEngineContainerRegistry().unlockWritingRegistry();
		}
		LOGGER.info("Swapping to a newly created kie container [{}] has finished successfully", kieContainer.getReleaseId());
	}

	protected boolean isBlocking()
	{
		return getConfigurationService().getConfiguration().getBoolean(SWAPPING_IS_BLOCKING, false);
	}


	/**
	 * Switches the Kie modules in the rule execution context
	 *
	 * @param module
	 *           instance of the {@link DroolsKIEModuleModel} module
	 * @param listener
	 *           instance of {@link KieContainerListener} that implements the switching logic
	 * @param propagateToOtherNodes
	 *           flag, if true, enables the module swap/initialization event be propagated in the cluster
	 * @param enableIncrementalUpdate
	 *           flag, if true, enables for incremental updates of the rule engine kie module
	 * @param result
	 *           instance of {@link RuleEngineActionResult} to be used for cluster nodes notification
	 * @param chainOfPostTasks
	 *           chain of suppliers to be used as a sequence of post-swapping tasks
	 */
	protected void switchKieModule(final DroolsKIEModuleModel module,
			final KieContainerListener listener, final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate,
			final RuleEngineActionResult result, final Collection<Supplier<Object>> chainOfPostTasks)
	{
		final String moduleName = module.getName();
		if (initialisationMultiFlag.compareAndSet(moduleName, false, true))
		{
			final Supplier<Object> resetFlagSupplier = () -> initialisationMultiFlag.compareAndSet(moduleName, true, false);
			try
			{
				final List<Object> resultAccumulator = newArrayList();
				final LinkedList<Supplier<Object>> postTaskList = newLinkedList();
				if (nonNull(chainOfPostTasks))
				{
					postTaskList.addAll(chainOfPostTasks);
				}
				postTaskList.addLast(resetFlagSupplier);
				getModelService().save(module);
				if (propagateToOtherNodes)
				{
					notifyOtherNodesAboutKieModuleSwapping(moduleName, result.getOldVersion());
				}
				getRuleEngineKieModuleSwapper().switchKieModuleAsync(moduleName, listener, resultAccumulator, resetFlagSupplier,
						postTaskList, enableIncrementalUpdate, result);
			}
			catch (final Exception e) //NOSONAR
			{
				LOGGER.error("Exception occurred on Kie Module switching", e);
				populateRuleEngineActionResult(result, e.getMessage(), moduleName, false, MessageLevel.ERROR);
				resetFlagSupplier.get();
				listener.onFailure(result);
			}
		}
		else
		{
			LOGGER.error("Kie container swapping is in progress, no rules updates are possible at this time");
			throw new DroolsInitializationException(
					"Kie container swapping is in progress, no rules updates are possible at this time");
		}
	}

	protected void notifyOtherNodes(final RuleEngineActionResult result)
	{
		if (!result.isActionFailed())
		{
			LOGGER.info("Publishing event that Kie Container swap completed successfully for module [{}] and version [{}]",
					result.getModuleName(),
					result.getDeployedVersion());
			getEventService().publishEvent(new RuleEngineInitializedEvent(result.getModuleName(), result.getOldVersion()));
		}
	}

	protected void notifyOtherNodesAboutKieModuleSwapping(final String moduleName, final String deployedReleaseIdVersion)
	{
		LOGGER.info("Publishing event that Kie Container swap started for module [{}] and release ID [{}]", moduleName,
				deployedReleaseIdVersion);
		getEventService().publishEvent(new KieModuleSwappingEvent(moduleName, deployedReleaseIdVersion));
	}

	protected boolean isRuleEngineActive()
	{
		return getConfigurationService().getConfiguration().getBoolean(RULE_ENGINE_ACTIVE, true);
	}

	@Override
	public List<RuleEngineActionResult> initializeAllRulesModules(final boolean propagateToOtherNodes)
	{
		List<RuleEngineActionResult> results = newArrayList();
		if (isRuleEngineActive())
		{
			LOGGER.info("Starting rules module activation");
			final InitializationFuture initializationFuture = initialize(getRulesModuleDao().findAll(), propagateToOtherNodes, false)
					.waitForInitializationToFinish();
			results = initializationFuture.getResults();
		}
		else
		{
			results.add(createRuleEngineActionResult(
					"cannot activate rules module as the rule engine is not active. "
							+ "To activate it set the property ruleengine.engine.active to true.",
					null, false, MessageLevel.WARNING));
		}
		return results;
	}

	@Override
	public List<RuleEngineActionResult> initializeAllRulesModules()
	{
		return initializeAllRulesModules(true);
	}

	@Override
	public RuleEngineActionResult updateEngineRule(@Nonnull final AbstractRuleEngineRuleModel ruleEngineRule,
			@Nonnull final AbstractRulesModuleModel rulesModule)
	{
		checkArgument(nonNull(ruleEngineRule), "ruleEngineRule argument must not be null");
		checkArgument(nonNull(rulesModule), "rulesModule argument must not be null");
		checkArgument(ruleEngineRule instanceof DroolsRuleModel, "ruleEngineRule must be an instance of DroolsRuleModel");
		checkArgument(rulesModule instanceof DroolsKIEModuleModel, "rulesModule must be an instance of DroolsKIEModuleModel");

		final DroolsRuleModel droolsRule = (DroolsRuleModel) ruleEngineRule;
		final DroolsKIEModuleModel droolsRulesModule = (DroolsKIEModuleModel) rulesModule;

		return addRuleToKieBase(droolsRule, droolsRulesModule);
	}

	private RuleEngineActionResult addRuleToKieBase(final DroolsRuleModel droolsRule, final DroolsKIEModuleModel droolsRulesModule)
	{
		final RuleEngineActionResult result;
		final DroolsKIEBaseModel kieBase = getDroolsKIEBaseFinderStrategy().getKIEBaseForKIEModule(droolsRulesModule);
		if (isNull(kieBase))
		{
			final String message = format(
					"Cannot update engine rule, given rules module with name: %s doesn't have any KIE base.",
					droolsRulesModule.getName());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, droolsRulesModule.getName(), false, MessageLevel.ERROR);
		}
		else
		{
			droolsRule.setKieBase(kieBase);
			getModelService().save(droolsRule);
			getModelService().refresh(kieBase);
			result = createRuleEngineActionResult(format("successfully updated rule with code: %s", droolsRule.getCode()),
					droolsRulesModule.getName(), true, MessageLevel.INFO);
		}
		return result;
	}

	protected Optional<RuleEngineActionResult> validateEngineRulesModule(final AbstractRulesModuleModel rulesModule)
	{
		RuleEngineActionResult result = null;
		if (isNull(rulesModule))
		{
			final String ERROR_MESSAGE = "Cannot update engine rule, given rule module is null";
			LOGGER.error(ERROR_MESSAGE);
			result = createRuleEngineActionResult(ERROR_MESSAGE, null, false, MessageLevel.ERROR);
		}
		else if (!(rulesModule instanceof DroolsKIEModuleModel))
		{
			final String message = format(
					"Cannot update engine rule, given rules module with name: %s is not DroolsKIEModuleModel, but %s.",
					rulesModule.getName(), rulesModule.getItemtype());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, null, false, MessageLevel.ERROR);
		}
		return Optional.ofNullable(result);
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleEngineActionResult archiveRule(@Nonnull final AbstractRuleEngineRuleModel ruleEngineRule)
	{

		checkArgument(nonNull(ruleEngineRule), ERROR_CANNOT_UPDATE_RULE);
		checkArgument(ruleEngineRule instanceof DroolsRuleModel,
				"Cannot update engine rule, given rule with code: %s is not DroolsRuleModel, but %s.",
				ruleEngineRule.getCode(),
				ruleEngineRule.getItemtype());
		final DroolsRuleModel droolsRule = (DroolsRuleModel) ruleEngineRule;

		final RuleEngineActionResult result;
		if (droolsRule.getKieBase() != null)
		{
			result = archiveRule(ruleEngineRule, droolsRule.getKieBase().getKieModule());
		}
		else
		{
			final String message = format(
					"Cannot archive DroolsRule with code %s and type %s. Can't find corresponding DroolsKIEModuleModel or DroolsKIEBaseModel.",
					ruleEngineRule.getCode(), ruleEngineRule.getItemtype());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, null, false, MessageLevel.ERROR);
		}
		return result;
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleEngineActionResult archiveRule(@Nonnull final AbstractRuleEngineRuleModel ruleEngineRule,
			@Nonnull final AbstractRulesModuleModel rulesModule)
	{
		RuleEngineActionResult result;
		checkArgument(nonNull(ruleEngineRule), ERROR_CANNOT_UPDATE_RULE);
		checkArgument(nonNull(rulesModule), "Cannot update engine rule, given module is null");
		checkArgument(ruleEngineRule instanceof DroolsRuleModel,
				"Cannot update engine rule, given rule with code: %s is not DroolsRuleModel, but %s.", ruleEngineRule.getCode(),
				ruleEngineRule.getItemtype());
		checkArgument(rulesModule instanceof DroolsKIEModuleModel,
				"Cannot update engine rule, given rules module with name: %s is not DroolsKIEModuleModel, but %s.",
				rulesModule.getName(), rulesModule.getItemtype());
		final DroolsRuleModel droolsRule = (DroolsRuleModel) ruleEngineRule;

		if (isFalse(droolsRule.getActive()))
		{
			final String message = format("Cannot archive DroolsRule. Given rule: %s is already inactive.",
					ruleEngineRule.getCode());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, null, false, MessageLevel.ERROR);
		}
		else if (!droolsRule.getKieBase().getKieModule().equals(rulesModule))
		{
			final String message = format("Cannot archive DroolsRule. Given rule: %s does not belong to %s Rules Module.",
					ruleEngineRule.getCode(), rulesModule.getName());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, rulesModule.getName(), false, MessageLevel.ERROR);
		}
		else if (droolsRule.getKieBase() == null)
		{
			final String message = format("Cannot archive DroolsRule. Given rule: %s does not belong to any KIE base.",
					ruleEngineRule.getCode());
			LOGGER.error(message);
			result = createRuleEngineActionResult(message, rulesModule.getName(), false, MessageLevel.ERROR);
		}
		else
		{
			droolsRule.setActive(Boolean.FALSE);
			droolsRule.getSourceRule().setStatus(RuleStatus.ARCHIVED);
			getModelService().saveAll(droolsRule, droolsRule.getSourceRule());
			final InitializationFuture initializationFuture = initialize(singletonList(rulesModule), true, true)
					.waitForInitializationToFinish();
			final List<RuleEngineActionResult> results = initializationFuture.getResults();
			if (isEmpty(results) || results.stream().noneMatch(RuleEngineActionResult::isActionFailed))
			{
				result = createRuleEngineActionResult(format("successfully archived rule with code: %s", ruleEngineRule.getCode()),
						rulesModule.getName(), true, MessageLevel.INFO);
			}
			else
			{
				result = results.get(0);
			}
		}
		return result;
	}

	@Override
	public <T extends DroolsRuleModel> Optional<InitializationFuture> archiveRules(@Nonnull final Collection<T> engineRules)
	{
		if (isNotEmpty(engineRules))
		{
			EngineRulePreconditions.checkRulesHaveSameType(engineRules);
			engineRules.forEach(EngineRulePreconditions::checkRuleHasKieModule);

			final Set<T> activeEngineRules = engineRules.stream().filter(r -> r.getCurrentVersion() && r.getActive())
					.collect(toSet());
			if (isNotEmpty(activeEngineRules))
			{
				final List<AbstractRulesModuleModel> modules = activeEngineRules.stream().map(r -> r.getKieBase().getKieModule())
						.distinct().collect(toList());

				for (final T droolsRule : activeEngineRules)
				{
					droolsRule.setActive(Boolean.FALSE);
				}
				getModelService().saveAll(activeEngineRules);
				return Optional.of(initialize(modules, true, true));
			}
		}
		return Optional.empty();
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleForCodeAndModule(final String code, final String moduleName)
	{
		return getEngineRuleDao().getRuleByCode(code, moduleName);
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleForUuid(final String uuid)
	{
		return getEngineRuleDao().getRuleByUuid(uuid);
	}

	protected EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
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

	/**
	 * creates a RuleEngineActionResult object with the given parameters. The result contains one ResultItem object with
	 * the given {@code message} and {@code level}.
	 *
	 * @param message
	 *           the message
	 * @param moduleName
	 *           the module's name
	 * @param success
	 *           whether the operation of this result was successful or not (sets
	 *           {@link RuleEngineActionResult#setActionFailed(boolean)} with the negated value of {@code success}
	 * @param level
	 *           the message level of the ResultItem
	 * @return a new RuleEngineActionResult object
	 */
	protected RuleEngineActionResult createRuleEngineActionResult(final String message, final String moduleName,
			final boolean success, final MessageLevel level)
	{
		final RuleEngineActionResult result = new RuleEngineActionResult();
		populateRuleEngineActionResult(result, message, moduleName, success, level);
		return result;
	}

	/**
	 * populates a RuleEngineActionResult object with the given parameters. Changes the state of
	 * {@link RuleEngineActionResult} and
	 * should be used with care
	 *
	 * @param result
	 *           the {@link RuleEngineActionResult} instance to be populated with given fields
	 * @param message
	 *           the message
	 * @param moduleName
	 *           the module's name
	 * @param success
	 *           whether the operation of this result was successful or not (sets
	 *           {@link RuleEngineActionResult#setActionFailed(boolean)} with the negated value of {@code success}
	 * @param level
	 *           the message level of the ResultItem
	 */
	protected void populateRuleEngineActionResult(final RuleEngineActionResult result, final String message,
			final String moduleName, final boolean success, final MessageLevel level)
	{
		result.setActionFailed(!success);
		final ResultItem item = new ResultItem();
		item.setMessage(message);
		item.setLevel(level);
		result.setResults(Collections.singleton(item));
		result.setModuleName(moduleName);
	}

	/**
	 * The method is used to set the date format and KIEServices before the class is put into service(dependency
	 * injection).
	 */
	@PostConstruct
	public void setup()
	{
		initialisationMultiFlag = new MultiFlag(getConcurrentMapFactory());
		final String dateFormat = getConfigurationService().getConfiguration()
				.getString(RuleEngineConstants.DROOLS_DATE_FORMAT_KEY);
		if (isNotBlank(dateFormat))
		{
			System.setProperty(RuleEngineConstants.DROOLS_DATE_FORMAT_KEY, dateFormat);
		}
		getRuleEngineKieModuleSwapper().setUpKieServices();
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

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
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

	protected RuleEngineKieModuleSwapper getRuleEngineKieModuleSwapper()
	{
		return ruleEngineKieModuleSwapper;
	}

	@Required
	public void setRuleEngineKieModuleSwapper(final RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper)
	{
		this.ruleEngineKieModuleSwapper = ruleEngineKieModuleSwapper;
	}

	protected DroolsKIEBaseFinderStrategy getDroolsKIEBaseFinderStrategy()
	{
		return droolsKIEBaseFinderStrategy;
	}

	@Required
	public void setDroolsKIEBaseFinderStrategy(final DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy)
	{
		this.droolsKIEBaseFinderStrategy = droolsKIEBaseFinderStrategy;
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

	protected ConcurrentMapFactory getConcurrentMapFactory()
	{
		return concurrentMapFactory;
	}

	@Required
	public void setConcurrentMapFactory(final ConcurrentMapFactory concurrentMapFactory)
	{
		this.concurrentMapFactory = concurrentMapFactory;
	}

	protected MultiFlag getInitializationMultiFlag()
	{
		return initialisationMultiFlag;
	}

	protected KieSessionHelper getKieSessionHelper()
	{
		return kieSessionHelper;
	}

	@Required
	public void setKieSessionHelper(final KieSessionHelper kieSessionHelper)
	{
		this.kieSessionHelper = kieSessionHelper;
	}

	protected RuleEngineTaskProcessor<AbstractRuleEngineRuleModel, TaskResult> getEngineRulesPersistingTaskProcessor()
	{
		return engineRulesPersistingTaskProcessor;
	}

	@Required
	public void setEngineRulesPersistingTaskProcessor(
			final RuleEngineTaskProcessor<AbstractRuleEngineRuleModel, TaskResult> engineRulesPersistingTaskProcessor)
	{
		this.engineRulesPersistingTaskProcessor = engineRulesPersistingTaskProcessor;
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

	protected RuleEngineContainerRegistry<ReleaseId, KieContainer> getRuleEngineContainerRegistry()
	{
		return ruleEngineContainerRegistry;
	}

	@Required
	public void setRuleEngineContainerRegistry(
			final RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry)
	{
		this.ruleEngineContainerRegistry = ruleEngineContainerRegistry;
	}

	protected EngineRulesRepository getEngineRulesRepository()
	{
		return engineRulesRepository;
	}

	@Required
	public void setEngineRulesRepository(final EngineRulesRepository engineRulesRepository)
	{
		this.engineRulesRepository = engineRulesRepository;
	}

	protected PostRulesModuleSwappingTasksProvider getPostRulesModuleSwappingTasksProvider()
	{
		return postRulesModuleSwappingTasksProvider;
	}

	@Required
	public void setPostRulesModuleSwappingTasksProvider(
			final PostRulesModuleSwappingTasksProvider postRulesModuleSwappingTasksProvider)
	{
		this.postRulesModuleSwappingTasksProvider = postRulesModuleSwappingTasksProvider;
	}

	protected Function<RuleEvaluationContext, Integer> getMaxRuleExecutionsFunction()
	{
		return maxRuleExecutionsFunction;
	}

	@Required
	public void setMaxRuleExecutionsFunction(final Function<RuleEvaluationContext, Integer> maxRuleExecutionsFunction)
	{
		this.maxRuleExecutionsFunction = maxRuleExecutionsFunction;
	}
}
