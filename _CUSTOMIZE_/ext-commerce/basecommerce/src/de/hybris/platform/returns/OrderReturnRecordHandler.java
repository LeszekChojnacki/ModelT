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
package de.hybris.platform.returns;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;


/**
 * Handles creation (based on OrderCancelRequests), of order return records in database
 */
public interface OrderReturnRecordHandler
{
	/**
	 * Get the whole return record for the given order
	 *
	 * @param order
	 * 		target order
	 * @return {@link OrderReturnRecordModel}
	 */
	OrderReturnRecordModel getReturnRecord(OrderModel order);

	/**
	 * Persist a new return record entry that corresponds to the return request.
	 *
	 * @param order
	 * 		{@link OrderModel}
	 * @param refunds
	 * 		{@link RefundEntryModel}
	 * @param description
	 * 		snapshot description
	 * @return {@link OrderReturnRecordEntryModel}
	 * @throws OrderReturnRecordsHandlerException
	 */
	public OrderReturnRecordEntryModel createRefundEntry(final OrderModel order, final List<RefundEntryModel> refunds,
			final String description) throws OrderReturnRecordsHandlerException;

	/**
	 * Updates the {@link OrderReturnRecordModel}, and the corresponding {@link OrderReturnRecordEntryModel}, after the {@link ReturnRequestModel} is finalized
	 *
	 * @param returnRequest
	 * 		the {@link ReturnRequestModel} being returned
	 * @return the updated {@link OrderReturnRecordModel}
	 */
	OrderReturnRecordModel finalizeOrderReturnRecordForReturnRequest(final ReturnRequestModel returnRequest);

	/**
	 * Get the {@link OrderReturnRecordEntryModel}, which is currently in progress state, for the given {@link ReturnRequestModel}
	 *
	 * @param returnRequest
	 * 		the {@link ReturnRequestModel} being returned
	 * @return the in progress {@link OrderReturnRecordEntryModel}
	 */
	OrderReturnRecordEntryModel getPendingReturnRecordEntryForReturnRequest(ReturnRequestModel returnRequest);

}
