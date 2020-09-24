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

import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.util.List;


/**
 * The interface provides with method for creating parameters from rule definition and also convert parameters.
 */
public interface RuleParametersService
{
	/**
	 * Creates a new parameter from a definition.
	 *
	 * @param definition
	 *           - the parameter definition
	 *
	 * @return the new parameter
	 */
	RuleParameterData createParameterFromDefinition(RuleParameterDefinitionData definition);

	/**
	 * Converts the {@link RuleParameterData} objects to a String representation.
	 *
	 * @param parameters
	 *           - the parameter data objects
	 *
	 * @return the String representation
	 */
	String convertParametersToString(List<RuleParameterData> parameters);

	/**
	 * Converts the String representation to {@link RuleParameterData} objects.
	 *
	 * @param parameters
	 *           - the String representation
	 *
	 * @return the parameter data objects
	 */
	List<RuleParameterData> convertParametersFromString(String parameters);
}
