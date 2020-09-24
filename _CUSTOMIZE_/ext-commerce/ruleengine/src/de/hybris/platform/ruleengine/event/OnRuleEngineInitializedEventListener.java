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
package de.hybris.platform.ruleengine.event;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULE_ENGINE_ACTIVE;
import static de.hybris.platform.ruleengine.InitializeMode.RESTORE;

import de.hybris.platform.ruleengine.ExecutionContext;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Event listener to handle {@link RuleEngineInitializedEvent}
 */
public class OnRuleEngineInitializedEventListener extends AbstractEventListener<RuleEngineInitializedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(OnRuleEngineInitializedEventListener.class);

	private RuleEngineService platformRuleEngineService;
	private RulesModuleDao rulesModuleDao;
	private ConfigurationService configurationService;

	@Override
	protected void onEvent(final RuleEngineInitializedEvent ruleEngineInitializedEvent)
	{

		if (getConfigurationService().getConfiguration().getBoolean(RULE_ENGINE_ACTIVE, true))
		{
			final String rulesModuleName = ruleEngineInitializedEvent.getRulesModuleName();
			LOG.info(
					"RuleEngineInitializedEvent received. Beginning initialization of the rule engine for being restored module [{}]",
					rulesModuleName);
			final AbstractRulesModuleModel rulesModel = getRulesModuleDao().findByName(rulesModuleName);
			final RuleEngineActionResult result = new RuleEngineActionResult();
			final ExecutionContext executionContext = new ExecutionContext();
			executionContext.setInitializeMode(RESTORE);
			getPlatformRuleEngineService().initialize(rulesModel, ruleEngineInitializedEvent.getDeployedReleaseIdVersion(), false,
					false, result, executionContext);
		}
	}

	protected RuleEngineService getPlatformRuleEngineService()
	{
		return platformRuleEngineService;
	}

	@Required
	public void setPlatformRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.platformRuleEngineService = ruleEngineService;
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
