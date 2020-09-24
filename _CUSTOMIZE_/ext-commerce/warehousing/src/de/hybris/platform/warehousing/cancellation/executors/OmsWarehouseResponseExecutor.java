/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.cancellation.executors;

import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.impl.executors.WarehouseResponseExecutor;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.warehousing.cancellation.ConsignmentCancellationService;
import de.hybris.platform.warehousing.cancellation.OmsOrderCancelService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Process the cancel response from an OMS warehouse
 */
public class OmsWarehouseResponseExecutor extends WarehouseResponseExecutor
{
	private static Logger LOGGER = LoggerFactory.getLogger(OmsWarehouseResponseExecutor.class);

	private OmsOrderCancelService omsOrderCancelService;
	private ConsignmentCancellationService consignmentCancellationService;

	@Override
	public void processCancelResponse(final OrderCancelResponse orderCancelResponse,
			final OrderCancelRecordEntryModel cancelRequestRecordEntry) throws OrderCancelException
	{
		LOGGER.info("Process cancel response from order {}", orderCancelResponse.getOrder().getCode());

		// Cancel first the unallocated quantities if existing
		final List<OrderCancelEntry> allocatedEntries = getOmsOrderCancelService().processOrderCancel(cancelRequestRecordEntry);
		if (!CollectionUtils.isEmpty(allocatedEntries))
		{
			final OrderCancelResponse updatedOrderCancelResponse = new OrderCancelResponse(orderCancelResponse.getOrder(),
					allocatedEntries);

			// Cancel order entries
			super.processCancelResponse(updatedOrderCancelResponse, cancelRequestRecordEntry);

			// Then process the cancellation of the consignments
			getConsignmentCancellationService().processConsignmentCancellation(updatedOrderCancelResponse);
		}
		else
		{
			super.processCancelResponse(createCancelResponseWithZeroQtyResponse(orderCancelResponse), cancelRequestRecordEntry);
		}
	}

	/**
	 * Creates the cloned {@link OrderCancelResponse}, with 0 cancelQuantity, from the given {@link OrderCancelResponse}
	 * @param orderCancelResponse
	 * 		the original orderCancelResponse
	 * @return
	 * 		the cloned orderCancelResponse with 0 cancelQuantity requested
	 */
	protected OrderCancelResponse createCancelResponseWithZeroQtyResponse(final OrderCancelResponse orderCancelResponse)
	{
		final List<OrderCancelEntry> orderCancelEntries = new ArrayList<>();
		orderCancelResponse.getEntriesToCancel().forEach(cancelEntry ->
		{
			final OrderCancelEntry orderCancelEntry = new OrderCancelEntry(cancelEntry.getOrderEntry(), Long.valueOf(0l),
					cancelEntry.getNotes(), cancelEntry.getCancelReason());
			orderCancelEntries.add(orderCancelEntry);
		});
		return new OrderCancelResponse(orderCancelResponse.getOrder(),orderCancelEntries,orderCancelResponse.getResponseStatus(),orderCancelResponse.getNotes());
	}

	protected OmsOrderCancelService getOmsOrderCancelService()
	{
		return omsOrderCancelService;
	}

	@Required
	public void setOmsOrderCancelService(final OmsOrderCancelService omsOrderCancelService)
	{
		this.omsOrderCancelService = omsOrderCancelService;
	}

	protected ConsignmentCancellationService getConsignmentCancellationService()
	{
		return consignmentCancellationService;
	}

	@Required
	public void setConsignmentCancellationService(final ConsignmentCancellationService consignmentCancellationService)
	{
		this.consignmentCancellationService = consignmentCancellationService;
	}
}
