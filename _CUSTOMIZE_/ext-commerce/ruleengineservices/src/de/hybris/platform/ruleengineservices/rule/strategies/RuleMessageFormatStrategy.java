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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;


/**
 * Strategy that can be used to construct messages displayed for end users. Messages have the same format as supported
 * by {@link MessageFormat} with the following exceptions:
 * <ul>
 * <li>arguments are specified by id, not index, e.g.: {operator} or {10a05984-afef-4f8e-a3e1-3ced0783951c}</li>
 * <li>numbers can have a multiplier, e.g.: {value,number,percent*1}</li>
 * </ul>
 */
public interface RuleMessageFormatStrategy
{
	/**
	 * Formats a message with the given parameters and locale.
	 *
	 * @param message
	 *           - the message
	 * @param parameters
	 *           - the parameters
	 * @param locale
	 *           - the locale
	 *
	 * @return the formatted message
	 *
	 */
	String format(final String message, final Map<String, RuleParameterData> parameters, Locale locale);

	/**
	 * Formats a message with the given parameters and locale. A parameter decorator can be used to change the message
	 * parameters after they were formatted.
	 *
	 * @param message
	 *           - the message
	 * @param parameters
	 *           - the parameters
	 * @param locale
	 *           - the locale
	 * @param parameterDecorator
	 *           - the parameter decorator
	 *
	 * @return the formatted message
	 */
	String format(final String message, final Map<String, RuleParameterData> parameters, Locale locale,
			RuleMessageParameterDecorator parameterDecorator);
}
