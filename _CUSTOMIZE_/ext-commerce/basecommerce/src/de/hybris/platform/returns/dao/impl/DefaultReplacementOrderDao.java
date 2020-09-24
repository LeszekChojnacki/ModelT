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

import de.hybris.platform.basecommerce.enums.ReturnFulfillmentStatus;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.returns.dao.ReplacementOrderDao;
import de.hybris.platform.returns.impl.DefaultReturnService;
import de.hybris.platform.returns.jalo.ReturnRequest;
import de.hybris.platform.returns.model.ReplacementOrderModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Dao object used in in {@link DefaultReturnService}
 * 
 */
public class DefaultReplacementOrderDao extends AbstractItemDao implements ReplacementOrderDao
{

	/**
	 * Returns the {@link ReplacementOrderModel} by the specified 'RMA value'
	 * 
	 * @param rma
	 *           value
	 * @return replacement order
	 */
	@Override
	public ReplacementOrderModel getReplacementOrder(final String rma)
	{
		final Map<String, Object> params = new HashMap();
		params.put("rma", rma);

		final String query = "SELECT {" + ReturnRequest.ORDER + "} FROM { " + ReturnRequestModel._TYPECODE + "} WHERE { "
				+ ReturnRequest.RMA + "}=?rma ORDER BY {" + Item.PK + "} ASC";
		final List<ReplacementOrderModel> result = (List) getFlexibleSearchService().search(query, params).getResult();
		return result == null ? null : result.iterator().next();
	}

	/**
	 * Creates a {@link ReplacementOrderModel}
	 * 
	 * @param request
	 *           the return request to which the order will be assigned
	 * @return the Replacement Order'
	 */
	@Override
	public ReplacementOrderModel createReplacementOrder(final ReturnRequestModel request)
	{
		final ReplacementOrderModel replacementOrder = getModelService().create(ReplacementOrderModel.class);
		replacementOrder.setFulfilmentStatus(ReturnFulfillmentStatus.INITIAL);

		if (request != null)
		{
			if (request.getOrder() != null)
			{
				replacementOrder.setCurrency(request.getOrder().getCurrency());
				replacementOrder.setDate(request.getOrder().getDate());
				replacementOrder.setNet(request.getOrder().getNet());
				replacementOrder.setUser(request.getOrder().getUser());
				replacementOrder.setDeliveryAddress(request.getOrder().getDeliveryAddress());
			}
			request.setReplacementOrder(replacementOrder);
		}
		getModelService().save(replacementOrder);

		return replacementOrder;
	}

}
