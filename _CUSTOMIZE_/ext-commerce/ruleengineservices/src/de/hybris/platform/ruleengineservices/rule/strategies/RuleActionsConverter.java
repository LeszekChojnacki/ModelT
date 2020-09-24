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

import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsService;

import java.util.List;
import java.util.Map;


/**
 * Implementations of this interface can perform conversions between actions data objects and a String representation of
 * those objects.
 */
public interface RuleActionsConverter
{
	/**
	 * Converts the action data objects to a String representation. In most cases it is better to use
	 * {@link RuleActionsService#convertActionsToString(List)}.
	 *
	 * @param actions
	 *           - the action data objects
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the String representation
	 */
	String toString(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions);

	/**
	 * Converts the String representation to action data objects. In most cases it is better to use
	 * {@link RuleActionsService#convertActionsFromString(String)}.
	 *
	 * @param actions
	 *           - the String representation
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the action data objects
	 */
	List<RuleActionData> fromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions);
}
