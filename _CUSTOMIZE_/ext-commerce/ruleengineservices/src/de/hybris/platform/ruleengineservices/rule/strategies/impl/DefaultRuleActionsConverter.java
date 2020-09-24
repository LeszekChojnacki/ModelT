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
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleActionsConverter;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
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
 * Implementation of {@link RuleActionsConverter} that uses a JSON format for strings.
 */
public class DefaultRuleActionsConverter extends AbstractRuleConverter implements RuleActionsConverter
{
	private RuleActionsRegistry ruleActionsRegistry;

	@Override
	public String toString(final List<RuleActionData> actions, final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		try
		{
			return getObjectWriter().writeValueAsString(actions);
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	@Override
	public List<RuleActionData> fromString(final String actions, final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		if (StringUtils.isBlank(actions))
		{
			return Collections.emptyList();
		}

		if (MapUtils.isEmpty(actionDefinitions))
		{
			return Collections.emptyList();
		}

		try
		{
			final ObjectReader objectReader = getObjectReader();
			final JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleActionData.class);
			final List<RuleActionData> parsedActions = objectReader.forType(javaType).readValue(actions);

			convertParameterValues(parsedActions, actionDefinitions);

			return parsedActions;
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	protected void convertParameterValues(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		if (CollectionUtils.isEmpty(actions))
		{
			return;
		}

		for (final RuleActionData action : actions)
		{
			final RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());
			if (actionDefinition == null)
			{
				throw new RuleConverterException("No definition found for action: [definitionId=" + action.getDefinitionId() + "]");
			}

			if (action.getParameters() == null)
			{
				action.setParameters(new HashMap<>());
			}

			// removes parameters that no longer exist
			if (MapUtils.isEmpty(actionDefinition.getParameters()))
			{
				action.getParameters().clear();
			}
			else
			{
				action.getParameters().keySet().retainAll(actionDefinition.getParameters().keySet());
				convertParameters(action, actionDefinition);
			}
		}
	}

	public RuleActionsRegistry getRuleActionsRegistry()
	{
		return ruleActionsRegistry;
	}

	@Required
	public void setRuleActionsRegistry(final RuleActionsRegistry ruleActionsRegistry)
	{
		this.ruleActionsRegistry = ruleActionsRegistry;
	}
}
