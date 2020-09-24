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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;




/**
 * {@link EvaluationContext} wrapper, that stores last read property during expression evaluation. Each
 * {@link PropertyAccessor} is wrapped with {@link AttributePropertyAccessor}.
 */
public class AttributeEvaluationContext implements EvaluationContext
{
	private final EvaluationContext context;

    private List<PropertyAccessor> accessors;

    private Object item;

    private String attribute;

    private Object value;

    public AttributeEvaluationContext(final EvaluationContext context) {
        this.context = context;
    }

    @Override
    public TypedValue getRootObject() {
        return context.getRootObject();
    }

    @Override
    public List<ConstructorResolver> getConstructorResolvers() {
        return context.getConstructorResolvers();
    }

    @Override
    public List<MethodResolver> getMethodResolvers() {
        return context.getMethodResolvers();
    }

    protected PropertyAccessor wrapPropertyAccessor(final PropertyAccessor accessor) {
        return new AttributePropertyAccessor(this, accessor);
    }

    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        if (accessors == null) {
            accessors = context.getPropertyAccessors().stream().map(accessor -> wrapPropertyAccessor(accessor)).collect(Collectors.toList());
        }
        return accessors;
    }

    @Override
    public TypeLocator getTypeLocator() {
        return context.getTypeLocator();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return context.getTypeConverter();
    }

    @Override
    public TypeComparator getTypeComparator() {
        return context.getTypeComparator();
    }

    @Override
    public OperatorOverloader getOperatorOverloader() {
        return context.getOperatorOverloader();
    }

    @Override
    public BeanResolver getBeanResolver() {
        return context.getBeanResolver();
    }

    @Override
    public void setVariable(final String key, final Object value) {
        context.setVariable(key, value);
    }

    @Override
    public Object lookupVariable(final String key) {
        return context.lookupVariable(key);
    }

    /**
     * Method stores information about new property read during expression evaluation.
     *
     * @param object object, which property is read
     * @param attribute qualifier of property
     * @param value value read
     */
    public void registerEvaluation(final Object object, final String attribute, final Object value) {
        this.item = object;
        this.attribute = attribute;
        this.value = value;
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
        return value instanceof TypedValue ? ((TypedValue) value).getValue() : value;
    }
}