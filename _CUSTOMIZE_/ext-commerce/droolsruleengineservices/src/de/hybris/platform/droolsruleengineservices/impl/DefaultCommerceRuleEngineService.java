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
package de.hybris.platform.droolsruleengineservices.impl;

import static com.google.common.base.Preconditions.checkState;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.droolsruleengineservices.agendafilter.AgendaFilterFactory;
import de.hybris.platform.droolsruleengineservices.agendafilter.impl.DefaultRuleAndRuleGroupExecutionTracker;
import de.hybris.platform.droolsruleengineservices.eventlisteners.AgendaEventListenerFactory;
import de.hybris.platform.droolsruleengineservices.eventlisteners.ProcessEventListenerFactory;
import de.hybris.platform.droolsruleengineservices.eventlisteners.RuleRuntimeEventListenerFactory;
import de.hybris.platform.ruleengine.ExecutionContext;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleEvaluationResult;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.exception.DroolsRuleLoopException;
import de.hybris.platform.ruleengine.init.InitializationFuture;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rao.providers.FactContextFactory;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rao.providers.impl.FactContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.impl.RuleAndRuleGroupExecutionTracker;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.rule.AgendaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of Commerce Rule Engine Service.
 */
public class DefaultCommerceRuleEngineService implements RuleEngineService
{

