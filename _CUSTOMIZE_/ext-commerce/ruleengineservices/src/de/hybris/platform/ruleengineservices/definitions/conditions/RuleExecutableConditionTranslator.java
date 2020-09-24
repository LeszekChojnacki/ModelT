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
package de.hybris.platform.ruleengineservices.definitions.conditions;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrEmptyCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableCondition;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleExecutableCondition;

import java.util.HashMap;
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


public class RuleExecutableConditionTranslator implements RuleConditionTranslator, RuleConditionValidator, ApplicationContextAware
{
	public static final String CONDITION_ID_PARAM = "conditionId";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;
	private ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleExecutableConditionTranslator.class);

	@Override
	public void validate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final String conditionId = conditionDefinition.getTranslatorParameters().get(CONDITION_ID_PARAM);
		if (StringUtils.isBlank(conditionId))
		{
			final RuleCompilerProblem ruleCompilerProblem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
					"rule.compiler.error.executablecondition.conditionid.empty");
			context.addProblem(ruleCompilerProblem);
		}

		RuleExecutableCondition ruleExecutableCondition = null;
		try
		{
			ruleExecutableCondition = getRuleExecutableCondition(conditionId);
		}
		catch (final RuleCompilerException e)
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Exception while compiling rule", e);
			}
			final RuleCompilerProblem ruleCompilerProblem = ruleCompilerProblemFactory.createProblem(Severity.ERROR,
					"rule.compiler.error.executablecondition.beanid.invalid");
			context.addProblem(ruleCompilerProblem);
		}

		if (ruleExecutableCondition instanceof RuleConditionValidator)
		{
			((RuleConditionValidator) ruleExecutableCondition).validate(context, condition, conditionDefinition);
		}
	}

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final String conditionId = conditionDefinition.getTranslatorParameters().get(CONDITION_ID_PARAM);
		if (conditionId == null)
		{
			return new RuleIrEmptyCondition();
		}

		Map<String, Object> conditionParameters = null;
		if (MapUtils.isNotEmpty(condition.getParameters()))
		{
			conditionParameters = new HashMap<>();
			for (final Entry<String, RuleParameterData> entry : condition.getParameters().entrySet())
			{
				final String parameterId = entry.getKey();
				final Object parameterValue = entry.getValue() == null ? null : entry.getValue().getValue();
				conditionParameters.put(parameterId, parameterValue);
			}
		}

		final RuleIrExecutableCondition irExecutableCondition = new RuleIrExecutableCondition();
		irExecutableCondition.setConditionId(conditionId);
		irExecutableCondition.setConditionParameters(conditionParameters);

		return irExecutableCondition;
	}

	protected RuleExecutableCondition getRuleExecutableCondition(final String conditionId)
	{
		try
		{
			return applicationContext.getBean(conditionId, RuleExecutableCondition.class);
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
