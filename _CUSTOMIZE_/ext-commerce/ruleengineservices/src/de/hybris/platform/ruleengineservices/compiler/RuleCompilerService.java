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

import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;


/**
 * Service that can be used to perform compilation of rules. Compilation means transforming a generic rule
 * representation (conditions + actions) into a format that a specific rule engine can understand.
 */
public interface RuleCompilerService
{

	/**
	 * Compiles a rule.
	 *
	 * @param ruleCompilationContext
	 * 		- instance of {@link RuleCompilationContext}
	 * @param rule
	 * 		- the rule to compile
	 * @param moduleName
	 * 		- the rules module name
	 * @return rule compiler result
	 */
	RuleCompilerResult compile(RuleCompilationContext ruleCompilationContext, AbstractRuleModel rule,
			String moduleName);

	/**
	 * Compiles a rule.
	 *
	 * @param rule
	 * 		- the rule to compile
	 * @param moduleName
	 * 		- the rules module name
	 * @return rule compiler result
	 */
	RuleCompilerResult compile(AbstractRuleModel rule, String moduleName);
}
