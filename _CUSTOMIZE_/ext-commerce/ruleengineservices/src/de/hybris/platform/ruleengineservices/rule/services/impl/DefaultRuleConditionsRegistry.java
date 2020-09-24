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

import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionDefinitionService;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleConditionsRegistry implements RuleConditionsRegistry
{
	private RuleConditionDefinitionService ruleConditionDefinitionService;
	private Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionConverter;

	@Override
	public List<RuleConditionDefinitionData> getAllConditionDefinitions()
	{
		return convertConditionDefinitions(ruleConditionDefinitionService
				.getAllRuleConditionDefinitions());
	}

	@Override
	public Map<String, RuleConditionDefinitionData> getAllConditionDefinitionsAsMap()
	{
		final List<RuleConditionDefinitionData> conditionDefinitions = convertConditionDefinitions(ruleConditionDefinitionService
				.getAllRuleConditionDefinitions());

		final Map<String, RuleConditionDefinitionData> result = new HashMap<>();

		for (final RuleConditionDefinitionData conditionDefinition : conditionDefinitions)
		{
			result.put(conditionDefinition.getId(), conditionDefinition);
		}

		return result;
	}

	@Override
	public List<RuleConditionDefinitionData> getConditionDefinitionsForRuleType(final Class<?> ruleType)
	{
		return convertConditionDefinitions(ruleConditionDefinitionService
				.getRuleConditionDefinitionsForRuleType(ruleType));
	}

	@Override
	public Map<String, RuleConditionDefinitionData> getConditionDefinitionsForRuleTypeAsMap(final Class<?> ruleType)
	{
		final List<RuleConditionDefinitionData> conditionDefinitions = convertConditionDefinitions(ruleConditionDefinitionService
				.getRuleConditionDefinitionsForRuleType(ruleType));

		final Map<String, RuleConditionDefinitionData> result = new HashMap<>();

		for (final RuleConditionDefinitionData actionDefinition : conditionDefinitions)
		{
			result.put(actionDefinition.getId(), actionDefinition);
		}

		return result;
	}

	protected List<RuleConditionDefinitionData> convertConditionDefinitions(final List<RuleConditionDefinitionModel> definitions)
	{
		final List<RuleConditionDefinitionData> definitionsData = new ArrayList<RuleConditionDefinitionData>();
		definitions.stream().forEach(model -> definitionsData.add(ruleConditionDefinitionConverter.convert(model)));
		return definitionsData;
	}

	public RuleConditionDefinitionService getRuleConditionDefinitionService()
	{
		return ruleConditionDefinitionService;
	}

	@Required
	public void setRuleConditionDefinitionService(final RuleConditionDefinitionService ruleConditionDefinitionService)
	{
		this.ruleConditionDefinitionService = ruleConditionDefinitionService;
	}

	public Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> getRuleConditionDefinitionConverter()
	{
		return ruleConditionDefinitionConverter;
	}

	@Required
	public void setRuleConditionDefinitionConverter(
			final Converter<RuleConditionDefinitionModel, RuleConditionDefinitionData> ruleConditionDefinitionConverter)
	{
		this.ruleConditionDefinitionConverter = ruleConditionDefinitionConverter;
	}
}
