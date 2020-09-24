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
package de.hybris.platform.warehousing.cancellation.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;


/**
 * Implementation of the {@link OmsOrderCancelNotificationServiceAdapter} to send notifications after an order has been
 * cancelled (finished and pending)
 */
public class OmsOrderCancelNotificationServiceAdapter implements OrderCancelNotificationServiceAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger(OmsOrderCancelNotificationServiceAdapter.class);

	@Override
	public void sendCancelFinishedNotifications(OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		LOG.info("Send cancel finished notification for cancelRequestRecordEntry: {}", cancelRequestRecordEntry.getCode());
	}

	@Override
	public void sendCancelPendingNotifications(OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		LOG.info("Send cancel pending notification for cancelRequestRecordEntry: {}", cancelRequestRecordEntry.getCode());
	}
}
