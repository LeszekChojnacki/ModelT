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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionDefinitionService;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.servicelayer.dto.converter.Converter;


public class DefaultRuleActionsRegistry implements RuleActionsRegistry
{
	private RuleActionDefinitionService ruleActionDefinitionService;
	private Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionConverter;

	@Override
	public List<RuleActionDefinitionData> getAllActionDefinitions()
	{
		return convertActionDefinitions(ruleActionDefinitionService.getAllRuleActionDefinitions());
	}

	@Override
	public Map<String, RuleActionDefinitionData> getAllActionDefinitionsAsMap()
	{
		final List<RuleActionDefinitionData> actionDefinitions = convertActionDefinitions(ruleActionDefinitionService
				.getAllRuleActionDefinitions());

		final Map<String, RuleActionDefinitionData> result = new HashMap<>();

		for (final RuleActionDefinitionData actionDefinition : actionDefinitions)
		{
			result.put(actionDefinition.getId(), actionDefinition);
		}

		return result;
	}

	@Override
	public List<RuleActionDefinitionData> getActionDefinitionsForRuleType(final Class<?> ruleType)
	{
		return convertActionDefinitions(ruleActionDefinitionService
				.getRuleActionDefinitionsForRuleType(ruleType));
	}

	@Override
	public Map<String, RuleActionDefinitionData> getActionDefinitionsForRuleTypeAsMap(final Class<?> ruleType)
	{
		final List<RuleActionDefinitionData> actionDefinitions = convertActionDefinitions(ruleActionDefinitionService
				.getRuleActionDefinitionsForRuleType(ruleType));

		final Map<String, RuleActionDefinitionData> result = new HashMap<>();

		for (final RuleActionDefinitionData actionDefinition : actionDefinitions)
		{
			result.put(actionDefinition.getId(), actionDefinition);
		}

		return result;
	}

	protected List<RuleActionDefinitionData> convertActionDefinitions(final List<RuleActionDefinitionModel> definitions)
	{
		final List<RuleActionDefinitionData> definitionsData = new ArrayList<RuleActionDefinitionData>();
		definitions.stream().forEach(model -> definitionsData.add(ruleActionDefinitionConverter.convert(model)));
		return definitionsData;
	}

	public RuleActionDefinitionService getRuleActionDefinitionService()
	{
		return ruleActionDefinitionService;
	}

	@Required
	public void setRuleActionDefinitionService(final RuleActionDefinitionService ruleActionDefinitionService)
	{
		this.ruleActionDefinitionService = ruleActionDefinitionService;
	}

	public Converter<RuleActionDefinitionModel, RuleActionDefinitionData> getRuleActionDefinitionConverter()
	{
		return ruleActionDefinitionConverter;
	}

	@Required
	public void setRuleActionDefinitionConverter(
			final Converter<RuleActionDefinitionModel, RuleActionDefinitionData> ruleActionDefinitionConverter)
	{
		this.ruleActionDefinitionConverter = ruleActionDefinitionConverter;
	}
}
