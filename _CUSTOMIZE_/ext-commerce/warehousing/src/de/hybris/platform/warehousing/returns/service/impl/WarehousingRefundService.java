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
package de.hybris.platform.warehousing.returns.service.impl;

import de.hybris.platform.basecommerce.enums.OrderEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.refund.RefundService;
import de.hybris.platform.refund.impl.DefaultRefundService;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.apache.commons.collections4.CollectionUtils;


/**
 * Warehousing implementation of {@link RefundService}<br/>
 */
public class WarehousingRefundService extends DefaultRefundService
{
	@Override
	public void apply(final OrderModel previewOrder, final ReturnRequestModel request) throws OrderReturnRecordsHandlerException
	{
		ServicesUtil.validateParameterNotNull(previewOrder, "Preview Order cannot be null");
		ServicesUtil.validateParameterNotNull(request, "Return Request cannot be null");
		ServicesUtil.validateParameterNotNull(request.getOrder(), "Order cannot be null inside Return request ");

		final OrderModel finalOrder = request.getOrder();
		final OrderReturnRecordEntryModel orderReturnRecordEntryModel = getModificationHandler()
				.createRefundEntry(finalOrder, getRefunds(request), "Refund request for order: " + finalOrder.getCode());
		orderReturnRecordEntryModel.setReturnRequest(request);
		getModelService().save(orderReturnRecordEntryModel);

		if (CollectionUtils.isNotEmpty(previewOrder.getEntries()))
		{
			previewOrder.getEntries().stream().forEach(previewEntry ->
			{
				final AbstractOrderEntryModel originalEntry = getEntry(finalOrder, previewEntry.getEntryNumber());
				final long newQuantity = previewEntry.getQuantity().longValue();

				originalEntry.setQuantity(Long.valueOf(newQuantity));
				originalEntry.setCalculated(Boolean.FALSE);

				if (newQuantity <= 0)
				{
					originalEntry.setQuantityStatus(OrderEntryStatus.DEAD);
				}

				getModelService().save(originalEntry);
			});
		}
	}

}
