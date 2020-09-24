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

import static de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator.CONTAINER_PATH_SEPARATOR;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleActionsGenerator;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatter;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatterException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNoOpAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleExecutableAction;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultDroolsRuleActionsGenerator implements DroolsRuleActionsGenerator
{
	public static final int BUFFER_SIZE = 4096;

	private static final String EXECUTABLE_ACTION_METHOD = "executeAction";

	private static final String VARIABLES_PARAM = "variables";
	private static final String DROOLS_CONTEXT_PARAM = "kcontext";

	private DroolsRuleValueFormatter droolsRuleValueFormatter;

	private boolean useDeprecatedRRDsInActions = true;

	@Override
	public String generateActions(final DroolsRuleGeneratorContext context, final String indentation)
	{
		final StringBuilder actionsBuffer = new StringBuilder();

		generateVariables(context, indentation, actionsBuffer);

		List<RuleIrAction> actions = context.getRuleIr().getActions();
		if (CollectionUtils.isEmpty(actions))
		{
			actions = emptyList();
		}

		for (final RuleIrAction action : actions)
		{
			if (action instanceof RuleIrExecutableAction)
			{
				try
				{
					generateExecutableAction(context, (RuleIrExecutableAction) action, indentation, actionsBuffer);
				}
				catch (final DroolsRuleValueFormatterException e)
				{
					throw new RuleCompilerException(e);
				}
			}
			else if (!(action instanceof RuleIrNoOpAction))
			{
				throw new RuleCompilerException("Not supported RuleIrAction");
			}
		}
		if (isUseDeprecatedRRDsInActions())
		{
			actionsBuffer.append(indentation).append("$groupExecution.trackRuleGroupExecution($config);\n");
		}
		else
		{
			actionsBuffer.append(indentation).append("$executionTracker.trackRuleExecution(kcontext);\n");
		}
		return actionsBuffer.toString();
	}

	protected void generateVariables(final DroolsRuleGeneratorContext context, final String indentation,
			final StringBuilder actionsBuffer)
	{
		final String mapClassName = context.generateClassName(Map.class);
		actionsBuffer.append(indentation).append(mapClassName).append(' ').append(VARIABLES_PARAM).append(" = [\n");

		final String variableIndentation = indentation + context.getIndentationSize();
		final Map<String, RuleIrVariable> variables = context.getVariables();
		int remainingVariables = variables.size();

		for (final RuleIrVariable variable : variables.values())
		{
			final String variableClassName = variable.getType().getName();

			actionsBuffer.append(variableIndentation);
			actionsBuffer.append('\"');

			final String[] path = variable.getPath();
			if (path != null && path.length > 0)
			{
				for (final String groupId : variable.getPath())
				{
					actionsBuffer.append(groupId);
					actionsBuffer.append(CONTAINER_PATH_SEPARATOR);
				}
			}

			actionsBuffer.append(variableClassName);
			actionsBuffer.append("\" : ");
			actionsBuffer.append(context.getVariablePrefix());
			actionsBuffer.append(variable.getName());
			actionsBuffer.append("_set");

			if (remainingVariables > 1)
			{
				actionsBuffer.append(',');
			}

			actionsBuffer.append('\n');
			
			remainingVariables--;
		}

		actionsBuffer.append(indentation).append("];\n");
	}

	protected void generateExecutableAction(final DroolsRuleGeneratorContext context, final RuleIrExecutableAction ruleIrAction,
			final String indentation, final StringBuilder actionsBuffer)
	{
		context.addGlobal(ruleIrAction.getActionId(), RuleExecutableAction.class);

		final String actionContextClassName = context.generateClassName(DefaultDroolsRuleActionContext.class);

		actionsBuffer.append(indentation);
		actionsBuffer.append(ruleIrAction.getActionId()).append('.').append(EXECUTABLE_ACTION_METHOD);
		actionsBuffer.append('(');
		actionsBuffer.append("new ").append(actionContextClassName).append('(').append(VARIABLES_PARAM).append(", ")
				.append(DROOLS_CONTEXT_PARAM).append(')');
		actionsBuffer.append(", ");

		final Map<String, Object> actionParameters = ruleIrAction.getActionParameters();

		actionsBuffer.append(droolsRuleValueFormatter.formatValue(context, actionParameters));
		actionsBuffer.append(");\n");
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	protected boolean ruleIsStackable(final AbstractRuleModel rule)
	{
		return nonNull(rule) && rule.getStackable().booleanValue() && nonNull(rule.getRuleGroup())
				&& isNotBlank(rule.getRuleGroup().getCode());
	}

	public DroolsRuleValueFormatter getDroolsRuleValueFormatter()
	{
		return droolsRuleValueFormatter;
	}

	@Required
	public void setDroolsRuleValueFormatter(final DroolsRuleValueFormatter droolsRuleValueFormatter)
	{
		this.droolsRuleValueFormatter = droolsRuleValueFormatter;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	public void setUseDeprecatedRRDsInActions(final boolean useDeprecatedRRDsInActions)
	{
		this.useDeprecatedRRDsInActions = useDeprecatedRRDsInActions;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	protected boolean isUseDeprecatedRRDsInActions()
	{
		return useDeprecatedRRDsInActions;
	}
}
