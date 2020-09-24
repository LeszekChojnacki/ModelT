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
package de.hybris.platform.orderprocessing.events;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * Abstract base class for all OrderProcess events
 */
public class OrderProcessingEvent extends AbstractEvent
{
	private final OrderProcessModel process;
	private final OrderStatus orderStatus;

	public OrderProcessingEvent(final OrderProcessModel process)
	{
		this.process = process;

		// Extract the current order status
		if (process != null)
		{
			final OrderModel order = process.getOrder();
			orderStatus = order == null ? null : order.getStatus();
		}
		else
		{
			orderStatus = null;
		}
	}

	public OrderProcessModel getProcess()
	{
		return process;
	}

	public OrderStatus getOrderStatus()
	{
		return orderStatus;
	}
}
