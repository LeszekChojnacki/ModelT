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
package de.hybris.platform.ruleengineservices.setup.tasks.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.config.ConfigurationService;


/**
 * Activates rule engine
 */
public class ActivateRuleEngineTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(ActivateRuleEngineTask.class);
	private ConfigurationService configurationService;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Activate rule engine");
		getConfigurationService().getConfiguration().setProperty(RuleEngineConstants.RULE_ENGINE_ACTIVE, "true");
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
