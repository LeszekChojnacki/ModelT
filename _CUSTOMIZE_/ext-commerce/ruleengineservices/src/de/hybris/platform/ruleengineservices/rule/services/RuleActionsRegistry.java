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

import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;

import java.util.List;
import java.util.Map;


public interface RuleActionsRegistry
{
	/**
	 * Returns all {@link RuleActionDefinitionData} objects.
	 *
	 * @return all {@link RuleActionDefinitionData}s.
	 */
	List<RuleActionDefinitionData> getAllActionDefinitions();

	/**
	 * Returns all {@link RuleActionDefinitionData} objects.
	 *
	 * @return all {@link RuleActionDefinitionData}s.
	 */
	Map<String, RuleActionDefinitionData> getAllActionDefinitionsAsMap();

	/**
	 * Returns {@link RuleActionDefinitionData} objects for given rule type.
	 *
	 * @param ruleType
	 *           - type of rule (ie. PromotionSourceRule)
	 *
	 * @return list of {@link RuleActionDefinitionData}s for given rule type.
	 */
	List<RuleActionDefinitionData> getActionDefinitionsForRuleType(Class<?> ruleType);

	/**
	 * Returns {@link RuleActionDefinitionData} objects for given rule type.
	 *
	 * @param ruleType
	 *           - type of rule (ie. PromotionSourceRule)
	 *
	 * @return Map of rule action definition ids and {@link RuleActionDefinitionData}s.
	 */
	Map<String, RuleActionDefinitionData> getActionDefinitionsForRuleTypeAsMap(Class<?> ruleType);
}
