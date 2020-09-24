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

import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;


/**
 * This is the interface for executors of cancel requests. An executor performs all actions necessary to initialize
 * cancel operation. Typically it is either canceling an Order immediately, or putting it in CANCELLING state and
 * forwarding the request to the Warehouse for further processing. Requests executors does not deal with Warehouse
 * responses to requests; This is handled by {@link OrderCancelResponseExecutor}
 */
public interface OrderCancelRequestExecutor
{
	void processCancelRequest(OrderCancelRequest orderCancelRequest, OrderCancelRecordEntryModel cancelRequestRecordEntry)
			throws OrderCancelException;
}
