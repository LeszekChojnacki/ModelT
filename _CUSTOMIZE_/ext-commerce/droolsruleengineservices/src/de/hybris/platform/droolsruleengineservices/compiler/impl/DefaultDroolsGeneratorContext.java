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
package de.hybris.platform.droolsruleengineservices.compiler.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesContainer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ClassUtils;

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.google.common.collect.Maps.newConcurrentMap;


public class DefaultDroolsGeneratorContext implements DroolsRuleGeneratorContext
{
	public static final String DEFAULT_INDENTATION_SIZE = "   ";
	public static final String DEFAULT_VARIABLE_PREFIX = "$";
	public static final String DEFAULT_ATTRIBUTE_DELIMITER = ".";

	private final RuleCompilerContext ruleCompilerContext;
	private final RuleIr ruleIr;
	private DroolsRuleModel droolsRule;
	private final Deque<Map<String, RuleIrVariable>> localVariables;

	private final Map<String, Class<?>> imports;
	private final Map<String, Class<?>> globals;
	private Map<String, RuleIrVariable> variables;

	public DefaultDroolsGeneratorContext(final RuleCompilerContext ruleCompilerContext, final RuleIr ruleIr, final DroolsRuleModel droolsRule)
	{
		this.ruleCompilerContext = ruleCompilerContext;
		this.ruleIr = ruleIr;
		this.droolsRule = droolsRule;
		this.localVariables = new ConcurrentLinkedDeque<>();
		this.imports = newConcurrentMap();
		this.globals = newConcurrentMap();
	}

	@Override
	public String getIndentationSize()
	{
		return DEFAULT_INDENTATION_SIZE;
	}

	@Override
	public String getVariablePrefix()
	{
		return DEFAULT_VARIABLE_PREFIX;
	}

	@Override
	public String getAttributeDelimiter()
	{
		return DEFAULT_ATTRIBUTE_DELIMITER;
	}

	@Override
	public RuleCompilerContext getRuleCompilerContext()
	{
		return ruleCompilerContext;
	}

	@Override
	public RuleIr getRuleIr()
	{
		return ruleIr;
	}

	@Override
	public DroolsRuleModel getDroolsRule()
	{
		return droolsRule;
	}

	@Override
	public Map<String, RuleIrVariable> getVariables()
	{
		if (this.variables == null)
		{
			this.variables = newConcurrentMap();
			populateVariables(ruleIr.getVariablesContainer());
		}
		return variables;
	}

	@Override
	public Deque<Map<String, RuleIrVariable>> getLocalVariables()
	{
		return localVariables;
	}

	@Override
	public void addLocalVariables(final Map<String, RuleIrVariable> ruleIrVariables)
	{
		localVariables.offerFirst(ruleIrVariables);
	}

	@Override
	public Set<Class<?>> getImports()
	{
		return ImmutableSet.copyOf(imports.values());
	}

	@Override
	public Map<String, Class<?>> getGlobals()
	{
		return ImmutableMap.copyOf(globals);
	}

	@Override
	public String generateClassName(final Class<?> type)
	{
		final String shortClassName = ClassUtils.getShortClassName(type);
		final Class<?> existingType = imports.get(shortClassName);

		if (existingType == null)
		{
			imports.put(shortClassName, type);
			return shortClassName;
		}
		else if (existingType.equals(type))
		{
			return shortClassName;
		}
		else
		{
			return type.getName();
		}
	}

	@Override
	public void addGlobal(final String name, final Class<?> type)
	{
		globals.put(name, type);
	}

	protected void populateVariables(final RuleIrVariablesContainer variablesContainer)
	{
		if (MapUtils.isNotEmpty(variablesContainer.getVariables()))
		{
			for (final RuleIrVariable variable : variablesContainer.getVariables().values())
			{
				this.variables.put(variable.getName(), variable);
			}
		}

		if (MapUtils.isNotEmpty(variablesContainer.getChildren()))
		{
			for (final RuleIrVariablesContainer childVariablesContainer : variablesContainer.getChildren().values())
			{
				populateVariables(childVariablesContainer);
			}
		}
	}
}
