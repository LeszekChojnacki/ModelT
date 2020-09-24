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

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.maintenance.RuleMaintenanceService;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * Re-Initialize all active rule modules.
 */
public class ReInitializeRuleModulesTask implements MigrationTask
{
	private static final Logger LOG = LoggerFactory.getLogger(ReInitializeRuleModulesTask.class);
	private RuleEngineService ruleEngineService;
	private RuleService ruleService;
	private RulesModuleDao rulesModuleDao;
	private ConfigurationService configurationService;
	private RuleMaintenanceService ruleMaintenanceService;
	private RuleDao ruleDao;

	@Override
	public void execute(@SuppressWarnings("unused") final SystemSetupContext systemSetupContext)
	{
		LOG.info("Task - Re-initializing active rule module");
		@SuppressWarnings({"deprecation","findByVersionAndStatuses"})
		final List<AbstractRuleModel> rulesToPublish = getRuleDao()
				.findByVersionAndStatuses(RuleEngineServicesConstants.DEFAULT_RULE_VERSION, RuleStatus.INACTIVE);
		final List<SourceRuleModel> sourceRulesToPublish = rulesToPublish.stream().filter(SourceRuleModel.class::isInstance)
				.map(SourceRuleModel.class::cast).collect(toList());
		final Map<RuleType, List<SourceRuleModel>> sourceRulesToPublishByType = sourceRulesToPublish.stream().collect(
				Collectors.groupingBy(rule ->
						getRuleService().getEngineRuleTypeForRuleType(rule.getClass())));

		for (final Map.Entry<RuleType, List<SourceRuleModel>> ruleToTypeEntry : sourceRulesToPublishByType.entrySet())
		{
			final List<AbstractRulesModuleModel> activeModules = getRulesModuleDao().findActiveRulesModulesByRuleType(
					ruleToTypeEntry.getKey());
			if (isNotEmpty(activeModules))
			{
				//prior 6.4 there was at most one rule module per type so we can safely assume that we get only one item in the collection during migration
				final AbstractRulesModuleModel liveModule = activeModules.iterator().next();
				getRuleMaintenanceService().compileAndPublishRulesWithBlocking(
						sourceRulesToPublishByType.get(ruleToTypeEntry.getKey()),
						liveModule.getName(), true);
			}
		}
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


	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected RuleMaintenanceService getRuleMaintenanceService()
	{
		return ruleMaintenanceService;
	}

	@Required
	public void setRuleMaintenanceService(final RuleMaintenanceService ruleMaintenanceService)
	{
		this.ruleMaintenanceService = ruleMaintenanceService;
	}

	protected RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
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
}
