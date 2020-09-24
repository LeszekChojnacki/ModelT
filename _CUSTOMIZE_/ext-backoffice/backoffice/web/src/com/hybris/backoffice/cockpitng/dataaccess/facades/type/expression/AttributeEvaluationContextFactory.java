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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.expression.EvaluationContext;

import com.hybris.cockpitng.core.expression.EvaluationContextFactory;

/**
 *
 * Factory for {@link AttributeEvaluationContext}
 */
public class AttributeEvaluationContextFactory implements EvaluationContextFactory {

    private EvaluationContextFactory contextFactory;

    protected EvaluationContextFactory getContextFactory() {
        return contextFactory;
    }

    @Required
    public void setContextFactory(final EvaluationContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public EvaluationContext createContext() {
        return new AttributeEvaluationContext(getContextFactory().createContext());
    }

    @Override
    public EvaluationContext createContext(final Map<String, Object> variables) {
        return new AttributeEvaluationContext(getContextFactory().createContext(variables));
    }

    @Override
    public EvaluationContext createContext(final Object rootObject, final Map<String, Object> contextParameters)
    {
        return new AttributeEvaluationContext(getContextFactory().createContext(rootObject,contextParameters));
    }
}
