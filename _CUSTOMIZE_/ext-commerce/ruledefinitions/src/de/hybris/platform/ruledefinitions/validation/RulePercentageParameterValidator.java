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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Required;


public class RulePercentageParameterValidator implements RuleParameterValidator
{
	protected static final String MESSAGE_KEY = "rule.validation.error.percentage.invalid";

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public void validate(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition, final RuleParameterData parameter,
			final RuleParameterDefinitionData parameterDefinition)
	{
		final BigDecimal percentage = parameter.getValue();

		if (percentage != null && (percentage.doubleValue() <= 0 || percentage.doubleValue() > 100))
		{
			context.addProblem(ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR, MESSAGE_KEY, parameter, parameterDefinition,
					parameterDefinition.getName(), parameter.getUuid()));
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
}
