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

import java.io.Serializable;


/**
 * A parameter decorator can be used to change the message parameters after they were formatted.
 */
@FunctionalInterface
public interface RuleMessageParameterDecorator extends Serializable
{
	/**
	 * @param formattedValue
	 *           - the formatted value of the parameter
	 * @param parameter
	 *           - the parameter
	 *
	 * @return the decorated value
	 */
	String decorate(String formattedValue, RuleParameterData parameter);
}
