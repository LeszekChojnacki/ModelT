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

import static java.lang.Runtime.getRuntime;

import de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.util.ThreadUtilities;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for the {@link RuleEngineSpliteratorStrategy}
 */
public class DefaultRuleEngineSpliteratorStrategy implements RuleEngineSpliteratorStrategy
{
	protected static final String FIXED_NO_OF_THREADS = "ruleengine.spliterator.threads.number";

	private ConfigurationService configurationService;

	@Override
	public int getNumberOfThreads()
	{
		final int fallback = getRuntime().availableProcessors() + 1;
		String expression = getConfigurationService().getConfiguration().getString(FIXED_NO_OF_THREADS);
		return ThreadUtilities.getNumberOfThreadsFromExpression(expression, fallback);
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
}
