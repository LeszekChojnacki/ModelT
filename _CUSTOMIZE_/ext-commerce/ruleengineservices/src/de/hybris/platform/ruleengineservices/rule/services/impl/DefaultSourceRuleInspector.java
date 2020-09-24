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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.SourceRuleInspector;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleActionsConverter;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConditionsConverter;


/**
 * Default implementation of {@link SourceRuleInspector}
 */
public class DefaultSourceRuleInspector implements SourceRuleInspector
{
	private RuleConditionsConverter ruleConditionsConverter;
	private RuleActionsConverter ruleActionsConverter;
	private RuleConditionsRegistry ruleConditionsRegistry;
	private RuleActionsRegistry ruleActionsRegistry;


	@Override
	public boolean hasRuleCondition(final SourceRuleModel sourceRule, final String conditionDefinitionId)
	{
		checkArgument(nonNull(sourceRule), "sourceRule cannot be null");
		checkArgument(nonNull(conditionDefinitionId), "conditionDefinitionId cannot be null");

		final Map<String, RuleConditionDefinitionData> ruleConditionDefinitions = getRuleConditionsRegistry()
					 .getConditionDefinitionsForRuleTypeAsMap(sourceRule.getClass());
		final List<RuleConditionData> ruleConditionDatas = getRuleConditionsConverter().fromString(sourceRule.getConditions(),
				ruleConditionDefinitions);
		return collectAll(ruleConditionDatas).anyMatch(c -> conditionDefinitionId.equals(c.getDefinitionId()));
	}

	protected Stream<RuleConditionData> collectAll(final List<RuleConditionData> ruleConditionDatas)
	{
		if( isEmpty(ruleConditionDatas))
		{
			return Stream.empty();
		}
		else
		{
			final RuleConditionData head = head(ruleConditionDatas);
			if( ruleConditionDatas.size() == 1)
			{
				return concat( of(head), collectAll(head.getChildren()));
			}
			else
			{
				final List<RuleConditionData> tail = tail(ruleConditionDatas);
				return concat( collectAll(tail),collectAll(newArrayList(head)));
			}
		}
	}

	protected RuleConditionData head(final List<RuleConditionData> list)
	{
		return list.get(0);
	}

	protected List<RuleConditionData> tail(final List<RuleConditionData> list)
	{
		return list.subList(1, list.size());
	}

	@Override
	public boolean hasRuleAction(final SourceRuleModel sourceRule, final String actionDefinitionId)
	{
		checkArgument(nonNull(sourceRule), "sourceRule cannot be null");
		checkArgument(nonNull(actionDefinitionId), "actionDefinitionId cannot be null");

		final Map<String, RuleActionDefinitionData> actionDefinitionsForRuleType = getRuleActionsRegistry()
					 .getActionDefinitionsForRuleTypeAsMap(sourceRule.getClass());
		final List<RuleActionData> ruleActionDatas = getRuleActionsConverter().fromString(sourceRule.getActions(),
				actionDefinitionsForRuleType);
		return ruleActionDatas.stream().anyMatch(c -> actionDefinitionId.equals(c.getDefinitionId()));
	}

	protected RuleConditionsConverter getRuleConditionsConverter()
	{
		return ruleConditionsConverter;
	}

	@Required
	public void setRuleConditionsConverter(final RuleConditionsConverter ruleConditionsConverter)
	{
		this.ruleConditionsConverter = ruleConditionsConverter;
	}

	protected RuleActionsConverter getRuleActionsConverter()
	{
		return ruleActionsConverter;
	}

	@Required
	public void setRuleActionsConverter(final RuleActionsConverter ruleActionsConverter)
	{
		this.ruleActionsConverter = ruleActionsConverter;
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

	protected RuleActionsRegistry getRuleActionsRegistry()
	{
		return ruleActionsRegistry;
	}
	@Required
	public void setRuleActionsRegistry(final RuleActionsRegistry ruleActionsRegistry)
	{
		this.ruleActionsRegistry = ruleActionsRegistry;
	}
}
