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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.jalo.order.OrderEntry;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;

import java.util.Map;


/**
 * Order Cancel Service interface
 */
public interface OrderCancelService
{
	/**
	 * Return current configuration
	 *
	 * @return current configuration
	 */
	OrderCancelConfigModel getConfiguration();

	/**
	 * Gets Cancellation record for a given order.
	 *
	 * @param order
	 * 		instance of {@link OrderModel} to get the cancel record for
	 * @return Cancellation record for the given order
	 * @throws OrderCancelException
	 * 		in the case of any error during order record cancellation
	 */
	OrderCancelRecordModel getCancelRecordForOrder(OrderModel order) throws OrderCancelException;

	/**
	 * Gets cancellation record entry being currently in progress.
	 *
	 * @param order
	 * 		instance of {@link OrderModel} to get the cancel record for
	 * @return Cancellation record entry being currently in progress.
	 * @throws OrderCancelException
	 * 		- if more than one entry is being in progress
	 */
	OrderCancelRecordEntryModel getPendingCancelRecordEntry(OrderModel order) throws OrderCancelException;

	/**
	 * Verifies if order cancel is possible for given order and given conditions
	 *
	 * @param order
	 * 		order to be canceled.
	 * @param partialCancel
	 * 		if true, the methods verifies if partial cancel can be performed on the given order
	 * @param partialEntryCancel
	 * 		if true, the methods verifies if partial entry cancel can be performed on the given order. Partial entry
	 * 		cancel means that only part of order entry is canceled (i.e. only order entry quantity is reduced)
	 * @param requestor
	 * 		instance of {@link PrincipalModel}, containing the principal of a requestor
	 * @return instance of {@link CancelDecision} containing the decision details
	 */
	CancelDecision isCancelPossible(OrderModel order, PrincipalModel requestor, boolean partialCancel, boolean partialEntryCancel);

	/**
	 * Requests complete cancel operation on an Order. Depending on current state, order might be canceled immediately or
	 * the cancellation decision might be delayed until the response from Warehouse arrives. Returned cancellation record
	 * allows to get information about order cancel request.
	 *
	 * @param orderCancelRequest
	 * 		order cancel request instance
	 * @param requestor
	 * 		instance of {@link PrincipalModel}, containing the principal of a requestor
	 * @return OrderCancelRecordEntryModel that represents the request and the result of cancel operation.
	 * @throws OrderCancelException
	 * 		in the case the  cancellation of order is not allowed
	 */
	OrderCancelRecordEntryModel requestOrderCancel(OrderCancelRequest orderCancelRequest, PrincipalModel requestor)
			throws OrderCancelException;

	/**
	 * Returns all cancellable {@link OrderEntry}
	 *
	 * @param order
	 * 		Order that is subject to cancel
	 * @param requestor
	 * 		Principal that originates the request ("issuer of the request"). It might be different from current
	 * 		session user.
	 * @return the cancellable {@link AbstractOrderEntryModel} and their cancellable quantity
	 */
	Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(OrderModel order, PrincipalModel requestor);

}
