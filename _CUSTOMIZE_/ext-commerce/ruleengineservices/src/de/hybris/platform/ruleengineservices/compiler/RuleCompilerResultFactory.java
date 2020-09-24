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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult.Result;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;


/**
 * Implementations of this interface are responsible for creating {@link RuleCompilerResult}.
 */
public interface RuleCompilerResultFactory
{
	/**
	 * Creates a result of rule compiler process.
	 *
	 * @param rule
	 * 			 - source rule entity
	 * @param result
	 *           - enum: error or success
	 * @param problems
	 *           - list of compilation problems
	 * @return rule compiler result
	 */
	RuleCompilerResult create(AbstractRuleModel rule, Result result, List<RuleCompilerProblem> problems);

	/**
	 * Creates a result of rule compiler process. Based on problems, calculates if the compilation process was successful
	 * or not.
	 *
	 * @param rule
	 * 			 - source rule entity
	 * @param problems
	 *           - list of compilation problems
	 * @return rule compiler result
	 */
	RuleCompilerResult create(AbstractRuleModel rule, List<RuleCompilerProblem> problems);

	/**
	 * Creates a result of rule compiler process based on the existing compiler result, decorating it with rule version information.
	 *
	 * @param compilerResult
	 * 			 - rule compiler result
	 * @param ruleVersion
	 *           - version of the compiled rule
	 * @return rule compiler result
	 */
	RuleCompilerResult create(RuleCompilerResult compilerResult, long ruleVersion);
}
