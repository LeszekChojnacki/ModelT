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


public interface RuleParameterValueNormalizerStrategy
{
	/**
	 * Normalizes provided value based on the criteria matching given type
	 *
	 * @param value
	 *           - the string value to be converted
	 * @param type
	 * 			 - parameter type
	 *
	 * @return the normalized value for the required type
	 */
	Object normalize(Object value, String type);
}
