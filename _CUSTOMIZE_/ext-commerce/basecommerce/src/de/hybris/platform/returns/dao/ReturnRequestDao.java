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
package de.hybris.platform.returns.dao;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;


/**
 *
 *
 */
public interface ReturnRequestDao
{
	/**
	 * Creates an "return request" object (@link ReturnRequest} for the order to be returned.
	 * 
	 * @param order
	 *           the order which should be returned
	 * @return ReturnRequest instance, which contains among others the RMA code.
	 */
	ReturnRequestModel createReturnRequest(OrderModel order);

	/**
	 * Returns the "return request" for the specified order
	 * 
	 * @param orderCode
	 *           code of the order we ask for the "return request"
	 * @return "return request" of the order
	 */
	List<ReturnRequestModel> getReturnRequests(final String orderCode);
}
