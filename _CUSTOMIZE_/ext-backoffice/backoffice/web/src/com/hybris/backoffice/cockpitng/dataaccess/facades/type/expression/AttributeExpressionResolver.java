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
package com.hybris.backoffice.cockpitng.dataaccess.facades.type.expression;

import java.util.Map;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.TypedValue;

import com.hybris.cockpitng.core.expression.EvaluationContextFactory;
import com.hybris.cockpitng.core.expression.impl.DefaultExpressionResolver;

/**
 *
 *
 */
public class AttributeExpressionResolver extends DefaultExpressionResolver {

    private Object item;

    private String attribute;

    private Object value;

    public AttributeExpressionResolver(final EvaluationContextFactory contextFactory) {
        super(contextFactory);
    }

    @Override
    public <T> T getValue(final Object sourceObject, final String expression, final Map<String, Object> variables) throws ExpressionException
	{
        final EvaluationContext context = this.getContextFactory().createContext(variables);
        final Object result = this.getExpressionParser().parseExpression(expression).getValue(context, sourceObject);
        if (context instanceof AttributeEvaluationContext) {
            final AttributeEvaluationContext attributeEvaluationContext = (AttributeEvaluationContext) context;
            item = attributeEvaluationContext.getItem();
            attribute = attributeEvaluationContext.getAttribute();
            value = attributeEvaluationContext.getValue();
        }
        return (T)result;
    }

    /**
     *
     * @return item, which property was last read
     */
    public Object getItem() {
        return item;
    }

    /**
     *
     * @return attribute of item, which value was last read
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     *
     * @return value of last read property
     */
    public Object getValue() {
        return (value instanceof TypedValue) ? ((TypedValue) value).getValue() : value;
    }

}
