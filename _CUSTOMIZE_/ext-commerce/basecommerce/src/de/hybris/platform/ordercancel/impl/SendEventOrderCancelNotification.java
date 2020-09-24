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

import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;
import de.hybris.platform.ordercancel.events.CancelFinishedEvent;
import de.hybris.platform.ordercancel.events.CancelPendingEvent;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.event.EventService;


/**
 * Sends events ({@link CancelFinishedEvent} and {@link CancelPendingEvent}) while notifications.
 */
public class SendEventOrderCancelNotification implements OrderCancelNotificationServiceAdapter
{
	private EventService eventService;

	@Override
	public void sendCancelFinishedNotifications(final OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		eventService.publishEvent(new CancelFinishedEvent(cancelRequestRecordEntry));

	}

	@Override
	public void sendCancelPendingNotifications(final OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		eventService.publishEvent(new CancelPendingEvent(cancelRequestRecordEntry));

	}


	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}
}
