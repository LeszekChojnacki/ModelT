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
package de.hybris.platform.ordercancel.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.basecommerce.enums.OrderCancelState;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelStateMappingStrategy;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

import java.util.Collection;


/**
 * Default impl
 * 
 */
public class DefaultOrderCancelStateMappingStrategy implements OrderCancelStateMappingStrategy
{
	/**
	 * ... still returns PENDINGORHOLDINGAREA
	 * 
	 * @param order
	 *           the order
	 * @return the state, will be PENDINGORHOLDINGAREA
	 */
	@Override
	public OrderCancelState getOrderCancelState(final OrderModel order)
	{
		final OrderStatus orderStatus = order.getStatus();
		if (OrderStatus.CANCELLED.equals(orderStatus) || OrderStatus.CANCELLING.equals(orderStatus)
				|| OrderStatus.COMPLETED.equals(orderStatus))
		{
			return OrderCancelState.CANCELIMPOSSIBLE;
		}
		final Collection<ConsignmentModel> consignments = order.getConsignments();
		if (consignments == null || consignments.isEmpty())
		{
			return OrderCancelState.PENDINGORHOLDINGAREA;
		}

		return checkConsignments(consignments);
	}

	/**
	 * 
	 */
	protected OrderCancelState checkConsignments(final Collection<ConsignmentModel> consignments)
	{
		boolean oneShipped = false;
		boolean allShipped = true;
		boolean allReady = true;
		for (final ConsignmentModel consignmentModel : consignments)
		{
			final ConsignmentStatus status = consignmentModel.getStatus();
			if (status.equals(ConsignmentStatus.SHIPPED))
			{
				oneShipped = true;
			}
			else
			{
				allShipped = false;
			}
			if (!status.equals(ConsignmentStatus.READY))
			{
				allReady = false;
			}
		}
		if (allShipped)
		{
			return OrderCancelState.CANCELIMPOSSIBLE;
		}
		else if (oneShipped)
		{
			return OrderCancelState.PARTIALLYSHIPPED;
		}
		else
		{
			if (allReady)
			{
				return OrderCancelState.SENTTOWAREHOUSE;
			}
			else
			{
				return OrderCancelState.SHIPPING;
			}
		}
	}
}