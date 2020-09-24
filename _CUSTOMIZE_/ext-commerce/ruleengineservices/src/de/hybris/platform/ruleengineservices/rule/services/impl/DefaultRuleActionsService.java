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

import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleParametersService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleActionBreadcrumbsBuilder;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleActionsConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link RuleActionsService}
 */
public class DefaultRuleActionsService implements RuleActionsService
{
	private RuleActionsConverter ruleActionsConverter;
	private RuleActionBreadcrumbsBuilder ruleActionBreadcrumbsBuilder;
	private RuleParametersService ruleParametersService;

	@Override
	public RuleActionData createActionFromDefinition(final RuleActionDefinitionData definition)
	{
		final Map<String, RuleParameterData> parameters = new HashMap<>();

		for (final Entry<String, RuleParameterDefinitionData> entry : definition.getParameters().entrySet())
		{
			final String parameterId = entry.getKey();
			final RuleParameterDefinitionData parameterDefinition = entry.getValue();
			final RuleParameterData parameter = ruleParametersService.createParameterFromDefinition(parameterDefinition);
			parameters.put(parameterId, parameter);
		}

		final RuleActionData action = new RuleActionData();
		action.setDefinitionId(definition.getId());
		action.setParameters(parameters);

		return action;
	}

	@Override
	public String buildActionBreadcrumbs(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return ruleActionBreadcrumbsBuilder.buildActionBreadcrumbs(actions, actionDefinitions);
	}

	@Override
	public String buildStyledActionBreadcrumbs(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return ruleActionBreadcrumbsBuilder.buildStyledActionBreadcrumbs(actions, actionDefinitions);
	}

	@Override
	public String convertActionsToString(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return ruleActionsConverter.toString(actions, actionDefinitions);
	}

	@Override
	public List<RuleActionData> convertActionsFromString(final String actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return ruleActionsConverter.fromString(actions, actionDefinitions);
	}

	public RuleActionsConverter getRuleActionsConverter()
	{
		return ruleActionsConverter;
	}

	@Required
	public void setRuleActionsConverter(final RuleActionsConverter ruleActionsConverter)
	{
		this.ruleActionsConverter = ruleActionsConverter;
	}

	public RuleActionBreadcrumbsBuilder getRuleActionBreadcrumbsBuilder()
	{
		return ruleActionBreadcrumbsBuilder;
	}

	@Required
	public void setRuleActionBreadcrumbsBuilder(final RuleActionBreadcrumbsBuilder ruleActionBreadcrumbsBuilder)
	{
		this.ruleActionBreadcrumbsBuilder = ruleActionBreadcrumbsBuilder;
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
