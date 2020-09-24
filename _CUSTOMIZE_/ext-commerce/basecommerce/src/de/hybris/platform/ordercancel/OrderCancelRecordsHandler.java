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

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.exceptions.OrderCancelRecordsHandlerException;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;


/**
 * Handles creation (based on OrderCancelRequests), updating (OrderCancelResponse) of order cancel records in database
 */
public interface OrderCancelRecordsHandler
{

	/**
	 * Get the whole cancel record for the given order
	 * 
	 * @param order
	 *           target order
	 * @return {@link OrderCancelRecordModel}
	 */
	OrderCancelRecordModel getCancelRecord(OrderModel order);

	/**
	 * Get the cancel record entry is currently in progress state
	 * 
	 * @param order
	 * @return {@link OrderCancelRecordEntryModel}
	 * @throws OrderCancelRecordsHandlerException
	 */
	OrderCancelRecordEntryModel getPendingCancelRecordEntry(OrderModel order) throws OrderCancelRecordsHandlerException;

	/**
	 * Persist a new cancel record entry that corresponds to the cancel request.
	 * 
	 * @param request
	 *           {@link OrderCancelRequest}
	 * @throws OrderCancelRecordsHandlerException
	 * @return {@link OrderCancelRecordEntryModel} - a model reference to the entry.
	 * 
	 */
	OrderCancelRecordEntryModel createRecordEntry(OrderCancelRequest request) throws OrderCancelRecordsHandlerException;

	/**
	 * Persist a new cancel record entry that corresponds to the cancel request.
	 * 
	 * @param request
	 *           {@link OrderCancelRequest}
	 * @param requestor
	 *           - who requests the cancel
	 * 
	 * @throws OrderCancelRecordsHandlerException
	 * @return {@link OrderCancelRecordEntryModel} - a model reference to the entry.
	 * 
	 */
	OrderCancelRecordEntryModel createRecordEntry(OrderCancelRequest request, PrincipalModel requestor)
			throws OrderCancelRecordsHandlerException;

	/**
	 * Updates cancel record entry basing on the cancel response. Entry should already be created earlier.
	 * 
	 * @param response
	 *           {@link OrderCancelResponse}
	 * @throws OrderCancelRecordsHandlerException
	 * @return {@link OrderCancelRecordEntryModel} - a model reference to the entry.
	 */
	OrderCancelRecordEntryModel updateRecordEntry(OrderCancelResponse response) throws OrderCancelRecordsHandlerException;


}
