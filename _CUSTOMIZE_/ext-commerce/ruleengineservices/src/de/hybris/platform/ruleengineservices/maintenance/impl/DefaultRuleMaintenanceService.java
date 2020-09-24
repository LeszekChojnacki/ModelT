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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.hybris.platform.cronjob.jalo.CronJobProgressTracker;
import de.hybris.platform.ruleengine.ExecutionContext;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.concurrency.GuardStatus;
import de.hybris.platform.ruleengine.concurrency.GuardedSuspension;
import de.hybris.platform.ruleengine.concurrency.TaskResult;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.event.RuleUpdatedEvent;
import de.hybris.platform.ruleengine.exception.RuleEngineRuntimeException;
import de.hybris.platform.ruleengine.init.InitializationFuture;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.strategies.RulesModuleResolver;
import de.hybris.platform.ruleengine.util.EngineRulePreconditions;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContextProvider;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerFuture;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult.Result;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerSpliterator;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerTaskResult;
import de.hybris.platform.ruleengineservices.maintenance.RuleMaintenanceService;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.hybris.platform.ruleengine.util.RuleMappings.moduleName;
import static de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult.Result.COMPILER_ERROR;
import static de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult.Result.SUCCESS;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;


/**
 * Default implementation of {@link RuleMaintenanceService}
 */
