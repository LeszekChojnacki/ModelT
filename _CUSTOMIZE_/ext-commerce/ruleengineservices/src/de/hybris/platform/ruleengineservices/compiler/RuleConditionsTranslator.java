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

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;

import java.util.List;


/**
 * Helper service that can be used to translate generic rule conditions to the intermediate representation.
 */
public interface RuleConditionsTranslator
{
	/**
	 * Validates the generic rule conditions.
	 *
	 * @param context
	 *           - the rule compiler context
	 * @param conditions
	 *           - the conditions
	 */
	void validate(RuleCompilerContext context, List<RuleConditionData> conditions);

	/**
	 * Translates generic rule conditions to the intermediate representation.
	 *
	 * @param context
	 *           - the rule compiler context
	 * @param conditions
	 *           - the conditions
	 *
	 * @return the intermediate representation of the conditions
	 */
	List<RuleIrCondition> translate(final RuleCompilerContext context, final List<RuleConditionData> conditions);
}
