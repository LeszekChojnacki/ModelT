/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.accessors.impl;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;


/**
 * Property Accessor concrete implementation to access "carts" attribute in CustomerModel
 *
 */
public class CustomerCartsPropertyAccessor implements PropertyAccessor
{
	private static final String CARTS_ATTR = "carts";
	private static final String SAVED_CARTS_ATTR = "savedCarts";

	@Override
	public Class<?>[] getSpecificTargetClasses()
	{
		final Class<?>[] classes =
		{ CustomerModel.class };

		return classes;
	}

	@Override
	public boolean canRead(final EvaluationContext evaluationContext, final Object currentObject, final String attribute)
			throws AccessException
	{
		return isTypeSupported(currentObject)
				&& (attribute.equalsIgnoreCase(CARTS_ATTR) || attribute.equalsIgnoreCase(SAVED_CARTS_ATTR));
	}

	protected boolean isTypeSupported(final Object object)
	{
		return object instanceof CustomerModel;
	}

	@Override
	public TypedValue read(final EvaluationContext evaluationContext, final Object target, final String attribute)
			throws AccessException
	{
		final CustomerModel currentCustomer = (CustomerModel) target;

		final Collection<CartModel> carts = currentCustomer.getCarts();

		if (null != carts)
		{
			if (attribute.equalsIgnoreCase(CARTS_ATTR))
			{
				return new TypedValue(
						carts.stream().filter(i -> null == i.getSaveTime() && !i.getEntries().isEmpty()).collect(Collectors.toList()));
			}
			else if (attribute.equalsIgnoreCase(SAVED_CARTS_ATTR))
			{
				return new TypedValue(carts.stream().filter(i -> null != i.getSaveTime()).collect(Collectors.toList()));
			}
		}
		return new TypedValue(carts);
	}

	@Override
	public boolean canWrite(final EvaluationContext evaluationContext, final Object currentObject, final String attribute)
			throws AccessException
	{
		return false;
	}

	@Override
	public void write(final EvaluationContext evaluationContext, final Object target, final String attributeName,
			final Object attributeValue) throws AccessException
	{
		//left empty intentionally
	}
}
