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
 * Implementations of this interface create breadcrumbs for the conditions.
 *
 */
public interface RuleConditionBreadcrumbsBuilder
{
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
}
