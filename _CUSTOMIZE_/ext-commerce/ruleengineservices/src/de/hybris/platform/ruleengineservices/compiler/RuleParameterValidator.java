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

import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;


/**
 * Implementations of this interface are responsible for validating rule parameter data only during the compilation phase.
 */
public interface RuleParameterValidator
{
	/**
	 * Validate a rule parameter
	 *
	 * @param context
	 *           - Compiler context
	 * @param ruleDefinition
	 *           - Abstract rule definition
	 * @param parameter
	 *           - List of rule parameter data
	 * @param parameterDefinition
	 *           - List of rule parameter definitions
	 */
	void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter,
			RuleParameterDefinitionData parameterDefinition);
}
