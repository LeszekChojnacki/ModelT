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
package de.hybris.platform.droolsruleengineservices.compiler;

import org.apache.commons.lang3.StringUtils;


public interface DroolsRuleConditionsGenerator
{
	/**
	 * Generates the conditions for the Drools rule engine.
	 *
	 * @param context
	 * 		- the drools rule generator context
	 * @param indentation
	 * 		- the indentation
	 * @return the String representation
	 */
	String generateConditions(final DroolsRuleGeneratorContext context, String indentation);

	/**
	 * Generates the required facts check pattern for the rule's LHS
	 *
	 * @param context
	 * 		- the drools rule generator context
	 * @return string, containing the check pattern
	 */
	String generateRequiredFactsCheckPattern(DroolsRuleGeneratorContext context);

	/**
	 * Generates the required type variables for the rule's LHS
	 *
	 * @param context
	 *           - the drools rule generator context
	 * @return string, containing the type variables
	 */
	default String generateRequiredTypeVariables(final DroolsRuleGeneratorContext context)
	{
		return StringUtils.EMPTY;
	}
}
