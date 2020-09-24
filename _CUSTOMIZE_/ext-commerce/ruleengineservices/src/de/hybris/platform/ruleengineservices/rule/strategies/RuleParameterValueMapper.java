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
 * Implementations of this interface can perform mapping between objects and a String representations of those objects.
 *
 * @param <T>
 *           - the type that the mapper can handle
 */
public interface RuleParameterValueMapper<T>
{
	/**
	 * Maps the object to its String representation.
	 *
	 * @param value
	 *           - object to map
	 *
	 * @return String representation
	 */
	String toString(T value);

	/**
	 * Maps the String representation to the specific object.
	 *
	 * @param value
	 *           - String representation
	 *
	 * @return mapped object
	 */
	T fromString(String value);
}
