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

import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;

import java.util.List;
import java.util.Map;


/**
 * The interface provides with method for creating action from rule action definition and build action breadcrumbs.
 */
public interface RuleActionsService
{
	/**
	 * Creates a new action from a definition.
	 *
	 * @param definition
	 *           - the action definition
	 *
	 * @return the new action
	 */
	RuleActionData createActionFromDefinition(RuleActionDefinitionData definition);

	/**
	 * Builds breadcrumbs for the actions.
	 *
	 * @param actions
	 *           - actions
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the action breadcrumbs
	 */
	String buildActionBreadcrumbs(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions);

	/**
	 * Builds styled breadcrumbs for the actions.
	 *
	 * @param actions
	 *           - actions
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the styled action breadcrumbs
	 */
	String buildStyledActionBreadcrumbs(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions);

	/**
	 * Converts the {@link RuleActionData} objects to a String representation.
	 *
	 * @param actions
	 *           - the action data objects
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the String representation
	 */
	String convertActionsToString(List<RuleActionData> actions, Map<String, RuleActionDefinitionData> actionDefinitions);

	/**
	 * Converts the String representation to {@link RuleActionData} objects.
	 *
	 * @param actions
	 *           - the String representation
	 * @param actionDefinitions
	 *           - action definitions
	 *
	 * @return the action data objects
	 *
	 */
	List<RuleActionData> convertActionsFromString(String actions, Map<String, RuleActionDefinitionData> actionDefinitions);
}
