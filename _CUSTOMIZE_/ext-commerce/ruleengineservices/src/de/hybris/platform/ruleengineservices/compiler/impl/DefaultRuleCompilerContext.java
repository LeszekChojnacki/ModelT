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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;


/**
 * Default implementation of {@link RuleCompilerContext}.
 */
public class DefaultRuleCompilerContext implements RuleCompilerContext
{
	private final AbstractRuleModel rule;
	private long ruleVersion;
	private final String moduleName;
	private final List<RuleParameterData> ruleParameters;
	private final List<RuleConditionData> ruleConditions;

	private final RuleIrVariablesGenerator variablesGenerator;

	private final Map<String, Object> attributes;
	private final List<Exception> failureExceptions;
	private final Map<String, RuleConditionDefinitionData> conditionDefinitions;
	private final Map<String, RuleActionDefinitionData> actionDefinitions;
	private RuleCompilationContext ruleCompilationContext;

	private final List<RuleCompilerProblem> problems;
	
	public DefaultRuleCompilerContext(final RuleCompilationContext ruleCompilationContext, final AbstractRuleModel rule,
				 final String moduleName, final RuleIrVariablesGenerator variablesGenerator)
	{
		this.ruleCompilationContext = ruleCompilationContext;
		this.rule = rule;
		this.moduleName = moduleName;
		this.ruleParameters = newArrayList();
		this.variablesGenerator = variablesGenerator;
		this.attributes = newHashMap();
		this.failureExceptions = newArrayList();
		this.conditionDefinitions = newHashMap();
		this.actionDefinitions = newHashMap();
		this.problems = newArrayList();
		this.ruleConditions = newArrayList();
	}

	@Override
	public AbstractRuleModel getRule()
	{
		return rule;
	}

	@Override
	public String getModuleName()
	{
		return moduleName;
	}



	@Override
	public List<RuleParameterData> getRuleParameters()
	{
		return ruleParameters;
	}

	@Override
	public RuleIrVariablesGenerator getVariablesGenerator()
	{
		return variablesGenerator;
	}

	@Override
	public String generateVariable(final Class<?> type)
	{
		return variablesGenerator.generateVariable(type);
	}

	@Override
	public RuleIrLocalVariablesContainer createLocalContainer()
	{
		return variablesGenerator.createLocalContainer();
	}

	@Override
	public String generateLocalVariable(final RuleIrLocalVariablesContainer container, final Class<?> type)
	{
		return variablesGenerator.generateLocalVariable(container, type);
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	public void addFailureException(final Exception exception)
	{
		failureExceptions.add(exception);
	}

	@Override
	public List<Exception> getFailureExceptions()
	{
		return failureExceptions;
	}

	@Override
	public List<RuleCompilerProblem> getProblems()
	{
		return problems;
	}

	@Override
	public void addProblem(final RuleCompilerProblem problem)
	{
		problems.add(problem);
	}

	@Override
	public Map<String, RuleConditionDefinitionData> getConditionDefinitions()
	{
		return conditionDefinitions;
	}

	@Override
	public Map<String, RuleActionDefinitionData> getActionDefinitions()
	{
		return actionDefinitions;
	}

	@Override
	public List<RuleConditionData> getRuleConditions()
	{
		return ruleConditions;
	}

	@Override
	public RuleCompilationContext getRuleCompilationContext()
	{
		return ruleCompilationContext;
	}

	@Override
	public long getRuleVersion()
	{
		return ruleVersion;
	}

	@Override
	public void setRuleVersion(final long version)
	{
		this.ruleVersion = version;
	}
}
