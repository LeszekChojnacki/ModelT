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

import static com.google.common.collect.Lists.newArrayList;

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
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
 * Default implementation of {@link RuleConditionsTranslator}.
 */
public class DefaultRuleConditionsTranslator implements RuleConditionsTranslator, ApplicationContextAware
{
	public static final String CONDITION_DEFINITIONS_ATTRIBUTE = "conditionDefinitions";
	public static final String MANDATORY_PARAMETER_VALIDATOR = "ruleRequiredParameterValidator";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;
	private ApplicationContext applicationContext;

	@Override
	public void validate(final RuleCompilerContext context, final List<RuleConditionData> conditions)
	{
		final Map<String, RuleConditionDefinitionData> conditionDefinitions = context.getConditionDefinitions();

		for (final RuleConditionData condition : conditions)
		{
			final RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(condition.getDefinitionId());
			if (conditionDefinition == null)
			{
				final RuleCompilerProblem problem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
						"rule.compiler.error.conditionstranslator.condition.definition.empty", new Object[]
						{ condition.getDefinitionId() });
				context.addProblem(problem);
			}
			else
			{
				validateParameters(context, conditionDefinition, condition.getParameters(), conditionDefinition.getParameters());

				final RuleConditionTranslator conditionTranslator = getConditionTranslator(conditionDefinition.getTranslatorId());
				if (conditionTranslator instanceof RuleConditionValidator)
				{
					((RuleConditionValidator) conditionTranslator).validate(context, condition, conditionDefinition);
				}
			}
		}
	}

	protected void validateParameters(final RuleCompilerContext context, final RuleConditionDefinitionData ruleDefinition,
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
	public synchronized List<RuleIrCondition> translate(final RuleCompilerContext context, final List<RuleConditionData> conditions)
	{
		final Map<String, RuleConditionDefinitionData> conditionDefinitions = context.getConditionDefinitions();
		final List<RuleIrCondition> ruleIrConditions = newArrayList();

		for (final RuleConditionData condition : conditions)
		{
			final RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(condition.getDefinitionId());
			if (conditionDefinition != null)
			{
				final RuleConditionTranslator conditionTranslator = getConditionTranslator(conditionDefinition.getTranslatorId());

				final RuleIrCondition ruleIrCondition = conditionTranslator.translate(context, condition, conditionDefinition);
				ruleIrConditions.add(ruleIrCondition);
			}
		}

		return ruleIrConditions;
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

	protected RuleConditionTranslator getConditionTranslator(final String translatorId)
	{
		try
		{
			return applicationContext.getBean(translatorId, RuleConditionTranslator.class);
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
