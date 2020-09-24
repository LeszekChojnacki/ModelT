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
 * This class provides the method <code>normalize</code> which transforms provided value into an
 * equivalent form, allowing for proper usage of the respective value
 */
public interface RuleParameterValueNormalizer
{
	/**
	 * Normalizes provided value based on the criteria matching given type
	 *
	 * @param value
	 *           - the value to be normalized
	 *
	 *  @return the normalized value for the required type
	 */
	Object normalize(Object value);
}
