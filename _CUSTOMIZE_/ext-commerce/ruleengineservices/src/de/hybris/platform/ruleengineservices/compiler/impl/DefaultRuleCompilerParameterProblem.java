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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerParameterProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;


public class DefaultRuleCompilerParameterProblem extends DefaultRuleCompilerProblem implements RuleCompilerParameterProblem
{
	private final RuleParameterData parameter;
	private final RuleParameterDefinitionData parameterDefinition;

	public DefaultRuleCompilerParameterProblem(final RuleCompilerProblem.Severity severity, final String message, final RuleParameterData parameter,
			final RuleParameterDefinitionData parameterDefinition)
	{
		super(severity, message);
		this.parameter = parameter;
		this.parameterDefinition = parameterDefinition;
	}

	@Override
	public RuleParameterData getParameter()
	{
		return parameter;
	}

	@Override
	public RuleParameterDefinitionData getParameterDefinition()
	{
		return parameterDefinition;
	}
}
