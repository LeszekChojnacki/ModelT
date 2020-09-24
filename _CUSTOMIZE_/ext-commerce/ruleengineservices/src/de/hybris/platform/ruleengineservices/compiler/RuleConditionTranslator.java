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
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;


/**
 * Implementations of this interface are responsible for converting a rule condition to the intermediate representation.
 */
public interface RuleConditionTranslator
{
	/**
	 * Translates a rule condition to the intermediate representation.
	 *
	 * @param context
	 *           - the compiler context
	 * @param condition
	 *           - the condition
	 * @param conditionDefinition
	 *           - the condition definition
	 *
	 * @return the intermediate representation for the condition
	 */
	RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition,
			RuleConditionDefinitionData conditionDefinition);
}
