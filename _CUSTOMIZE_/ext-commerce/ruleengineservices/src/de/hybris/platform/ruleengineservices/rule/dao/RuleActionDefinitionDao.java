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

import de.hybris.platform.ruleengineservices.model.RuleActionDefinitionModel;

import java.util.List;


public interface RuleActionDefinitionDao
{
	/**
	 * Finds all {@link RuleActionDefinitionModel}s
	 *
	 * @return List of {@link RuleActionDefinitionModel}s
	 */
	List<RuleActionDefinitionModel> findAllRuleActionDefinitions();

	/**
	 * Finds all {@link RuleActionDefinitionModel}s for a given rule type.
	 *
	 * @param ruleType
	 *           - type of rule
	 * @return List of {@link RuleActionDefinitionModel}s
	 */
	List<RuleActionDefinitionModel> findRuleActionDefinitionsByRuleType(Class<?> ruleType);
}
