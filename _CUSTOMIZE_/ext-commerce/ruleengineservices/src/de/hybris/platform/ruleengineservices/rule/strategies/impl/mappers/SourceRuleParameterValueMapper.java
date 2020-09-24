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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;


/**
 * Performs mapping between AbstractRuleModel and a String representation of it instance.
 */
public class SourceRuleParameterValueMapper implements RuleParameterValueMapper<AbstractRuleModel>
{
	private RuleService ruleService;

	@Override
	public AbstractRuleModel fromString(final String code)
	{
		ServicesUtil.validateParameterNotNull(code, "String value cannot be null");
		try
		{
			return getRuleService().getRuleForCode(code);
		}
		catch (final ModelNotFoundException ex)
		{
			throw new RuleParameterValueMapperException("Cannot find rule with the code: " + code);
		}
	}

	@Override
	public String toString(final AbstractRuleModel sourceRule)
	{
		ServicesUtil.validateParameterNotNull(sourceRule, "Object cannot be null");
		return sourceRule.getCode();
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
