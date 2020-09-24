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

import java.util.List;
import java.util.Map;


/**
 * Implementations of this interface create breadcrumbs for the actions.
 *
 */
public interface RuleActionBreadcrumbsBuilder
{
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
}
