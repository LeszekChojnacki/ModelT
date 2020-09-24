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
package de.hybris.platform.ordercancel;


/**
 * This interface is used by Warehouse adapter to provide feedback information about execution of cancel request.
 */
public interface OrderCancelCallbackService
{
	/**
	 * Callback method used by the Warehouse adapter to pass cancel operation result. Warehouse adapter uses this method
	 * to provide feedback information how was the Order canceled (completely, partially, not at all).
	 *
	 * @param cancelResponse
	 * 		instance of {@link OrderCancelResponse}
	 * @throws OrderCancelException
	 * 		in the case of any error during the order cancellation
	 */
	void onOrderCancelResponse(OrderCancelResponse cancelResponse) throws OrderCancelException;
}
