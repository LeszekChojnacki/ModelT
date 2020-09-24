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
package de.hybris.platform.ruledefinitions.validation;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Objects.isNull;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class RulePositiveNumberParameterValidator implements RuleParameterValidator
{
	protected static final String MESSAGE_KEY = "rule.validation.error.negative.quantity";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public void validate(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition,
			final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition)
	{
		final Object parameterValue = parameter.getValue();

		if (parameterValue instanceof Number)
		{
			final Number number = parameter.getValue();

			validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
		}
		else if (parameterValue instanceof Collection<?>)
		{
			validatePositiveCollectionValue(context, ruleDefinition, parameter, parameterDefinition);
		}
		else if (parameterValue instanceof Map<?, ?>)
		{
			validatePositiveMapValue(context, ruleDefinition, parameter, parameterDefinition);
		}
	}

	protected void validatePositiveNumber(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition,
			final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition, final Number number)
	{
		if (checkIsNegativeNumber(number))
		{
			context.addProblem(ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR, MESSAGE_KEY,
					parameter, parameterDefinition, parameterDefinition.getName(), parameter.getUuid(), ruleDefinition.getName()));
		}
	}

	protected void validatePositiveMapValue(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition,
			final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition)
	{
		final Map<?, Number> mapValue = parameter.getValue();

		for (final Map.Entry<?, Number> entry : mapValue.entrySet())
		{
			final Number number = entry.getValue();

			validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
		}
	}

	protected void validatePositiveCollectionValue(final RuleCompilerContext context,
			final AbstractRuleDefinitionData ruleDefinition, final RuleParameterData parameter,
			final RuleParameterDefinitionData parameterDefinition)
	{
		final Collection<Number> collectionValue = parameter.getValue();

		for (final Number number : collectionValue)
		{
			validatePositiveNumber(context, ruleDefinition, parameter, parameterDefinition, number);
		}
	}

	protected boolean checkIsNegativeNumber(final Number number)
	{
		if (isNull(number) || !(number instanceof Comparable))
		{
			return true;
		}
		return ((Comparable) number).compareTo(ZeroNumberFactory.newInstance(number.getClass())) < 0;
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

	protected static class ZeroNumberFactory
	{
		protected ZeroNumberFactory()
		{
		}

		protected static Number newInstance(final Class<? extends Number> clazz)
		{
			validateParameterNotNullStandardMessage("Valid class must be provided here", clazz);

			try
			{
				return clazz.getConstructor(String.class).newInstance("0");
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				throw new IllegalStateException("Cannot instantiate the class " + clazz.getName(), e);
			}
		}
	}
}
