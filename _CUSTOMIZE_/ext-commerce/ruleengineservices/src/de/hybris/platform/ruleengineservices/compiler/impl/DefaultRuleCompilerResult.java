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
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;

import java.util.List;


/**
 * Default implementation of {@link RuleCompilerResult}.
 */
public class DefaultRuleCompilerResult implements RuleCompilerResult
{
	private final String ruleCode;
	private long ruleVersion;
	private final Result result;
	private final List<RuleCompilerProblem> problems;

	public DefaultRuleCompilerResult(final String ruleCode, final Result result, final List<RuleCompilerProblem> problems)
	{
		this.ruleCode = ruleCode;
		this.result = result;
		this.problems = problems;
	}

	public DefaultRuleCompilerResult(final String ruleCode, final List<RuleCompilerProblem> problems)
	{
		this.ruleCode = ruleCode;
		this.problems = problems;
		if (problems != null)
		{
			this.result = problems.stream().map(RuleCompilerProblem::getSeverity).anyMatch(Severity.ERROR::equals) ? Result.ERROR
					: Result.SUCCESS;
		}
		else
		{
			this.result = Result.SUCCESS;
		}
	}

	public DefaultRuleCompilerResult(final String ruleCode, final Result result, final List<RuleCompilerProblem> problems,
			final long ruleVersion)
	{
		this(ruleCode, result, problems);
		this.ruleVersion = ruleVersion;
	}

	@Override
	public String getRuleCode()
	{
		return ruleCode;
	}

	@Override
	public long getRuleVersion()
	{
		return ruleVersion;
	}

	@Override
	public Result getResult()
	{
		return result;
	}

	@Override
	public List<RuleCompilerProblem> getProblems()
	{
		return problems;
	}
}
