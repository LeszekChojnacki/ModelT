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

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleParametersService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConditionBreadcrumbsBuilder;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConditionsConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link RuleConditionsService}
 */
public class DefaultRuleConditionsService implements RuleConditionsService
{
	private RuleConditionsConverter ruleConditionsConverter;
	private RuleConditionBreadcrumbsBuilder ruleConditionBreadcrumbsBuilder;
	private RuleParametersService ruleParametersService;

	@Override
	public RuleConditionData createConditionFromDefinition(final RuleConditionDefinitionData definition)
	{
		final Map<String, RuleParameterData> parameters = new HashMap<>();

		for (final Entry<String, RuleParameterDefinitionData> entry : definition.getParameters().entrySet())
		{
			final String parameterId = entry.getKey();
			final RuleParameterDefinitionData parameterDefinition = entry.getValue();
			final RuleParameterData parameter = ruleParametersService.createParameterFromDefinition(parameterDefinition);

			parameters.put(parameterId, parameter);
		}

		final RuleConditionData condition = new RuleConditionData();
		condition.setDefinitionId(definition.getId());
		condition.setParameters(parameters);
		condition.setChildren(new ArrayList<>());

		return condition;
	}

	@Override
	public String buildConditionBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return ruleConditionBreadcrumbsBuilder.buildConditionBreadcrumbs(conditions, conditionDefinitions);
	}

	@Override
	public String buildStyledConditionBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return ruleConditionBreadcrumbsBuilder.buildStyledConditionBreadcrumbs(conditions, conditionDefinitions);
	}

	@Override
	public String convertConditionsToString(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return ruleConditionsConverter.toString(conditions, conditionDefinitions);
	}

	@Override
	public List<RuleConditionData> convertConditionsFromString(final String conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return ruleConditionsConverter.fromString(conditions, conditionDefinitions);
	}

	public RuleConditionsConverter getRuleConditionsConverter()
	{
		return ruleConditionsConverter;
	}

	@Required
	public void setRuleConditionsConverter(final RuleConditionsConverter ruleConditionsConverter)
	{
		this.ruleConditionsConverter = ruleConditionsConverter;
	}

	public RuleConditionBreadcrumbsBuilder getRuleConditionBreadcrumbsBuilder()
	{
		return ruleConditionBreadcrumbsBuilder;
	}

	@Required
	public void setRuleConditionBreadcrumbsBuilder(final RuleConditionBreadcrumbsBuilder ruleConditionBreadcrumbsBuilder)
	{
		this.ruleConditionBreadcrumbsBuilder = ruleConditionBreadcrumbsBuilder;
	}

	public RuleParametersService getRuleParametersService()
	{
		return ruleParametersService;
	}

	@Required
	public void setRuleParametersService(final RuleParametersService ruleParametersService)
	{
		this.ruleParametersService = ruleParametersService;
	}
}
