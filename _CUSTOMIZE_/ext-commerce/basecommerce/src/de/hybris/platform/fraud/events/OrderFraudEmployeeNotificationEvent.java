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
package de.hybris.platform.fraud.events;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * This event is sent when some fraud symptoms were recognized in the order and it requires manual check by employee.
 * Register a listener to handle this event.
 */
public class OrderFraudEmployeeNotificationEvent extends AbstractEvent
{

	private final OrderModel order;

	public OrderFraudEmployeeNotificationEvent(final OrderModel order)
	{
		super();
		this.order = order;
	}

	/**
	 * @return the order
	 */
	public OrderModel getOrder()
	{
		return order;
	}
}
