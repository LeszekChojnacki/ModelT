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
package de.hybris.platform.ruleengineservices.maintenance;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;


/**
 * interface for compiler task spliterator, accumulating and splitting the rules to be compiled and published
 *
 * @param <T>
 * 			 concrete type of the {@link AbstractRuleModel}
 */
public interface RuleCompilerSpliterator<T extends AbstractRuleModel>
{

	/**
	 * compile the single rule
	 *
	 * @param rule
	 * 			 the instance of {@link AbstractRuleModel} to compile
	 * @param moduleName
	 * 			 rules module name
	 * @return instance of {@link RuleCompilerResult}
	 */
	RuleCompilerResult compileSingleRule(T rule, String moduleName);

	/**
	 * split and compile multiple rules in parallel
	 *
	 * @param rules
	 * 			 a list of {@link AbstractRuleModel} to compile
	 * @param moduleName
	 * 			 rules module name
	 * @return instance of {@link RuleCompilerFuture}
	 */
	RuleCompilerFuture compileRulesAsync(List<T> rules, String moduleName);

}
