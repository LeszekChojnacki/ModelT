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
package de.hybris.platform.ruleengineservices.rule.dao;

import de.hybris.platform.ruleengineservices.model.RuleConditionDefinitionModel;

import java.util.List;


public interface RuleConditionDefinitionDao
{
	/**
	 * Finds List of {@link RuleConditionDefinitionModel}s.
	 *
	 * @return List of {@link RuleConditionDefinitionModel}s.
	 */
	List<RuleConditionDefinitionModel> findAllRuleConditionDefinitions();

	/**
	 * Finds all {@link RuleConditionDefinitionModel}s for a given rule type.
	 *
	 * @param ruleType
	 *           - model class of rule type
	 * @return List of {@link RuleConditionDefinitionModel}s
	 */
	List<RuleConditionDefinitionModel> findRuleConditionDefinitionsByRuleType(Class<?> ruleType);
}
