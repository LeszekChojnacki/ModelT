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
package de.hybris.platform.ruleengineservices.rule.services;

/**
 * This interface provides means of evaluating rule parameter filters
 */
public interface RuleParameterFilterValueProvider
{
	/**
	 * Identifies parameter id within the provided String value
	 *
	 * @param value
	 * 			- whole value to identify parameter id from
	 *
	 * @return parameter id from the provided value
	 */
	String getParameterId(String value);

	/**
	 * Performs evaluation of the provided filter expression encoded within its value, matching it against
	 * object that acts as an evaluation context
	 *
	 * @param value
	 * 			- whole value to identify parameter id from
	 *
	 * @param contextObject
	 * 			- object used as a context for the evaluated expression
	 *
	 * @return evaluated object based on the provided expression and context
	 */
	Object evaluate(String value, Object contextObject);
}
