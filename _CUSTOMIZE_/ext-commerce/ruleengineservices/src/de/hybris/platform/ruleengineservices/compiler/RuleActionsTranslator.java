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
package de.hybris.platform.ruleengineservices.compiler;

import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;

import java.util.List;


/**
 * Helper service that can be used to translate generic rule actions to the intermediate representation.
 */
public interface RuleActionsTranslator
{
	/**
	 * Validates the generic rule actions.
	 *
	 * @param context
	 *           - the rule compiler context
	 * @param actions
	 *           - the actions
	 */
	void validate(RuleCompilerContext context, List<RuleActionData> actions);

	/**
	 * Translates generic rule actions to the intermediate representation.
	 *
	 * @param context
	 *           - the rule compiler context
	 * @param actions
	 *           - the actions
	 *
	 * @return the intermediate representation of the actions
	 */
	List<RuleIrAction> translate(RuleCompilerContext context, List<RuleActionData> actions);
}
