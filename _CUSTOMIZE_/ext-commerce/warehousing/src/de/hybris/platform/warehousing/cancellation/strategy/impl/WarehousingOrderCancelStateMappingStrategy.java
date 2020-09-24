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
package de.hybris.platform.warehousing.cancellation.strategy.impl;


import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.basecommerce.enums.OrderCancelState;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelStateMappingStrategy;
import de.hybris.platform.ordercancel.impl.DefaultOrderCancelStateMappingStrategy;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;


/**
 * Default implementation of {@link OrderCancelStateMappingStrategy}
 * Does not return {@link OrderCancelState#CANCELIMPOSSIBLE} on {@link OrderStatus#CANCELLING} status as it is the one returned by OMS
 */
public class WarehousingOrderCancelStateMappingStrategy extends DefaultOrderCancelStateMappingStrategy implements
		OrderCancelStateMappingStrategy
{
	private List<ConsignmentStatus> confirmedConsignmentStatus;

	@Override
	public OrderCancelState getOrderCancelState(OrderModel order)
	{
		final OrderStatus orderStatus = order.getStatus();
		if (OrderStatus.CANCELLED.equals(orderStatus) || OrderStatus.COMPLETED.equals(orderStatus))
		{
			return OrderCancelState.CANCELIMPOSSIBLE;
		}
		final Collection<ConsignmentModel> consignments = order.getConsignments();
		if (consignments == null || consignments.isEmpty())
		{
			return OrderCancelState.PENDINGORHOLDINGAREA;
		}
		if (consignments.stream().allMatch(
				consignment -> consignment.getStatus().equals(ConsignmentStatus.READY)
						|| consignment.getStatus().equals(ConsignmentStatus.READY_FOR_PICKUP)
						|| consignment.getStatus().equals(ConsignmentStatus.READY_FOR_SHIPPING)
						|| consignment.getStatus().equals(ConsignmentStatus.CANCELLED)))
		{
			return OrderCancelState.SENTTOWAREHOUSE;
		}
		return checkConsignments(consignments);
	}


	@Override
	protected OrderCancelState checkConsignments(final Collection<ConsignmentModel> consignments)
	{
		boolean oneShipped = false;
		boolean allShipped = true;
		boolean allReady = true;
		final AbstractOrderModel orderModel = consignments.iterator().next().getOrder();
		final boolean isQtyUnallocated = orderModel.getEntries().stream()
				.anyMatch(entry -> ((OrderEntryModel) entry).getQuantityUnallocated() > 0);

		for (final ConsignmentModel consignmentModel : consignments)
		{
			final ConsignmentStatus status = consignmentModel.getStatus();
			if (getConfirmedConsignmentStatus().contains(status))
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

		if (allShipped && !isQtyUnallocated)
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

	protected List<ConsignmentStatus> getConfirmedConsignmentStatus()
	{
		return confirmedConsignmentStatus;
	}

	@Required
	public void setConfirmedConsignmentStatus(final List<ConsignmentStatus> confirmedConsignmentStatus)
	{
		this.confirmedConsignmentStatus = confirmedConsignmentStatus;
	}
}
