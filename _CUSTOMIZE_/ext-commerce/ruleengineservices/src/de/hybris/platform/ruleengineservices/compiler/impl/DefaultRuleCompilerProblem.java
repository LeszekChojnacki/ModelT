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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;


/**
 * Default implementation of {@link RuleCompilerProblem}.
 */
public class DefaultRuleCompilerProblem implements RuleCompilerProblem
{
	private final Severity severity;
	private final String message;

	public DefaultRuleCompilerProblem(final Severity severity, final String message)
	{
		this.severity = severity;
		this.message = message;
	}

	@Override
	public Severity getSeverity()
	{
		return severity;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
