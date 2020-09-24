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
 * Implementations of this interface are responsible for converting from a specific rule type to the intermediate
 * representation.
 */
public interface RuleSourceCodeTranslator
{
	/**
	 * Translates the source code into the intermediate representation.
	 *
	 * @param context
	 *           - the rule compiler context
	 *
	 * @return the intermediate representation
	 */
	RuleIr translate(RuleCompilerContext context);
}
