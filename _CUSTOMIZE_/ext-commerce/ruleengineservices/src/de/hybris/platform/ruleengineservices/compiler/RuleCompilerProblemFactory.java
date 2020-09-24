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
package de.hybris.platform.ruleengineservices.compiler;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;


/**
 * Implementations of this interface are responsible for creating {@link RuleCompilerProblem}.
 */
public interface RuleCompilerProblemFactory
{
	/**
	 * Creates a new rule compiler problem with localized message with parameters.
	 *
	 * @param severity
	 * @param messageKey
	 *           - key for localized message. If not found, key will be displayed
	 * @param parameters
	 *           - message arguments
	 *
	 * @return the new compiler problem
	 */
	RuleCompilerProblem createProblem(final Severity severity, final String messageKey, Object... parameters);

	/**
	 * Creates a new rule compiler parameter problem with localized message with parameters.
	 *
	 * @param severity
	 * @param messageKey
	 *           - key for localized message. If not found, key will be displayed
	 * @param parameterData
	 * @param parameterDefinitionData
	 * @param parameters
	 *           - message arguments
	 *
	 * @return the new compiler problem
	 */
	RuleCompilerParameterProblem createParameterProblem(final Severity severity, final String messageKey,
			final RuleParameterData parameterData, final RuleParameterDefinitionData parameterDefinitionData, final Object... parameters);
}
