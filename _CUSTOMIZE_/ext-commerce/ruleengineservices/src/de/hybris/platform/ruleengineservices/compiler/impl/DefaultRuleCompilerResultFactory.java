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
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult.Result;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResultFactory;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;


/**
 * Default implementation of {@link RuleCompilerResultFactory}
 *
 */
public class DefaultRuleCompilerResultFactory implements RuleCompilerResultFactory
{

	@Override
	public RuleCompilerResult create(final AbstractRuleModel rule, final Result result,
			final List<RuleCompilerProblem> problems)
	{
		return new DefaultRuleCompilerResult(rule.getCode(), result, problems);
	}

	@Override
	public RuleCompilerResult create(final AbstractRuleModel rule, final List<RuleCompilerProblem> problems)
	{
		return new DefaultRuleCompilerResult(rule.getCode(), problems);
	}

	@Override
	public RuleCompilerResult create(final RuleCompilerResult compilerResult, final long ruleVersion)
	{
		return new DefaultRuleCompilerResult(compilerResult.getRuleCode(), compilerResult.getResult(), compilerResult.getProblems(),
				ruleVersion);
	}

}