	private RuleEngineService platformRuleEngineService;

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommerceRuleEngineService.class);

	private AgendaFilterFactory agendaFilterFactory;
	private AgendaEventListenerFactory agendaEventListenerFactory;
	private ProcessEventListenerFactory processEventListenerFactory;
	private RuleRuntimeEventListenerFactory ruleRuntimeEventListenerFactory;

	private ConfigurationService configurationService;
	private FactContextFactory factContextFactory;

	private RAOProvider ruleConfigurationProvider;
	private RAOProvider ruleGroupExecutionProvider;

	private EngineRuleDao engineRuleDao;

	private boolean useDeprecatedRRDsInEvaluations = true;

	@Override
	public void initialize(final AbstractRulesModuleModel module,
			final String deployedMvnVersion, final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate,
			final RuleEngineActionResult result)
	{
		getPlatformRuleEngineService()
				.initialize(module, deployedMvnVersion, propagateToOtherNodes, enableIncrementalUpdate, result);
	}

	@Override
	public void initialize(final AbstractRulesModuleModel module, final String deployedMvnVersion, final boolean propagateToOtherNodes,
			final boolean enableIncrementalUpdate, final RuleEngineActionResult result, final ExecutionContext executionContext)
	{
		getPlatformRuleEngineService()
				.initialize(module, deployedMvnVersion, propagateToOtherNodes, enableIncrementalUpdate, result, executionContext);
	}

	@Override
	public void initializeNonBlocking(final AbstractRulesModuleModel module,
			final String deployedReleaseIdVersion,
			final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate, final RuleEngineActionResult result)
	{
		getPlatformRuleEngineService()
				.initializeNonBlocking(module, deployedReleaseIdVersion, propagateToOtherNodes, enableIncrementalUpdate, result);
	}

	@Override
	public InitializationFuture initialize(final List<AbstractRulesModuleModel> modules,
			final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate)
	{
		return getPlatformRuleEngineService().initialize(modules, propagateToOtherNodes, enableIncrementalUpdate);
	}

	@Override
	public InitializationFuture initialize(final List<AbstractRulesModuleModel> modules,
			final boolean propagateToOtherNodes, final boolean enableIncrementalUpdate, final ExecutionContext executionContext)
	{
		return getPlatformRuleEngineService().initialize(modules, propagateToOtherNodes, enableIncrementalUpdate, executionContext);
	}

	@Override
	public RuleEvaluationResult evaluate(final RuleEvaluationContext context)
	{
		validateParameterNotNull(context, "context must not be null");
		final AbstractRuleEngineContextModel abstractREContext = context.getRuleEngineContext();
		validateParameterNotNull(abstractREContext, "context.ruleEngineContext must not be null");

		checkState(abstractREContext instanceof DroolsRuleEngineContextModel,
				"ruleEngineContext %s is not a DroolsRuleEngineContext. %s is not supported.", abstractREContext.getName(),
				abstractREContext.getItemtype());

		logContextFacts(context);

		final DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel) abstractREContext;

		try
		{

			final RuleEngineResultRAO rao = addRuleEngineResultRAO(context);

			if (!isUseDeprecatedRRDsInEvaluations())
			{
				addDroolsRuleExecutionTracker(context);
			}

			final AgendaFilter agendaFilter = getAgendaFilterFactory().createAgendaFilter(abstractREContext);
			context.setFilter(agendaFilter);

			final Set<Object> eventListeners = getEventListners(ruleEngineContext);
			context.setEventListeners(eventListeners);

			final RuleEvaluationResult result = getPlatformRuleEngineService().evaluate(context);
			result.setResult(rao);
			return result;
		}
		catch (final DroolsRuleLoopException ex)
		{
			LOGGER.error(ex.getMessage());
			throw ex;
		}
		catch (final Exception ex)
		{
			final String errorMessage = String.format("Rule evaluation failed with message '%s' for facts: %s.", ex.getMessage(),
					Arrays.toString(context.getFacts() != null ? context.getFacts().toArray() : null));
			LOGGER.error(errorMessage, ex);

			final RuleEvaluationResult result = new RuleEvaluationResult();
			result.setEvaluationFailed(true);
			result.setErrorMessage(errorMessage);
			result.setFacts(context.getFacts());
			return result;
		}
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> void deactivateRulesModuleEngineRules(final String moduleName,
			final Collection<T> engineRules)
	{
		getPlatformRuleEngineService().deactivateRulesModuleEngineRules(moduleName, engineRules);
	}

	protected void logContextFacts(final RuleEvaluationContext context)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Rule evaluation triggered with the facts: {}",
					context.getFacts() == null ? "[]" : context.getFacts().toString());
		}
	}

	protected Set<Object> getEventListners(final DroolsRuleEngineContextModel ruleEngineContext)
	{
		final Set<Object> eventListeners = new LinkedHashSet<>();

		final Set<AgendaEventListener> listeners = getAgendaEventListenerFactory().createAgendaEventListeners(ruleEngineContext);
		if (CollectionUtils.isNotEmpty(listeners))
		{
			eventListeners.addAll(listeners);
		}

		if (getRuleRuntimeEventListenerFactory() != null)
		{
			final Set<RuleRuntimeEventListener> ruleRuntimeEventlisteners = getRuleRuntimeEventListenerFactory()
					.createRuleRuntimeEventListeners(ruleEngineContext);
			if (CollectionUtils.isNotEmpty(ruleRuntimeEventlisteners))
			{
				eventListeners.addAll(ruleRuntimeEventlisteners);
			}
		}

		if (getProcessEventListenerFactory() != null)
		{
			final Set<ProcessEventListener> processEventListeners = getProcessEventListenerFactory()
					.createProcessEventListeners(ruleEngineContext);
			if (CollectionUtils.isNotEmpty(processEventListeners))
			{
				eventListeners.addAll(processEventListeners);
			}
		}

		if (LOGGER.isDebugEnabled())
		{
			eventListeners.add(new DebugRuleRuntimeEventListener());
			eventListeners.add(new DebugAgendaEventListener());
		}

		return eventListeners;
	}

	/**
	 * Adds a RuleEngineResultRAO to the facts of this context if it doesn't exist already.
	 *
	 * @param context
	 * 		the rule engine context
	 * @return the result rao (already added to the context.facts)
	 */
	protected RuleEngineResultRAO addRuleEngineResultRAO(final RuleEvaluationContext context)
	{
		// result rao is created and added to the facts
		// (unless there is one already)
		RuleEngineResultRAO rao = null;

		if (context.getFacts() == null)
		{
			final LinkedHashSet<Object> facts = new LinkedHashSet<Object>();
			context.setFacts(facts);
		}
		for (final Object fact : context.getFacts())
		{
			if (fact instanceof RuleEngineResultRAO)
			{
				rao = (RuleEngineResultRAO) fact;
			}
		}

		if (rao == null)
		{
			rao = new RuleEngineResultRAO();
			rao.setActions(new LinkedHashSet<>());
			final Set<Object> facts = new LinkedHashSet<Object>(context.getFacts());
			facts.add(rao);
			context.setFacts(facts);
		}
		return rao;
	}

	protected RuleAndRuleGroupExecutionTracker addDroolsRuleExecutionTracker(final RuleEvaluationContext context)
	{
		final DefaultRuleAndRuleGroupExecutionTracker tracker = new DefaultRuleAndRuleGroupExecutionTracker();
		context.getFacts().add(tracker);
		return tracker;
	}

	protected Set<Object> provideRAOs(final FactContext factContext)
	{
		final Set<Object> result = new HashSet<Object>();
		for (final Object modelFact : factContext.getFacts())
		{
			for (final RAOProvider raoProvider : factContext.getProviders(modelFact))
			{
				result.addAll(raoProvider.expandFactModel(modelFact));
			}
		}
		return result;
	}

	@Override
	public List<RuleEngineActionResult> initializeAllRulesModules()
	{
		return getPlatformRuleEngineService().initializeAllRulesModules();
	}

	@Override
	public List<RuleEngineActionResult> initializeAllRulesModules(final boolean propagateToOtherNodes)
	{
		return getPlatformRuleEngineService().initializeAllRulesModules(propagateToOtherNodes);
	}

	@Override
	public RuleEngineActionResult updateEngineRule(final AbstractRuleEngineRuleModel ruleEngineRule,
			final AbstractRulesModuleModel rulesModule)
	{
		return getPlatformRuleEngineService().updateEngineRule(ruleEngineRule, rulesModule);
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleEngineActionResult archiveRule(final AbstractRuleEngineRuleModel ruleEngineRule)
	{
		return getPlatformRuleEngineService().archiveRule(ruleEngineRule);
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public RuleEngineActionResult archiveRule(final AbstractRuleEngineRuleModel ruleEngineRule,
			final AbstractRulesModuleModel rulesModule)
	{
		return getPlatformRuleEngineService().archiveRule(ruleEngineRule, rulesModule);
	}

	@Override
	public <T extends DroolsRuleModel> Optional<InitializationFuture> archiveRules(final Collection<T> engineRules)
	{
		return getPlatformRuleEngineService().archiveRules(engineRules);
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleForCodeAndModule(final String code, final String moduleName)
	{
		return getPlatformRuleEngineService().getRuleForCodeAndModule(code, moduleName);
	}

	@Override
	public AbstractRuleEngineRuleModel getRuleForUuid(final String uuid)
	{
		return getPlatformRuleEngineService().getRuleForUuid(uuid);
	}

	protected AgendaFilterFactory getAgendaFilterFactory()
	{
		return agendaFilterFactory;
	}

	public void setAgendaFilterFactory(final AgendaFilterFactory agendaFilterFactory)
	{
		this.agendaFilterFactory = agendaFilterFactory;
	}

	protected AgendaEventListenerFactory getAgendaEventListenerFactory()
	{
		return agendaEventListenerFactory;
	}

	@Required
	public void setAgendaEventListenerFactory(final AgendaEventListenerFactory agendaEventListenerFactory)
	{
		this.agendaEventListenerFactory = agendaEventListenerFactory;
	}

	protected ProcessEventListenerFactory getProcessEventListenerFactory()
	{
		return processEventListenerFactory;
	}

	public void setProcessEventListenerFactory(final ProcessEventListenerFactory processEventListenerFactory)
	{
		this.processEventListenerFactory = processEventListenerFactory;
	}

	protected RuleRuntimeEventListenerFactory getRuleRuntimeEventListenerFactory()
	{
		return ruleRuntimeEventListenerFactory;
	}

	public void setRuleRuntimeEventListenerFactory(final RuleRuntimeEventListenerFactory ruleRuntimeEventListenerFactory)
	{
		this.ruleRuntimeEventListenerFactory = ruleRuntimeEventListenerFactory;
	}

	protected RAOProvider getRuleConfigurationProvider()
	{
		return ruleConfigurationProvider;
	}

	public void setRuleConfigurationProvider(final RAOProvider ruleConfigurationProvider)
	{
		this.ruleConfigurationProvider = ruleConfigurationProvider;
	}

	protected RAOProvider getRuleGroupExecutionProvider()
	{
		return ruleGroupExecutionProvider;
	}

	public void setRuleGroupExecutionProvider(final RAOProvider ruleGroupExecutionProvider)
	{
		this.ruleGroupExecutionProvider = ruleGroupExecutionProvider;
	}

	protected RuleEngineService getPlatformRuleEngineService()
	{
		return platformRuleEngineService;
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

	@Required
	public void setPlatformRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.platformRuleEngineService = ruleEngineService;
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

	protected FactContextFactory getFactContextFactory()
	{
		return factContextFactory;
	}

	@Required
	public void setFactContextFactory(final FactContextFactory factContextFactory)
	{
		this.factContextFactory = factContextFactory;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	public void setUseDeprecatedRRDsInEvaluations(final boolean useDeprecatedRRDsInEvaluations)
	{
		this.useDeprecatedRRDsInEvaluations = useDeprecatedRRDsInEvaluations;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	protected boolean isUseDeprecatedRRDsInEvaluations()
	{
		return useDeprecatedRRDsInEvaluations;
	}

}
