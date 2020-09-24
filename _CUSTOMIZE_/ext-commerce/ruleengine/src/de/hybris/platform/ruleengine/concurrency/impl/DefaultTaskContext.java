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
package de.hybris.platform.ruleengine.concurrency.impl;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy;
import de.hybris.platform.ruleengine.concurrency.TaskContext;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.init.ConcurrentMapFactory;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the {@link de.hybris.platform.ruleengine.concurrency.TaskContext} interface
 */
public class DefaultTaskContext implements TaskContext
{

	private static final String WORKER_PRE_DESTROY_TIMEOUT = "ruleengine.task.predestroytimeout";     // NOSONAR

	private Tenant currentTenant;
	private ThreadFactory threadFactory;
	private ConfigurationService configurationService;
	private RulesModuleDao rulesModuleDao;
	private ConcurrentMapFactory concurrentMapFactory;
	private RuleEngineSpliteratorStrategy ruleEngineSpliteratorStrategy;

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
	public int getNumberOfThreads()
	{
		return getRuleEngineSpliteratorStrategy().getNumberOfThreads();
	}

	@Override
	public Long getThreadTimeout()
	{
		return getConfigurationService().getConfiguration()
					 .getLong(WORKER_PRE_DESTROY_TIMEOUT, 3600000L);
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
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
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
}
