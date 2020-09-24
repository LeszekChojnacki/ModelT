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
package de.hybris.platform.ruleengineservices.rule.strategies;

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;

import java.util.List;
import java.util.Map;


/**
 * Implementations of this interface can perform conversions between condition data objects and a String representation
 * of those objects.
 */
public interface RuleConditionsConverter
{
	/**
	 * Converts the condition data objects to a String representation.
	 *
	 * @param conditions
	 *           - the condition data objects
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the String representation
	 */
	String toString(List<RuleConditionData> conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions);


	/**
	 * Converts the String representation to condition data objects.
	 *
	 * @param conditions
	 *           - the String representation
	 * @param conditionDefinitions
	 *           - condition definitions
	 *
	 * @return the condition data objects
	 */
	List<RuleConditionData> fromString(String conditions, Map<String, RuleConditionDefinitionData> conditionDefinitions);
}
