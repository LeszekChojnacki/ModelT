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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.provider.ExpressionEvaluator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * SPEL expression evaluator
 */
public class DefaultExpressionEvaluator implements ExpressionEvaluator, ApplicationContextAware
{
	private ExpressionParser parser;

	private ApplicationContext applicationContext;

	public ExpressionParser getParser()
	{
		return parser;
	}

	@Required
	public void setParser(final ExpressionParser parser)
	{
		this.parser = parser;
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

	@Override
	public Object evaluate(final String expression, final Object contex)
	{
		if (StringUtils.isNotEmpty(expression))
		{
			final Expression parsedExpression = parser.parseExpression(expression);
			final StandardEvaluationContext context = new StandardEvaluationContext(contex);
			context.setBeanResolver(new BeanFactoryResolver(applicationContext));

			return parsedExpression.getValue(context);
		}

		return null;
	}
}