public class DefaultRuleMaintenanceService implements RuleMaintenanceService
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultRuleMaintenanceService.class);

	private ModelService modelService;
	private RuleEngineService ruleEngineService;
	private RuleService ruleService;
	private RulesModuleResolver rulesModuleResolver;
	private RulesModuleDao rulesModuleDao;
	private EngineRuleDao engineRuleDao;
	private RuleCompilationContextProvider ruleCompilationContextProvider;
	private GuardedSuspension<String> rulesCompilationGuardedSuspension;
	private EngineRulesRepository engineRulesRepository;
	private EventService eventService;

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleCompilerPublisherResult archiveRule(@Nonnull final AbstractRuleModel rule)
	{
		validateParameterNotNull(rule, "Rule cannot be null");

		final List<AbstractRuleEngineRuleModel> deployedEngineRules = rule.getEngineRules().stream()
				.map(DroolsRuleModel.class::cast)
				.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r, moduleName(r))).collect(toList());

		return isEmpty(deployedEngineRules) ? archiveNonDeployedRule(rule) : archiveDeployedRule(deployedEngineRules);
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected RuleCompilerPublisherResult archiveNonDeployedRule(final AbstractRuleModel rule)
	{
		rule.setStatus(RuleStatus.ARCHIVED);
		getModelService().save(rule);

		getEventService().publishEvent(new RuleUpdatedEvent(rule.getCode()));

		return new DefaultRuleCompilerPublisherResult(SUCCESS, emptyList(), emptyList());
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected RuleCompilerPublisherResult archiveDeployedRule(final List<AbstractRuleEngineRuleModel> deployedEngineRules)
	{
		final List<RuleEngineActionResult> results = deployedEngineRules.stream().map(getRuleEngineService()::archiveRule)
				.collect(toList());

		final Result result = results.stream().filter(RuleEngineActionResult::isActionFailed).findAny()
				.map(r -> Result.PUBLISHER_ERROR).orElse(Result.SUCCESS);

		return new DefaultRuleCompilerPublisherResult(result, emptyList(), results);
	}

	@Override
	public <T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRulesWithBlocking(final List<T> rules,
			final String moduleName, final boolean enableIncrementalUpdate)
	{
		return compileAndPublishRules(rules, moduleName, enableIncrementalUpdate, true);
	}

	@Override
	public <T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRulesWithBlocking(final List<T> rules,
			final String moduleName, final boolean enableIncrementalUpdate, final CronJobProgressTracker cronJobProgressTracker)
	{
		return compileAndPublishRules(rules, moduleName, enableIncrementalUpdate, cronJobProgressTracker, true);
	}

	@Override
	public <T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRules(final List<T> rules,
			final String moduleName, final boolean enableIncrementalUpdate)
	{
		return compileAndPublishRules(rules, moduleName, enableIncrementalUpdate, false);
	}

	@Override
	public RuleCompilerPublisherResult initializeModule(final String moduleName,
			final boolean enableIncrementalUpdate)
	{
		final DroolsKIEModuleModel module = getRulesModuleByName(moduleName);
		final InitializationFuture initializationFuture = getRuleEngineService()
				.initialize(Lists.newArrayList(module), true, enableIncrementalUpdate);
		final List<RuleEngineActionResult> ruleEngineActionResults = initializationFuture.waitForInitializationToFinish()
				.getResults();
		final Result result = ruleEngineActionResults.stream().filter(RuleEngineActionResult::isActionFailed).findAny()
				.map(r -> Result.PUBLISHER_ERROR).orElse(Result.SUCCESS);

		return new DefaultRuleCompilerPublisherResult(result, null, ruleEngineActionResults);
	}

	@Override
	public RuleCompilerPublisherResult initializeAllModules(final boolean enableIncrementalUpdate)
	{
		final List<RuleEngineActionResult> ruleEngineActionResults = getRuleEngineService().initializeAllRulesModules();
		final Result result = ruleEngineActionResults.stream().filter(RuleEngineActionResult::isActionFailed).findAny()
				.map(r -> Result.PUBLISHER_ERROR).orElse(Result.SUCCESS);

		return new DefaultRuleCompilerPublisherResult(result, null, ruleEngineActionResults);
	}

	@Override
	public <T extends SourceRuleModel> Optional<RuleCompilerPublisherResult> undeployRules(final List<T> rules,
			final String moduleName)
	{
		if (isNotEmpty(rules))
		{
			final Set<DroolsRuleModel> engineRules = rules.stream().filter(s -> isNotEmpty(s.getEngineRules()))
					.flatMap(s -> s.getEngineRules().stream())
					.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r, moduleName))
					.map(r -> (DroolsRuleModel) r).collect(toSet());

			EngineRulePreconditions.checkRulesHaveSameType(engineRules);

			if (isNotEmpty(engineRules))
			{
				Result result = Result.SUCCESS;
				final Optional<InitializationFuture> initializationFuture = getRuleEngineService().archiveRules(engineRules);
				if (initializationFuture.isPresent())
				{
					final List<RuleEngineActionResult> results = initializationFuture.get().waitForInitializationToFinish().getResults();
					result = results.stream().filter(RuleEngineActionResult::isActionFailed).findAny()
							.map(r -> Result.PUBLISHER_ERROR)
							.orElse(result);

					return Optional.of(new DefaultRuleCompilerPublisherResult(result, emptyList(), results));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<RuleCompilerPublisherResult> synchronizeModules(final String sourceModuleName, final String targetModuleName)
	{
		Preconditions.checkArgument(Objects.nonNull(sourceModuleName), "The source module name should be provided");
		Preconditions.checkArgument(Objects.nonNull(targetModuleName), "The target module name should be provided");

		final DroolsKIEModuleModel sourceModule = getRulesModuleDao().findByName(sourceModuleName);
		final DroolsKIEModuleModel targetModule = getRulesModuleDao().findByName(targetModuleName);

		if (!sourceModule.getRuleType().equals(targetModule.getRuleType()))
		{
			throw new RuleEngineRuntimeException(String.format("Cannot synchronize modules with different rule types [%s -> %s]",
					sourceModule.getRuleType(), targetModule.getRuleType()));
		}

		final Map<? extends SourceRuleModel, DroolsRuleModel> deployedRulesForSourceModule = getDeployedRules(sourceModule);
		final Map<? extends SourceRuleModel, DroolsRuleModel> deployedRulesForTargetModule = getDeployedRules(targetModule);

		final Map<String, ? extends SourceRuleModel> sourceRulesDeployedForSourceModule = getSourceRulesByName(
				deployedRulesForSourceModule.keySet());
		final Map<String, ? extends SourceRuleModel> sourceRulesDeployedForTargetModule = getSourceRulesByName(
				deployedRulesForTargetModule.keySet());

		List<? extends SourceRuleModel> rulesToDeploy = Collections.emptyList();
		List<AbstractRuleModel> rulesToRemove = Collections.emptyList();
		if (MapUtils.isNotEmpty(sourceRulesDeployedForTargetModule))
		{
			if (MapUtils.isNotEmpty(sourceRulesDeployedForSourceModule))
			{
				rulesToDeploy = sourceRulesDeployedForSourceModule.values().stream()
						.filter(r -> !isSourceRuleDeployed(sourceRulesDeployedForTargetModule, r)).collect(toList());
				rulesToRemove = sourceRulesDeployedForTargetModule.values().stream()
						.filter(r -> !isSourceRuleDeployed(sourceRulesDeployedForSourceModule, r)).collect(toList());
			}
			else
			{
				rulesToRemove = new ArrayList<>(sourceRulesDeployedForTargetModule.values());
			}
		}
		else if (MapUtils.isNotEmpty(sourceRulesDeployedForSourceModule))
		{
			rulesToDeploy = new ArrayList<>(sourceRulesDeployedForSourceModule.values());
		}
		if (isNotEmpty(rulesToDeploy) || isNotEmpty(rulesToRemove))
		{
			getRuleEngineService()
					.deactivateRulesModuleEngineRules(targetModuleName,
							rulesToRemove.stream().map(deployedRulesForTargetModule::get).collect(toList()));

			return Optional.ofNullable(compileAndPublishRules(rulesToDeploy, targetModule.getName(), true, false));
		}
		return Optional.empty();
	}

	/**
	 * Given the map of deployed source rules, verify whether the provided rule instance makes part of it
	 * @param deployedRulesMap
	 *           map of deployed source rules (by name)
	 * @param sourceRule
	 *           source rule instance to check
	 * @param <S>
	 *           type of the source rule
	 * @return true if the provided source rule instance makes part of deployed rules. False otherwise
	 */
	protected <S extends SourceRuleModel> boolean isSourceRuleDeployed(final Map<String, S> deployedRulesMap,
			final SourceRuleModel sourceRule)
	{
		return deployedRulesMap.containsKey(sourceRule.getCode())
				&& deployedRulesMap.get(sourceRule.getCode()).getVersion().equals(sourceRule.getVersion());
	}

	/**
	 * Maps provided collection of source rules to their names
	 * @param sourceRules
	 *           collection of source rules to map
	 * @param <S>
	 *           generic type of source rules
	 * @return map of source rules by code
	 */
	protected <S extends SourceRuleModel> Map<String, S> getSourceRulesByName(final Collection<S> sourceRules)
	{
		return sourceRules.stream().collect(toConcurrentMap(AbstractRuleModel::getCode, identity()));
	}

	/**
	 * Given the rule engine module get the map of source rules to deployed drools rules
	 * @param module
	 *           instance of {@link DroolsKIEModuleModel} to find a map for
	 * @param <S>
	 *           generic type of source rules
	 * @return map of deployed drools rule by corresponding source rule
	 */
	@SuppressWarnings("unchecked")
	protected <S extends SourceRuleModel> Map<S, DroolsRuleModel> getDeployedRules(final DroolsKIEModuleModel module)
	{
		final Map<S, List<DroolsRuleModel>> deployedRules = module.getKieBases().stream().filter(b -> isNotEmpty(b.getRules()))
				.flatMap(b -> b.getRules().stream()).parallel()
				.filter(r -> getEngineRulesRepository().checkEngineRuleDeployedForModule(r,
						moduleName(r)))
				.collect(groupingBy(r -> (S) r.getSourceRule()));

		return deployedRules.entrySet().stream().parallel().filter(deployedRule -> isNotEmpty(deployedRule.getValue()))
				.collect(toConcurrentMap(Map.Entry::getKey, deployedRule -> deployedRule.getValue().iterator().next()));
	}

	protected <T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRules(final List<T> rules,
			final String moduleName, final boolean enableIncrementalUpdate, final boolean blocking)
	{
		return compileAndPublishRules(rules, moduleName, enableIncrementalUpdate, null, blocking);
	}

	protected <T extends SourceRuleModel> RuleCompilerPublisherResult compileAndPublishRules(final List<T> rules,
			final String moduleName, final boolean enableIncrementalUpdate, final CronJobProgressTracker cronJobProgressTracker,
			final boolean blocking)
	{
		validateParameterNotNull(rules, "Rules list cannot be null");

		Result result;
		final DroolsKIEModuleModel module = getRulesModuleByName(moduleName);
		final RuleCompilerTaskResult ruleCompilerTaskResult = compileRules(rules, moduleName);
		result = ruleCompilerTaskResult.getState().equals(TaskResult.State.SUCCESS) ? Result.SUCCESS : Result.COMPILER_ERROR;
		List<RuleEngineActionResult> publisherResults = null;
		if (!COMPILER_ERROR.equals(result))
		{
			updateCronJobTracker(cronJobProgressTracker, rules.size(), rules.size() / 2);

			final InitializationFuture initializationFuture = getRuleEngineService().initialize(singletonList(module), true,
					enableIncrementalUpdate, createExecutionContext(ruleCompilerTaskResult));

			if (blocking)
			{
				initializationFuture.waitForInitializationToFinish();
				updateCronJobTracker(cronJobProgressTracker, rules.size(), rules.size());
			}
			publisherResults = initializationFuture.getResults();
			result = publisherResults.stream().filter(RuleEngineActionResult::isActionFailed).findAny()
					.map(r -> Result.PUBLISHER_ERROR).orElse(result);
		}
		else
		{
			LOG.error("The rule compilation finished with errors");
			logCompilerErrors(ruleCompilerTaskResult);
			onCompileErrorCleanup(ruleCompilerTaskResult, moduleName);
			updateCronJobTracker(cronJobProgressTracker, rules.size(), rules.size());
		}
		return new DefaultRuleCompilerPublisherResult(result, ruleCompilerTaskResult.getRuleCompilerResults(), publisherResults);
	}

	protected void onCompileErrorCleanup(final RuleCompilerTaskResult result, final String moduleName)
	{
		final Set<AbstractRuleEngineRuleModel> engineRules = result.getRuleCompilerResults().stream()
				.filter(r -> RuleCompilerResult.Result.SUCCESS.equals(r.getResult()))
				.map(r -> getEngineRuleDao().getActiveRuleByCodeAndMaxVersion(r.getRuleCode(), moduleName, r.getRuleVersion()))
				.filter(Objects::nonNull)
				.collect(toSet());

		getModelService().removeAll(engineRules);
	}

	protected ExecutionContext createExecutionContext(final RuleCompilerTaskResult ruleCompilerTaskResult)
	{
		final ExecutionContext executionContext = new ExecutionContext();
		executionContext.setRuleVersions(ruleCompilerTaskResult.getRuleCompilerResults().stream()
				.collect(toMap(RuleCompilerResult::getRuleCode, RuleCompilerResult::getRuleVersion)));
		return executionContext;
	}

	protected void updateCronJobTracker(final CronJobProgressTracker tracker, final int itemsTotal, final int itemsProcessed)
	{
		if (Objects.nonNull(tracker))
		{
			if (itemsTotal > 0 && itemsProcessed >= 0)
			{
				tracker.setProgress(100 * (double) itemsProcessed / itemsTotal);
			}
			else if (itemsTotal == 0)
			{
				tracker.setProgress(100d);
			}
		}
	}

	protected <T extends SourceRuleModel> RuleCompilerTaskResult compileRules(final List<T> rules, final String moduleName)
	{
		validateParameterNotNull(rules, "Rules list cannot be null");

		final GuardStatus guardStatus = getRulesCompilationGuardedSuspension().checkPreconditions(moduleName);
		if (GuardStatus.Type.NO_GO.equals(guardStatus.getType()))
		{
			throw new IllegalStateException(
					"The compilation of rules is currently in progress for rules module [" + moduleName + "]");
		}

		RuleCompilationContext ruleCompilationContext = null;
		final RuleCompilerTaskResult ruleCompilerTaskResult;
		try
		{
			ruleCompilationContext = getRuleCompilationContextProvider().getRuleCompilationContext();
			ruleCompilationContext.registerCompilationListeners(moduleName);
			final RuleCompilerSpliterator<T> ruleCompilerSpliterator = DefaultRuleCompilerSpliterator
					.withCompilationContext(ruleCompilationContext);

			final RuleCompilerFuture ruleCompilerFuture = ruleCompilerSpliterator.compileRulesAsync(rules, moduleName);
			ruleCompilerTaskResult = (RuleCompilerTaskResult) ruleCompilerFuture.getTaskResult();
		}
		finally
		{
			if (nonNull(ruleCompilationContext))
			{
				ruleCompilationContext.cleanup(moduleName); // NOSONAR
			}
		}
		return ruleCompilerTaskResult;
	}

	protected DroolsKIEModuleModel getRulesModuleByName(final String moduleName)
	{
		try
		{
			final AbstractRulesModuleModel module = getRulesModuleDao().findByName(moduleName);
			if (!(module instanceof DroolsKIEModuleModel))
			{
				throw new RuleEngineServiceException("No DroolsKIEModuleModel instance was found for the name [" + moduleName + "]");
			}
			return (DroolsKIEModuleModel) module;
		}
		catch (final ModelNotFoundException e)
		{
			throw new RuleEngineServiceException("No RulesModuleModel was found for the name [" + moduleName + "]");
		}
	}

	protected void logCompilerErrors(final RuleCompilerTaskResult ruleCompilerTaskResult)
	{
		if (LOG.isDebugEnabled() && isNotEmpty(ruleCompilerTaskResult.getRuleCompilerResults()))
		{
			LOG.debug("rule compilation errors:");
			for (final RuleCompilerResult rcr : ruleCompilerTaskResult.getRuleCompilerResults())
			{
				if (isEmpty(rcr.getProblems()))
				{
					continue;
				}
				LOG.debug("rule[version]: result  -  {}[{}]: {}", rcr.getRuleCode(), rcr.getRuleVersion(), rcr.getResult());
				final List<RuleCompilerProblem> problems = rcr.getProblems().stream().filter(Objects::nonNull).collect(toList());
				for (final RuleCompilerProblem rcp : problems)
				{
					LOG.debug("              {} {}", rcp.getSeverity(), rcp.getMessage());
				}
			}
		}
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

	protected RuleEngineService getRuleEngineService()
	{
		return ruleEngineService;
	}

	@Required
	public void setRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.ruleEngineService = ruleEngineService;
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

	protected RuleService getRuleService()
	{
		return ruleService;
	}

	@Required
	public void setRuleService(final RuleService ruleService)
	{
		this.ruleService = ruleService;
	}

	protected RuleCompilationContextProvider getRuleCompilationContextProvider()
	{
		return ruleCompilationContextProvider;
	}

	@Required
	public void setRuleCompilationContextProvider(final RuleCompilationContextProvider ruleCompilationContextProvider)
	{
		this.ruleCompilationContextProvider = ruleCompilationContextProvider;
	}

	protected RulesModuleResolver getRulesModuleResolver()
	{
		return rulesModuleResolver;
	}

	@Required
	public void setRulesModuleResolver(final RulesModuleResolver rulesModuleResolver)
	{
		this.rulesModuleResolver = rulesModuleResolver;
	}

	protected GuardedSuspension<String> getRulesCompilationGuardedSuspension()
	{
		return rulesCompilationGuardedSuspension;
	}

	@Required
	public void setRulesCompilationGuardedSuspension(final GuardedSuspension<String> rulesCompilationGuardedSuspension)
	{
		this.rulesCompilationGuardedSuspension = rulesCompilationGuardedSuspension;
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

	protected EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
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
