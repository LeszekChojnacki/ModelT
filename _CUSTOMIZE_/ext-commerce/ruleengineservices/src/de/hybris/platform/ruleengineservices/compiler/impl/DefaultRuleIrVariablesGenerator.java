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

import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import org.fest.util.Arrays;


/**
 * Default implementation of {@link RuleIrVariablesGenerator}
 */
public class DefaultRuleIrVariablesGenerator implements RuleIrVariablesGenerator
{
	public static final String DEFAULT_VARIABLE_PREFIX = "v";

	private int count;
	private final RuleIrVariablesContainer rootContainer;
	private final Deque<RuleIrVariablesContainer> containers;

	public DefaultRuleIrVariablesGenerator()
	{
		this.count = 0;
		this.rootContainer = createNewContainerForId(DEFAULT_VARIABLES_CONTAINER_ID, null);
		this.containers = new ArrayDeque<>();
		this.containers.push(rootContainer);
	}

	@Override
	public RuleIrVariablesContainer getRootContainer()
	{
		return rootContainer;
	}

	@Override
	public RuleIrVariablesContainer getCurrentContainer()
	{
		if (containers.isEmpty())
		{
			throw new IllegalStateException("There should exist at least one root container but no container found");
		}

		return containers.peek();
	}

	@Override
	public RuleIrVariablesContainer createContainer(final String id)
	{
		final RuleIrVariablesContainer parentContainer = getCurrentContainer();
		final RuleIrVariablesContainer container = createNewContainerForId(id, parentContainer);

		containers.push(container);

		return container;
	}

	@Override
	public void closeContainer()
	{
		if (containers.size() == 1)
		{
			throw new IllegalStateException("Root container cannot be closed, only previously created containers can be closed");
		}

		containers.pop();
	}

	private static RuleIrVariablesContainer createNewContainerForId(final String id, final RuleIrVariablesContainer parent)
	{
		final RuleIrVariablesContainer container = new RuleIrVariablesContainer();
		container.setName(id);
		container.setVariables(new HashMap<>());
		container.setChildren(new HashMap<>());

		if (parent != null)
		{
			parent.getChildren().put(id, container);
			container.setParent(parent);

			final int parentPathLength = parent.getPath().length;
			final String[] path = Arrays.copyOf(parent.getPath(), parentPathLength + 1);
			path[parentPathLength] = id;

			container.setPath(path);
		}
		else
		{
			container.setPath(new String[0]);
		}

		return container;
	}

	@Override
	public String generateVariable(final Class<?> type)
	{
		final RuleIrVariablesContainer container = getCurrentContainer();
		RuleIrVariable variable = findVariable(container, type);

		if (variable == null)
		{
			final String variableName = generateVariableName(type);

			variable = new RuleIrVariable();
			variable.setName(variableName);
			variable.setType(type);
			variable.setPath(container.getPath());

			container.getVariables().put(variableName, variable);
		}

		return variable.getName();
	}

	protected RuleIrVariable findVariable(final RuleIrVariablesContainer container, final Class<?> type)
	{
		for (final RuleIrVariable variable : container.getVariables().values())
		{
			if (type.equals(variable.getType()))
			{
				return variable;
			}
		}

		if (container.getParent() != null)
		{
			return findVariable(container.getParent(), type);
		}
		else
		{
			return null;
		}
	}

	@Override
	public RuleIrLocalVariablesContainer createLocalContainer()
	{
		final RuleIrLocalVariablesContainer container = new RuleIrLocalVariablesContainer();
		container.setVariables(new HashMap<>());
		return container;
	}

	@Override
	public String generateLocalVariable(final RuleIrLocalVariablesContainer container, final Class<?> type)
	{
		final String variableName = generateVariableName(type);

		final RuleIrVariable variable = new RuleIrVariable();
		variable.setName(variableName);
		variable.setType(type);
		variable.setPath(new String[0]);

		container.getVariables().put(variableName, variable);

		return variableName;
	}

	@SuppressWarnings("unused")
	protected String generateVariableName(final Class<?> type)
	{
		count++;
		return DEFAULT_VARIABLE_PREFIX + count;
	}
}
