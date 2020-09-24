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

/**
 * Implementations for this interface can format a value to its drools representation.
 */
public interface DroolsRuleValueFormatter
{
	/**
	 * Converts a value to its drools representation.
	 *
	 * @param context
	 *           - the drools rule generator context
	 * @param value
	 *           - the value to convert
	 * @throws {@link DroolsRuleValueFormatterException}
	 * @return the drools representation for the value
	 */
	String formatValue(DroolsRuleGeneratorContext context, Object value);
}
