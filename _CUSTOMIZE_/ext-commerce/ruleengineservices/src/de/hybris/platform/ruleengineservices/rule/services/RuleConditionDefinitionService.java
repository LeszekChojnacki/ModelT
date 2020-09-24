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

import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;

import java.util.List;


/**
 * The interface provides with method for retrieving rule condition definitions.
 */
public interface RuleConditionDefinitionService
{
	/**
	 * Finds List of {@link RuleConditionDefinitionModel}s.
	 *
	 * @return List of {@link RuleConditionDefinitionModel}s.
	 */
	List<RuleConditionDefinitionModel> getAllRuleConditionDefinitions();

	/**
	 * Finds List of {@link RuleConditionDefinitionModel}s for given rule type.
	 *
	 * @param ruleType
	 *           - type of rule
	 *
	 * @return List of {@link RuleConditionDefinitionModel}s.
	 */
	List<RuleConditionDefinitionModel> getRuleConditionDefinitionsForRuleType(final Class<?> ruleType);
}
