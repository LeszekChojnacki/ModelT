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

/**
 * Converter for primitive types and some other supported types.
 */
public interface RuleParameterValueConverter
{
	/**
	 * Converts from the required type to String.
	 *
	 * @param value
	 *           - the value to be converted to string
	 *
	 * @return the required type converted to String
	 */
	String toString(Object value);

	/**
	 * Converts from String to the required type.
	 *
	 * @param value
	 *           - the string value to be converted
	 *
	 * @return the converted value from string to the required type
	 */
	Object fromString(String value, String type);
}
