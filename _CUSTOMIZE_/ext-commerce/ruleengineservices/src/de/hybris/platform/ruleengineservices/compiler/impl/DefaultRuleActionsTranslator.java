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

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.compiler.RuleActionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleActionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleActionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAction;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link RuleActionsTranslator}.
 */
public class DefaultRuleActionsTranslator implements RuleActionsTranslator, ApplicationContextAware
{

	public static final String ACTION_DEFINITIONS_ATTRIBUTE = "actionDefinitions";
	public static final String MANDATORY_PARAMETER_VALIDATOR = "ruleRequiredParameterValidator";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;
	private ApplicationContext applicationContext;

	@Override
	public void validate(final RuleCompilerContext context, final List<RuleActionData> actions)
	{
		final Map<String, RuleActionDefinitionData> actionDefinitions = context.getActionDefinitions();

		for (final RuleActionData action : actions)
		{
			final RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());
			if (actionDefinition == null)
			{
				final RuleCompilerProblem problem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
						"rule.compiler.error.actionstranslator.action.definition.empty", new Object[]
						{ action.getDefinitionId() });
				context.addProblem(problem);
			}
			else
			{
				validateParameters(context, actionDefinition, action.getParameters(), actionDefinition.getParameters());

				final RuleActionTranslator actionTranslator = getActionTranslator(actionDefinition.getTranslatorId());

				if (actionTranslator instanceof RuleActionValidator)
				{
					((RuleActionValidator) actionTranslator).validate(context, action, actionDefinition);
				}
			}
		}
	}

	protected void validateParameters(final RuleCompilerContext context, final RuleActionDefinitionData ruleDefinition,
			final Map<String, RuleParameterData> parameters, final Map<String, RuleParameterDefinitionData> parameterDefinitions)
	{
		for (final Map.Entry<String, RuleParameterData> entry : parameters.entrySet())
		{
			final String parameterId = entry.getKey();

			final RuleParameterDefinitionData parameterDefinition = parameterDefinitions.get(parameterId);

			final List<String> validatorIds = new ArrayList<>();
			validatorIds.add(MANDATORY_PARAMETER_VALIDATOR);

			validatorIds.addAll(parameterDefinition.getValidators());

			for (final String validatorId : validatorIds)
			{
				try
				{
					getParameterValidator(validatorId).validate(context, ruleDefinition, entry.getValue(), parameterDefinition);
				}
				catch (final RuleEngineServiceException e)
				{
					throw new RuleCompilerException(e);
				}
			}
		}
	}

	@Override
	public List<RuleIrAction> translate(final RuleCompilerContext context, final List<RuleActionData> actions)
	{
		final Map<String, RuleActionDefinitionData> actionDefinitions = context.getActionDefinitions();
		final List<RuleIrAction> ruleIrActions = new ArrayList<>();

		for (final RuleActionData action : actions)
		{
			final RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());
			if (actionDefinition != null)
			{
				final RuleActionTranslator actionTranslator = getActionTranslator(actionDefinition.getTranslatorId());

				final RuleIrAction ruleIrAction = actionTranslator.translate(context, action, actionDefinition);
				ruleIrActions.add(ruleIrAction);
			}
		}

		return ruleIrActions;
	}

	protected RuleParameterValidator getParameterValidator(final String validatorId)
	{
		try
		{
			return applicationContext.getBean(validatorId, RuleParameterValidator.class);
		}
		catch (final BeansException e)
		{
			throw new RuleCompilerException(e);
		}
	}

	protected RuleActionTranslator getActionTranslator(final String translatorId)
	{
		try
		{
			return applicationContext.getBean(translatorId, RuleActionTranslator.class);
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
