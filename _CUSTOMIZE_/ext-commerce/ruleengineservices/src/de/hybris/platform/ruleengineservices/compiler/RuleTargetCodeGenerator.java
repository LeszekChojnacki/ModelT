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

/**
 * Implementations of this interface are responsible for converting from the intermediate representation to a rule
 * engine specific format.
 */
public interface RuleTargetCodeGenerator
{
	/**
	 * Generates the rule engine specific code from the intermediate representation.
	 *
	 * @param context
	 * 			 - the rule compiler context
	 * @param ruleIr
	 * 			 - the intermediate representation
	 */
	void generate(RuleCompilerContext context, RuleIr ruleIr);

}
