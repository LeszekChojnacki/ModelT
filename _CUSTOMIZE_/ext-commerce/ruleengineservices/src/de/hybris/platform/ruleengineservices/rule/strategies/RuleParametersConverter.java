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

import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;


/**
 * Implementations of this interface can perform conversions between parameter data objects and a String representation
 * of those objects.
 */
public interface RuleParametersConverter
{
	/**
	 * Converts the parameter data objects to a String representation.
	 *
	 * @param parameters
	 *           - the parameter data objects
	 *
	 * @return the String representation
	 */
	String toString(List<RuleParameterData> parameters);


	/**
	 * Converts the String representation to parameter data objects.
	 *
	 * @param parameters
	 *           - the String representation
	 *
	 * @return the parameter data objects
	 */
	List<RuleParameterData> fromString(String parameters);
}
