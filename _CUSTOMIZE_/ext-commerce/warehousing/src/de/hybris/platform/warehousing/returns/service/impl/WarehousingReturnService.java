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
package de.hybris.platform.warehousing.returns.service.impl;

import de.hybris.platform.basecommerce.enums.RefundReason;
import de.hybris.platform.basecommerce.enums.ReturnAction;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.impl.DefaultReturnService;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.math.BigDecimal;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default OMS implementation of ReturnService. It adds validation to the return creation and can modify the newly
 * created objects.
 */
public class WarehousingReturnService extends DefaultReturnService
{

	@Override
	public ReturnRequestModel createReturnRequest(final OrderModel order)
	{
		validateReturnRequest(order);
		final ReturnRequestModel returnRequest = super.createReturnRequest(order);
		finalizeReturnRequest(returnRequest);

		return returnRequest;
	}

	@Override
	public RefundEntryModel createRefund(final ReturnRequestModel request, final AbstractOrderEntryModel entry, final String notes,
			final Long expectedQuantity, final ReturnAction action, final RefundReason reason)
	{
		validateRefund(request, (OrderEntryModel) entry, expectedQuantity, action, reason);

		final RefundEntryModel refundEntry = getModelService().create(RefundEntryModel.class);
		refundEntry.setOrderEntry(entry);
		refundEntry.setAction(action);
		refundEntry.setNotes(notes);
		refundEntry.setReason(reason);
		refundEntry.setReturnRequest(request);
		refundEntry.setExpectedQuantity(expectedQuantity);
		refundEntry.setStatus(ReturnStatus.WAIT);
		getModelService().save(refundEntry);
		getModelService().save(request);

		boolean isInStore = refundEntry.getReturnRequest().getReturnEntries().stream()
				.allMatch(entryModel -> entryModel.getAction().equals(ReturnAction.IMMEDIATE));
		finalizeRefund(refundEntry, isInStore);

		return refundEntry;
	}

	/**
	 * Verifies the parameters of the createReturnRequest call
	 *
	 * @param order
	 * 		The order for which the return is created
	 */
	protected void validateReturnRequest(final OrderModel order)
	{
		validateParameterNotNull(order, "Parameter order cannot be null");
	}

	/**
	 * This updates {@link ReturnRequestModel} with {@link ReturnStatus} APPROVAL_PENDING
	 *
	 * @param returnRequest
	 * 		The newly created ReturnRequest
	 */
	protected void finalizeReturnRequest(final ReturnRequestModel returnRequest)
	{
		returnRequest.setStatus(ReturnStatus.APPROVAL_PENDING);
		createRMA(returnRequest); //This method includes saving the model
	}

	/**
	 * Verifies the parameters of the createRefund call
	 *
	 * @param request
	 * 		The ReturnRequest that will contain this RefundEntry
	 * @param entry
	 * 		The OrderEntry with the products to be refunded
	 * @param expectedQuantity
	 * 		The amount of items to be refunded
	 * @param action
	 * 		The action to take on this refund
	 * @param reason
	 * 		Reason code why the refund is requested
	 */
	protected void validateRefund(final ReturnRequestModel request, final OrderEntryModel entry, final Long expectedQuantity,
			final ReturnAction action, final RefundReason reason)
	{
		validateParameterNotNullStandardMessage("request", request);
		validateParameterNotNullStandardMessage("entry", entry);
		validateParameterNotNullStandardMessage("expectedQuantity", expectedQuantity);
		validateParameterNotNullStandardMessage("action", action);
		validateParameterNotNullStandardMessage("reason", reason);

		if (expectedQuantity.longValue() <= 0)
		{
			throw new IllegalArgumentException("Expected quantity must be above 0");
		}

		final OrderModel order = request.getOrder();

		if (!order.equals(entry.getOrder()))
		{
			throw new IllegalArgumentException("Order entry is not part of the order");
		}

		if (!isReturnable(order, entry, expectedQuantity.longValue()))
		{
			throw new IllegalArgumentException("Item is not returnable for this quantity");
		}
	}

	/**
	 * Handles whether or not the return is in store. For the in store return request, this method sets directly the
	 * {@value ReturnRequestModel#STATUS} and {@value de.hybris.platform.returns.model.ReturnEntryModel#STATUS} to {@link ReturnStatus#RECEIVED} and the received quantity to the expected quantity. For an online return
	 * request, the received quantity is set to 0 and the {@value ReturnRequestModel#STATUS} and {@value de.hybris.platform.returns.model.ReturnEntryModel#STATUS} is set to {@link ReturnStatus#APPROVAL_PENDING}
	 *
	 * @param refundEntry
	 * 		The newly created refundEntry
	 * @param isInStore
	 * 		indicates if the return request is in store or online
	 */
	protected void finalizeRefund(final RefundEntryModel refundEntry, final boolean isInStore)
	{
		if (isInStore)
		{
			refundEntry.getReturnRequest().setStatus(ReturnStatus.RECEIVED);
			refundEntry.setStatus(ReturnStatus.RECEIVED);
			refundEntry.setReceivedQuantity(refundEntry.getExpectedQuantity());
		}
		else
		{
			refundEntry.getReturnRequest().setStatus(ReturnStatus.APPROVAL_PENDING);
			refundEntry.setStatus(ReturnStatus.APPROVAL_PENDING);
			refundEntry.setReceivedQuantity(Long.valueOf(0L));
		}
		getModelService().save(refundEntry);
		getModelService().save(refundEntry.getReturnRequest());
	}

}
