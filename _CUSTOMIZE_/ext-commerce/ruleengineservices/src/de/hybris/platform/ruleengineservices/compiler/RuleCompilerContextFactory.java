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
 * Implementations of this interface are responsible for creating and destroying instances of
 * {@link RuleCompilerContext}.
 */
public interface RuleCompilerContextFactory<T extends RuleCompilerContext>
{

	/**
	 * Creates a new compiler context and sets it as the current one.
	 *
	 * @param ruleCompilationContext
	 * 		- instance of {@link RuleCompilationContext}
	 * @param rule
	 * 		- the rule to compile
	 * @param moduleName
	 * 		- the rules module name
	 * @param variablesGenerator
	 * 		- the variables generator
	 * @return the new context
	 */
	T createContext(final RuleCompilationContext ruleCompilationContext, final AbstractRuleModel rule,
			final String moduleName, final RuleIrVariablesGenerator variablesGenerator);

}
