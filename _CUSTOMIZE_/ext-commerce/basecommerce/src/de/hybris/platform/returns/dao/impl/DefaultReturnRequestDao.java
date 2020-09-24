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
package de.hybris.platform.returns.dao.impl;

import static java.util.Collections.emptyList;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.returns.dao.ReturnRequestDao;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation for ReturnRequestDao.
 */
public class DefaultReturnRequestDao extends AbstractItemDao implements ReturnRequestDao
{

	/**
	 * Creates an "authorization" object (@link ReturnRequest} for the order to be returned, if there doesn't exists one
	 * for that order so far
	 *
	 * @param order
	 *           the order which should be returned
	 * @return ReturnRequest instance, which will deliver among others the RMA code of every processed "return order".
	 */
	@Override
	public ReturnRequestModel createReturnRequest(final OrderModel order)
	{
		final ReturnRequestModel request = getModelService().create(ReturnRequestModel.class);
		request.setOrder(order);
		getModelService().save(request);
		return request;
	}

	@Override
	public List<ReturnRequestModel> getReturnRequests(final String code)
	{
		final OrderModel order = getOrderByCode(code);
		if (order == null)
		{
			return emptyList();
		}

		final Map<String, Object> values = new HashMap<>();
		values.put("value", order);
		final String query = "SELECT {" + Item.PK + "} FROM {" + ReturnRequestModel._TYPECODE + "} WHERE { " + OrderModel._TYPECODE
				+ "} = ?value ORDER BY {" + Item.PK + "} ASC";
		final List<ReturnRequestModel> result = getFlexibleSearchService().<ReturnRequestModel> search(query, values).getResult();
		return result == null ? emptyList() : result;
	}

	/**
	 * Returns an order by its code
	 *
	 * @param code
	 *           the code of the order
	 * @return the order
	 */
	protected OrderModel getOrderByCode(final String code)
	{
		final Map<String, Object> values = new HashMap<>();
		values.put("value", code);
		final String query = "SELECT {" + Item.PK + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + AbstractOrder.CODE
				+ "} = ?value ORDER BY {" + Item.PK + "} ASC";
		final List<OrderModel> result = getFlexibleSearchService().<OrderModel> search(query, values).getResult();
		return result.isEmpty() ? null : result.iterator().next();
	}

}
