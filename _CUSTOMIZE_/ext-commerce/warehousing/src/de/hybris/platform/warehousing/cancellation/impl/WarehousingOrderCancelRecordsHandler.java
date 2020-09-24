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
package de.hybris.platform.warehousing.cancellation.impl;

import de.hybris.platform.basecommerce.enums.OrderCancelEntryStatus;
import de.hybris.platform.basecommerce.enums.OrderModificationEntryStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.dao.OrderCancelDao;
import de.hybris.platform.ordercancel.exceptions.OrderCancelRecordsHandlerException;
import de.hybris.platform.ordercancel.impl.DefaultOrderCancelRecordsHandler;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordModel;
import org.springframework.beans.factory.annotation.Required;

/**
 * Warehousing implementation of OrderCancelRecordsHandler
 */
public class WarehousingOrderCancelRecordsHandler extends DefaultOrderCancelRecordsHandler
{
	@Override
	public OrderCancelRecordEntryModel updateRecordEntry(final OrderCancelResponse response)
			throws OrderCancelRecordsHandlerException
	{
		if (response == null)
		{
			throw new IllegalArgumentException("Cancel response cannot be null");
		}
		if (response.getOrder() == null)
		{
			throw new IllegalArgumentException("Cancel response contains no order reference");
		}
		final OrderModel order = response.getOrder();

		final OrderCancelRecordEntryModel currentEntry = getPendingCancelRecordEntry(order);
		switch (response.getResponseStatus())
		{
			case full:
				currentEntry.setCancelResult(OrderCancelEntryStatus.FULL);
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case partial:
				currentEntry.setCancelResult(OrderCancelEntryStatus.PARTIAL);
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case denied:
				currentEntry.setCancelResult(OrderCancelEntryStatus.DENIED);
				currentEntry.setRefusedMessage(response.getNotes());
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case error:
				currentEntry.setStatus(OrderModificationEntryStatus.FAILED);
				currentEntry.setFailedMessage(response.getNotes());
				break;

			default:
				break;
		}

		response.getEntriesToCancel().forEach(responseEntry ->
		{
			final OrderEntryCancelRecordEntryModel orderEntryRecord = getOrderCancelDao().getOrderEntryCancelRecord(
					(OrderEntryModel) responseEntry.getOrderEntry(), currentEntry);
			final Integer previousCancelledQty = orderEntryRecord.getCancelledQuantity() != null ? orderEntryRecord.getCancelledQuantity() : Integer.valueOf(0);
			final Integer newCancelledQty = Integer.valueOf(previousCancelledQty.intValue() + (int)responseEntry.getCancelQuantity());
			orderEntryRecord.setCancelledQuantity(newCancelledQty);
			getModelService().save(orderEntryRecord);
		});

		getModelService().save(currentEntry);

		final OrderModificationRecordModel record = currentEntry.getModificationRecord();
		record.setInProgress(false);
		getModelService().save(record);
		return currentEntry;
	}
}
