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
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;


/**
 * This interface represents the context valid for the compilation of a rule. It is passed to, and potentially modified
 * by, each phase of the compilation process.
 */
public interface RuleCompilerContext
{
	/**
	 * Returns the rule.
	 *
	 * @return rule
	 */
	AbstractRuleModel getRule();

	/**
	 * Returns the rule version.
	 *
	 * @return engine rule version
	 */
	long getRuleVersion();

	/**
	 * Provides means of setting engine version of the rule.
	 *
	 * @param version
	 * 					- engine rule version
	 */
	void setRuleVersion(long version);

	/**
	 * Returns the rules module name.
	 *
	 * @return rules module name
	 */
	String getModuleName();

	/**
	 * Returns the rule parameters.
	 *
	 * @return The rule parameters
	 */
	List<RuleParameterData> getRuleParameters();

	/**
	 * Returns the variables generator valid for this compiler context.
	 *
	 * @return the variables generator
	 */
	RuleIrVariablesGenerator getVariablesGenerator();

	/**
	 * A shortcut to {@link RuleIrVariablesGenerator#generateVariable(Class)}.
	 *
	 * @param type
	 * 			 - the type
	 * @return the name of the variable
	 */
	String generateVariable(Class<?> type);

	/**
	 * A shortcut to {@link RuleIrVariablesGenerator#createLocalContainer()}.
	 *
	 * @return the current container
	 */
	RuleIrLocalVariablesContainer createLocalContainer();

	/**
	 * A shortcut to {@link RuleIrVariablesGenerator#generateLocalVariable(RuleIrLocalVariablesContainer, Class)}.
	 *
	 * @param type
	 * 			 - the type
	 * @return the name of the variable
	 */
	String generateLocalVariable(RuleIrLocalVariablesContainer container, Class<?> type);

	/**
	 * Returns a mutable {@link Map} that can be used to store attributes associated with this
	 * {@link RuleCompilerContext}.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();

	/**
	 * Returns all failure causing exceptions for this {@link RuleCompilerContext}.
	 */
	List<Exception> getFailureExceptions();

	/**
	 * Returns all problems arose during compilation process.
	 */
	List<RuleCompilerProblem> getProblems();

	/**
	 * Adds a problem arose during compilation process to the list.
	 *
	 * @param problem
	 */
	void addProblem(RuleCompilerProblem problem);

	/**
	 * Returns condition definitions for rule
	 */
	Map<String, RuleConditionDefinitionData> getConditionDefinitions();

	/**
	 * Returns action definitions for rule
	 */
	Map<String, RuleActionDefinitionData> getActionDefinitions();

	/**
	 * Returns rule conditions for rule
	 */
	List<RuleConditionData> getRuleConditions();

	/**
	 * get Rules compilation context for a given compilation
	 *
	 * @return instance of {@link RuleCompilationContext}
	 */
	RuleCompilationContext getRuleCompilationContext();
}
