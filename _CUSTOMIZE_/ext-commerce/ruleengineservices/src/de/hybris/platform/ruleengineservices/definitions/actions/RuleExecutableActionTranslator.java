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
package de.hybris.platform.ruleengineservices.definitions.actions;

import de.hybris.platform.ruleengineservices.compiler.RuleActionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleActionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNoOpAction;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleExecutableAction;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Maps;


public class RuleExecutableActionTranslator implements RuleActionTranslator, RuleActionValidator, ApplicationContextAware
{
	public static final String ACTION_ID_PARAM = "actionId";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;
	private ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleExecutableActionTranslator.class);

	@Override
	public void validate(final RuleCompilerContext context, final RuleActionData action,
			final RuleActionDefinitionData actionDefinition)
	{
		final String actionId = actionDefinition.getTranslatorParameters().get(ACTION_ID_PARAM);
		if (StringUtils.isBlank(actionId))
		{
			final RuleCompilerProblem ruleCompilerProblem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
					"rule.compiler.error.executableaction.actionid.empty");
			context.addProblem(ruleCompilerProblem);
		}

		RuleExecutableAction ruleExecutableAction = null;
		try
		{
			ruleExecutableAction = getRuleExecutableAction(actionId);
		}
		catch (final RuleCompilerException e)
		{
			final RuleCompilerProblem ruleCompilerProblem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
					"rule.compiler.error.executableaction.beanid.invalid");
			context.addProblem(ruleCompilerProblem);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Rule Compiler Error Executable Action Bean Id Invalid", e);
			}
		}

		if (ruleExecutableAction instanceof RuleActionValidator)
		{
			((RuleActionValidator) ruleExecutableAction).validate(context, action, actionDefinition);
		}
	}

	@Override
	public RuleIrAction translate(final RuleCompilerContext context, final RuleActionData action,
			final RuleActionDefinitionData actionDefinition)
	{
		final String actionId = actionDefinition.getTranslatorParameters().get(ACTION_ID_PARAM);
		if (StringUtils.isBlank(actionId))
		{
			return new RuleIrNoOpAction();
		}

		Map<String, Object> actionParameters = Maps.newHashMap();
		if (MapUtils.isNotEmpty(action.getParameters()))
		{
			for (final Entry<String, RuleParameterData> entry : action.getParameters().entrySet())
			{
				final String parameterId = entry.getKey();
				if (entry.getValue() != null)
				{
					actionParameters.put(parameterId, entry.getValue().getValue());
					actionParameters.put(parameterId + "_uuid", entry.getValue().getUuid());
				}
			}
		}

		final RuleIrExecutableAction irExecutableAction = new RuleIrExecutableAction();
		irExecutableAction.setActionId(actionId);
		irExecutableAction.setActionParameters(actionParameters);

		return irExecutableAction;
	}

	protected RuleExecutableAction getRuleExecutableAction(final String actionId)
	{
		try
		{
			return applicationContext.getBean(actionId, RuleExecutableAction.class);
		}
		catch (final BeansException e)
		{
			throw new RuleCompilerException(e);
		}
	}

	public RuleCompilerProblemFactory getRuleCompilerProblemFactory()
	{
		return ruleCompilerProblemFactory;
	}

	@Required
	public void setRuleCompilerProblemFactory(final RuleCompilerProblemFactory ruleCompilerProblemFactory)
	{
		this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
	}

	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

}
