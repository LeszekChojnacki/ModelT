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

import static de.hybris.platform.ruleengineservices.constants.RuleEngineServicesConstants.DEFAULT_RULEGROUP_CODE_PROPERTY;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.model.RuleGroupModel;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;
import de.hybris.platform.ruleengineservices.rule.dao.RuleGroupDao;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * RuleGroupExecutionRrdPopulator is a populator for populating a {@link RuleGroupExecutionRRD} from a
 * {@code AbstractRuleEngineRuleModel}.
 */
public class RuleGroupExecutionRrdPopulator implements Populator<AbstractRuleEngineRuleModel, RuleGroupExecutionRRD>
{

	private ConfigurationService configurationService;
	private RuleGroupDao ruleGroupDao;

	@Override
	public void populate(final AbstractRuleEngineRuleModel source, final RuleGroupExecutionRRD target)
	{
		final String ruleGroupCode = source.getRuleGroupCode();
		target.setCode(ruleGroupCode);

		final Optional<RuleGroupModel> ruleGroup = getRuleGroupDao().findRuleGroupByCode(target.getCode());
		if (ruleGroup.isPresent())
		{
			target.setExclusive(ruleGroup.get().isExclusive());
		}
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

	protected RuleGroupDao getRuleGroupDao()
	{
		return ruleGroupDao;
	}

	@Required
	public void setRuleGroupDao(final RuleGroupDao ruleGroupDao)
	{
		this.ruleGroupDao = ruleGroupDao;
	}

}
