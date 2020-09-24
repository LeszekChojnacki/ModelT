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
package de.hybris.platform.warehousing.returns.impl;

import de.hybris.platform.basecommerce.enums.OrderModificationEntryStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.impl.DefaultOrderReturnRecordsHandler;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;


/**
 * Warehousing implementation of {@link de.hybris.platform.returns.OrderReturnRecordHandler}.
 * It allows multiple {@link OrderReturnRecordEntryModel}(s) to stay in progress, while each {@link OrderReturnRecordEntryModel} corresponds to a {@link ReturnRequestModel}
 */
public class WarehousingOrderReturnRecordsHandler extends DefaultOrderReturnRecordsHandler
{

	@Override
	public OrderReturnRecordEntryModel createRefundEntry(final OrderModel order, final List<RefundEntryModel> refunds,
			final String description) throws OrderReturnRecordsHandlerException
	{
		final OrderHistoryEntryModel snapshot = createSnaphot(order, description);
		final OrderReturnRecordModel returnRecord = getOrCreateReturnRecord(order);
		returnRecord.setInProgress(true);
		getModelService().save(returnRecord);
		return createRefundRecordEntry(order, returnRecord, snapshot, refunds, null);
	}

	@Override
	protected void finalizeOrderReturnRecord(final OrderReturnRecordModel orderReturnRecord)
	{
		ServicesUtil.validateParameterNotNull(orderReturnRecord, "Order Return Record cannot be null");

		if (CollectionUtils.isNotEmpty(orderReturnRecord.getModificationRecordEntries()))
		{
			final boolean isAnyRecordEntryInProgress = orderReturnRecord.getModificationRecordEntries().stream().anyMatch(
					modificationRecordEntry -> OrderModificationEntryStatus.INPROGRESS.equals(modificationRecordEntry.getStatus()));
			orderReturnRecord.setInProgress(isAnyRecordEntryInProgress);
			getModelService().save(orderReturnRecord);
		}
	}

	@Override
	protected boolean isReturnRecordEntryForReturnRequest(final ReturnRequestModel returnRequest,
			final OrderReturnRecordEntryModel orderReturnRecordEntry)
	{
		ServicesUtil.validateParameterNotNull(orderReturnRecordEntry, "Order return record entry cannot be null");
		ServicesUtil.validateParameterNotNull(returnRequest, "Return request cannot be null");
		ServicesUtil.validateParameterNotNull(orderReturnRecordEntry.getReturnRequest(), "Return request cannot be null for OrderReturnRecordEntry");

		return OrderModificationEntryStatus.INPROGRESS.equals(orderReturnRecordEntry.getStatus()) && returnRequest.getCode()
				.equals(orderReturnRecordEntry.getReturnRequest().getCode());
	}

}
