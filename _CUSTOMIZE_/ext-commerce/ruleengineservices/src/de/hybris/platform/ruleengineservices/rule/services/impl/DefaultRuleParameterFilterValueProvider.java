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
package de.hybris.platform.ruleengineservices.rule.services.impl;

import de.hybris.platform.ruleengineservices.rule.services.RuleParameterFilterValueProvider;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;


/**
 * Default implementation of @{@link RuleParameterFilterValueProvider}
 */
public class DefaultRuleParameterFilterValueProvider implements RuleParameterFilterValueProvider
{
	private static final String SEPARATOR = "#";

	private ExpressionParser parser;

	@Override
	public String getParameterId(final String value)
	{
		return substringBefore(value, SEPARATOR);
	}

	@Override
	public Object evaluate(final String value, final Object contextObject)
	{
		if (!value.contains(SEPARATOR))
		{
			return value;
		}

		final String expression = substringAfter(value, SEPARATOR);
		final Expression parsedExpression = getParser().parseExpression(expression);
		final StandardEvaluationContext evaluationContext = new StandardEvaluationContext(contextObject);
		return parsedExpression.getValue(evaluationContext);
	}

	protected ExpressionParser getParser()
	{
		return parser;
	}

	@Required
	public void setParser(final ExpressionParser parser)
	{
		this.parser = parser;
	}
}
