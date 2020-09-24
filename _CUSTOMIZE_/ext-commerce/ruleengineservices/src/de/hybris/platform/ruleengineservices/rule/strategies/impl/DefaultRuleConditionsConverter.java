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
package de.hybris.platform.ruleengineservices.rule.strategies.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConditionsConverter;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterUuidGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of {@link RuleConditionsConverter} that uses a JSON format for strings.
 */
public class DefaultRuleConditionsConverter extends AbstractRuleConverter implements RuleConditionsConverter
{
	private RuleConditionsRegistry ruleConditionsRegistry;
	private RuleParameterUuidGenerator ruleParameterUuidGenerator;

	@Override
	public String toString(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		try
		{
			return getObjectWriter().writeValueAsString(conditions);
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	@Override
	public List<RuleConditionData> fromString(final String conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		if (StringUtils.isBlank(conditions))
		{
			return Collections.emptyList();
		}

		if (MapUtils.isEmpty(conditionDefinitions))
		{
			return Collections.emptyList();
		}

		try
		{
			final ObjectReader objectReader = getObjectReader();
			final JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleConditionData.class);
			final List<RuleConditionData> parsedConditions = objectReader.forType(javaType).readValue(conditions);

			convertParameterValues(conditionDefinitions, parsedConditions);

			return parsedConditions;
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	protected void convertParameterValues(final Map<String, RuleConditionDefinitionData> conditionDefinitions,
			final List<RuleConditionData> conditions)
	{
		if (CollectionUtils.isEmpty(conditions))
		{
			return;
		}

		for (final RuleConditionData condition : conditions)
		{
			final RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(condition.getDefinitionId());
			if (conditionDefinition == null)
			{
				throw new RuleConverterException("No definition found for condition: [definitionId=" + condition.getDefinitionId()
						+ "]");
			}

			if (condition.getParameters() == null)
			{
				condition.setParameters(new HashMap<>());
			}

			// removes parameters that no longer exist
			if (MapUtils.isEmpty(conditionDefinition.getParameters()))
			{
				condition.getParameters().clear();
			}
			else
			{
				condition.getParameters().keySet().retainAll(conditionDefinition.getParameters().keySet());
				convertParameters(condition, conditionDefinition);
			}

			convertParameterValues(conditionDefinitions, condition.getChildren());
		}
	}

	protected RuleConditionsRegistry getRuleConditionsRegistry()
	{
		return ruleConditionsRegistry;
	}

	@Required
	public void setRuleConditionsRegistry(final RuleConditionsRegistry ruleConditionsRegistry)
	{
		this.ruleConditionsRegistry = ruleConditionsRegistry;
	}

	@Override
	protected RuleParameterUuidGenerator getRuleParameterUuidGenerator()
	{
		return ruleParameterUuidGenerator;
	}

	@Override
	@Required
	public void setRuleParameterUuidGenerator(final RuleParameterUuidGenerator ruleParameterUuidGenerator)
	{
		this.ruleParameterUuidGenerator = ruleParameterUuidGenerator;
	}
}
