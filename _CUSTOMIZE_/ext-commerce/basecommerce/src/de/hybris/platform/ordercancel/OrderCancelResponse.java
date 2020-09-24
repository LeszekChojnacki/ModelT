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

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;



/**
 * Represents Order Cancel responses. Instances of this class can represent:
 * <ul>
 * <li>Responses for canceling whole order (all order entries of an Order are discarded)</li>
 * <li>Responses for canceling only some of the order entries of an Order</li>
 * An order entry may be canceled completely (whole order entry is discarded) or partially (i.e. only order entry
 * quantity is reduced).
 *
 * </ul>
 */
public class OrderCancelResponse extends OrderCancelRequest
{

	public enum ResponseStatus
	{
		denied, full, partial, error // NOSONAR
	}

	private final ResponseStatus responseStatus;

	/**
	 * @param order
	 * @param orderCancelEntries
	 */
	public OrderCancelResponse(final OrderModel order, final List<OrderCancelEntry> orderCancelEntries)
	{
		super(order, orderCancelEntries);
		this.responseStatus = ResponseStatus.partial;
	}

	public OrderCancelResponse(final OrderModel order, final List<OrderCancelEntry> orderCancelEntries,
			final ResponseStatus status, final String statusMessage)
	{
		super(order, orderCancelEntries, statusMessage);
		this.responseStatus = status;
	}

	public OrderCancelResponse(final OrderModel order)
	{
		super(order);
		this.responseStatus = ResponseStatus.full;
	}

	public OrderCancelResponse(final OrderModel order, final ResponseStatus status, final String statusMessage)
	{
		super(order, CancelReason.NA, statusMessage);
		this.responseStatus = status;
	}

	public ResponseStatus getResponseStatus()
	{
		return responseStatus;
	}
}
