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
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;


/**
 * Implementations of this interface are responsible for converting a rule action to the intermediate representation.
 */
public interface RuleActionTranslator
{
	/**
	 * Translates a rule action to the intermediate representation.
	 *
	 * @param context
	 *           - the compiler context
	 * @param action
	 *           - the action
	 * @param actionDefinition
	 *           - the action definition
	 *
	 * @return the intermediate representation for the action
	 */
	RuleIrAction translate(RuleCompilerContext context, RuleActionData action, RuleActionDefinitionData actionDefinition);
}
