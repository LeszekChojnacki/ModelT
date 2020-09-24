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
package de.hybris.platform.droolsruleengineservices.compiler;

import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;

import java.util.Deque;
import java.util.Map;
import java.util.Set;


public interface DroolsRuleGeneratorContext
{
	RuleCompilerContext getRuleCompilerContext();

	String getIndentationSize();

	String getVariablePrefix();

	String getAttributeDelimiter();

	RuleIr getRuleIr();

	Map<String, RuleIrVariable> getVariables();

	Deque<Map<String, RuleIrVariable>> getLocalVariables();

	void addLocalVariables(Map<String, RuleIrVariable> ruleIrVariables);

	Set<Class<?>> getImports();

	Map<String, Class<?>> getGlobals();

	String generateClassName(final Class<?> clazz);

	void addGlobal(final String name, final Class<?> clazz);

	DroolsRuleModel getDroolsRule();
}
