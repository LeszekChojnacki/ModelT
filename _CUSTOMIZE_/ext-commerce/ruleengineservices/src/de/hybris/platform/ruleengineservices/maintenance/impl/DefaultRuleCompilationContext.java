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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy;
import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.init.ConcurrentMapFactory;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerService;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.maintenance.RulesCompilationInProgressQueryEvent;
import de.hybris.platform.ruleengineservices.maintenance.RulesCompilationInProgressResponseEvent;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;


/**
 * Default implementation of the {@link RuleCompilationContext} interface
 */
public class DefaultRuleCompilationContext implements RuleCompilationContext
{
	public static final String WORKER_PRE_DESTROY_TIMEOUT = "ruleengineservices.compiler.task.predestroytimeout"; // NOSONAR
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleCompilationContext.class);

	private Tenant currentTenant;
	private ThreadFactory threadFactory;
	private RuleCompilerService ruleCompilerService;
	private ConfigurationService configurationService;
	private RuleEngineSpliteratorStrategy ruleEngineSpliteratorStrategy;
	private RulesModuleDao rulesModuleDao;
	private EventService eventService;
	private ConcurrentMapFactory concurrentMapFactory;
	private SuspendResumeTaskManager suspendResumeTaskManager;

	private Map<String, AtomicLong> ruleVersionForModules;
	private Map<String, RulesCompilationInProgressApplicationListener> listeners;

	@Override
	public Tenant getCurrentTenant()
	{
		return currentTenant;
	}

	@Override
	public ThreadFactory getThreadFactory()
	{
		return threadFactory;
	}

	@Override
	public RuleCompilerService getRuleCompilerService()
	{
		return ruleCompilerService;
	}

	@Override
	public int getNumberOfThreads()
	{
		return getRuleEngineSpliteratorStrategy().getNumberOfThreads();
	}

	@Override
	public Long getThreadTimeout()
	{
		return getConfigurationService().getConfiguration().getLong(WORKER_PRE_DESTROY_TIMEOUT, 3600000L);
	}

	@Override
	public AtomicLong resetRuleEngineRuleVersion(final String moduleName)
	{
		final AbstractRulesModuleModel moduleModel = getRulesModuleDao().findByName(moduleName);
		LOGGER.debug("Resetting the module version to [{}]", moduleModel.getVersion());
		final AtomicLong initVal = new AtomicLong(moduleModel.getVersion() + 1);
		if (nonNull(ruleVersionForModules.putIfAbsent(moduleName, initVal)))
		{
			ruleVersionForModules.replace(moduleName, initVal);
		}
		synchronized (this)
		{
			final AtomicLong moduleVersion = ruleVersionForModules.get(moduleName);
			moduleVersion.set(moduleModel.getVersion() + 1);
			return moduleVersion;
		}
	}

	@Override
	public Long getNextRuleEngineRuleVersion(final String moduleName)
	{
		LOGGER.debug("Getting next rule version for module [{}]", moduleName);
		AtomicLong moduleVersion = ruleVersionForModules.get(moduleName);
		if (isNull(moduleVersion))
		{
			moduleVersion = resetRuleEngineRuleVersion(moduleName);
		}
		return moduleVersion.getAndAdd(1);
	}

	@Override
	public synchronized void cleanup(final String moduleName)
	{
		checkArgument(nonNull(moduleName), "Module name should be specified");
		synchronized (this)
		{
			final RulesCompilationInProgressApplicationListener listener = listeners
						 .get(moduleName);
			if (nonNull(listener))
			{
				listeners.remove(moduleName);
				getEventService().unregisterEventListener(listener);
			}
		}
	}

	@Override
	public synchronized void registerCompilationListeners(final String moduleName)
	{
		checkArgument(nonNull(moduleName), "Module name should be specified");
		synchronized (this)
		{
			RulesCompilationInProgressApplicationListener listener = listeners
						 .get(moduleName);
			if (isNull(listener))
			{
				listener = new RulesCompilationInProgressApplicationListener(moduleName);
				getEventService().registerEventListener(listener);
				listeners.put(moduleName, listener);
			}
		}
	}

	@PostConstruct
	public void setUp()
	{
		ruleVersionForModules = getConcurrentMapFactory().createNew();
		listeners = getConcurrentMapFactory().createNew();
	}

	private class RulesCompilationInProgressApplicationListener
				 implements ApplicationListener<RulesCompilationInProgressQueryEvent>
	{

		private final String moduleName;

		public RulesCompilationInProgressApplicationListener(final String moduleName)
		{
			this.moduleName = moduleName;
		}

		@Override
		public void onApplicationEvent(final RulesCompilationInProgressQueryEvent queryEvent)
		{
			LOGGER.debug("Query event {} fired", queryEvent);
			if (moduleName.equals(queryEvent.getModuleName()))
			{
				getEventService().publishEvent(new RulesCompilationInProgressResponseEvent(moduleName));
			}
		}
	}

	@Required
	public void setCurrentTenant(final Tenant currentTenant)
	{
		this.currentTenant = currentTenant;
	}

	@Required
	public void setThreadFactory(final ThreadFactory threadFactory)
	{
		this.threadFactory = threadFactory;
	}

	@Required
	public void setRuleCompilerService(final RuleCompilerService ruleCompilerService)
	{
		this.ruleCompilerService = ruleCompilerService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	protected RuleEngineSpliteratorStrategy getRuleEngineSpliteratorStrategy()
	{
		return ruleEngineSpliteratorStrategy;
	}

	@Required
	public void setRuleEngineSpliteratorStrategy(
				 final RuleEngineSpliteratorStrategy ruleEngineSpliteratorStrategy)
	{
		this.ruleEngineSpliteratorStrategy = ruleEngineSpliteratorStrategy;
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

	protected EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
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

	@Override
	public SuspendResumeTaskManager getSuspendResumeTaskManager()
	{
		return suspendResumeTaskManager;
	}

	@Required
	public void setSuspendResumeTaskManager(final SuspendResumeTaskManager suspendResumeTaskManager)
	{
		this.suspendResumeTaskManager = suspendResumeTaskManager;
	}
}
