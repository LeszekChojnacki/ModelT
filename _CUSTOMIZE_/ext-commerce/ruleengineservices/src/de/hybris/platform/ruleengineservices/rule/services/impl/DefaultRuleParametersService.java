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
package de.hybris.platform.ruleengineservices.rule.services.impl;

import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleParametersService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterUuidGenerator;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParametersConverter;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link RuleParametersService}
 */
public class DefaultRuleParametersService implements RuleParametersService
{
	private RuleParametersConverter ruleParametersConverter;
	private RuleParameterUuidGenerator ruleParameterUuidGenerator;

	@Override
	public RuleParameterData createParameterFromDefinition(final RuleParameterDefinitionData definition)
	{
		final RuleParameterData parameter = new RuleParameterData();
		parameter.setUuid(ruleParameterUuidGenerator.generateUuid(parameter, definition));
		parameter.setType(definition.getType());
		parameter.setValue(definition.getDefaultValue());
		return parameter;
	}

	@Override
	public String convertParametersToString(final List<RuleParameterData> parameters)
	{
		return ruleParametersConverter.toString(parameters);
	}

	@Override
	public List<RuleParameterData> convertParametersFromString(final String parameters)
	{
		return ruleParametersConverter.fromString(parameters);
	}

	public RuleParametersConverter getRuleParametersConverter()
	{
		return ruleParametersConverter;
	}

	@Required
	public void setRuleParametersConverter(final RuleParametersConverter ruleParametersConverter)
	{
		this.ruleParametersConverter = ruleParametersConverter;
	}

	public RuleParameterUuidGenerator getRuleParameterUuidGenerator()
	{
		return ruleParameterUuidGenerator;
	}

	@Required
	public void setRuleParameterUuidGenerator(final RuleParameterUuidGenerator ruleParameterUuidGenerator)
	{
		this.ruleParameterUuidGenerator = ruleParameterUuidGenerator;
	}
}
