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
package de.hybris.platform.ruleengineservices.rule.services;

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;


public interface RuleConditionsRegistry
{
	/**
	 * Returns all {@link RuleConditionDefinitionData} objects.
	 *
	 * @return all {@link RuleConditionDefinitionData}s.
	 */
	List<RuleConditionDefinitionData> getAllConditionDefinitions();

	/**
	 * Returns all {@link RuleConditionDefinitionData} objects.
	 *
	 * @return all {@link RuleConditionDefinitionData}s.
	 */
	Map<String, RuleConditionDefinitionData> getAllConditionDefinitionsAsMap();

	/**
	 * Returns {@link RuleConditionDefinitionData} objects for given rule type.
	 *
	 * @param ruleType
	 *           - type of rule (ie. PromotionSourceRule)
	 *
	 * @return list of {@link RuleConditionDefinitionData}s for given rule type.
	 */
	List<RuleConditionDefinitionData> getConditionDefinitionsForRuleType(Class<?> ruleType);

	/**
	 * Returns {@link RuleConditionDefinitionData} objects for given rule type.
	 *
	 * @param ruleType
	 *           - type of rule (ie. PromotionSourceRule)
	 *
	 * @return Map of rule action definition ids and {@link RuleConditionDefinitionData}s.
	 */
	Map<String, RuleConditionDefinitionData> getConditionDefinitionsForRuleTypeAsMap(Class<?> ruleType);
}
