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

import de.hybris.platform.ruleengineservices.model.SourceRuleModel;


/**
 * Allows a source rule inspection by checking if a condition or an action definition is present for a given rule
 */
public interface SourceRuleInspector
{
	/**
	 * Check if given condition definition is present in a source rule
	 * 
	 * @param rule
	 * @param conditionDefinitionId
	 * @return true if the condition defintion is present otherwise false
	 */
	boolean hasRuleCondition(SourceRuleModel rule, String conditionDefinitionId);

	/**
	 * Check if given action definition is present in a source rule
	 * 
	 * @param rule
	 * @param actionDefinitionId
	 * @return true if the action defintion is present otherwise false
	 */
	boolean hasRuleAction(SourceRuleModel rule, String actionDefinitionId);
}
