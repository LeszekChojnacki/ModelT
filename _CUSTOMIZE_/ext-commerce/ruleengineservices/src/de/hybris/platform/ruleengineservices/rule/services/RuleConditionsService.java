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

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;


/**
 * The interface provides with method for creating condition from rule condition definition and build condition
 * breadcrumbs.
 */
public interface RuleConditionsService
{
	/**
	 * Creates a new condition from a definition.
	 *
	 * @param definition
	 *           - the condition definition
	 *
	 * @return the new condition
	 */
	RuleConditionData createConditionFromDefinition(RuleConditionDefinitionData definition);

	/**
	 * Builds breadcrumbs for the conditions.
	 *
	 * @param conditions
	 *           - conditions
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the condition breadcrumbs
	 */
	String buildConditionBreadcrumbs(List<RuleConditionData> conditions,
			Map<String, RuleConditionDefinitionData> conditionDefinitions);

	/**
	 * Builds styled breadcrumbs for the conditions.
	 *
	 * @param conditions
	 *           - conditions
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the styled condition breadcrumbs
	 */
	String buildStyledConditionBreadcrumbs(List<RuleConditionData> conditions,
			Map<String, RuleConditionDefinitionData> conditionDefinitions);


	/**
	 * Converts the {@link RuleConditionData} objects to a String representation.
	 *
	 * @param conditions
	 *           - the condition data objects
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the String representation
	 */
	String convertConditionsToString(List<RuleConditionData> conditions,
			Map<String, RuleConditionDefinitionData> conditionDefinitions);

	/**
	 * Converts the String representation to {@link RuleConditionData} objects.
	 *
	 * @param conditions
	 *           - the String representation
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the condition data objects
	 */
	List<RuleConditionData> convertConditionsFromString(String conditions,
			Map<String, RuleConditionDefinitionData> conditionDefinitions);
}
