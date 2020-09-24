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
package de.hybris.platform.ruleengineservices.order.dao.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.daos.impl.DefaultOrderDao;
import de.hybris.platform.ruleengineservices.order.dao.ExtendedOrderDao;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * The class extends {@link DefaultOrderDao} and is a default implementation of {@link ExtendedOrderDao} interface.
 */

public class DefaultExtendedOrderDao extends DefaultOrderDao implements ExtendedOrderDao
{
	@Override
	public AbstractOrderModel findOrderByCode(final String code)
	{
		validateParameterNotNull(code, "Code must not be null");

		final AbstractOrderModel example = new AbstractOrderModel();
		example.setCode(code);
		final List<AbstractOrderModel> orders = getFlexibleSearchService().getModelsByExample(example);

		final AbstractOrderModel result;
		if (orders.isEmpty())
		{
			throw new ModelNotFoundException("Cannot find order/cart with code: " + code);
		}

		if (orders.size() == 1)
		{
			result = orders.get(0);
		}
		else
		{
			result = orders.stream().filter(this::isOrderModelOriginal).findFirst()
					.orElseThrow(() -> new ModelNotFoundException("Cannot find order/cart with code: " + code));
		}

		return result;
	}

	/**
	 * Defines if an {@link AbstractOrderModel} is a snapshot or an original order
	 *
	 * @param abstractOrderModel
	 * 		an instance of {@link AbstractOrderModel} to be checked
	 * @return true if the passed order is an original order otherwise false
	 */
	protected boolean isOrderModelOriginal(final AbstractOrderModel abstractOrderModel)
	{
		final boolean result;
		if (abstractOrderModel instanceof OrderModel)
		{
			final OrderModel orderModel = (OrderModel) abstractOrderModel;
			result = orderModel.getVersionID() == null;
		}
		else
		{
			result = false;
		}

		return result;
	}
}
