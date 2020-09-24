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
package de.hybris.platform.ruleengineservices.converters.populator;

import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.DEFAULT_MAX_ALLOWED_RUNS;
import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.DEFAULT_RULEGROUP_CODE_PROPERTY;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * RuleConfigurationRrdPopulator is a populator for populating a {@link RuleConfigurationRRD} from a
 * {@code AbstractRuleEngineRuleModel}.
 */
public class RuleConfigurationRrdPopulator implements Populator<AbstractRuleEngineRuleModel, RuleConfigurationRRD>
{

	private ConfigurationService configurationService;

	@Override
	public void populate(final AbstractRuleEngineRuleModel source, final RuleConfigurationRRD target) 
	{
		target.setRuleCode(source.getCode());
		target.setCurrentRuns(Integer.valueOf(0));
		if (source.getMaxAllowedRuns() != null && source.getMaxAllowedRuns().intValue() != 0)
		{
			target.setMaxAllowedRuns(source.getMaxAllowedRuns());
		}
		else
		{
			target.setMaxAllowedRuns(Integer.valueOf(DEFAULT_MAX_ALLOWED_RUNS));
		}
		target.setRuleGroupCode(source.getRuleGroupCode());
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
