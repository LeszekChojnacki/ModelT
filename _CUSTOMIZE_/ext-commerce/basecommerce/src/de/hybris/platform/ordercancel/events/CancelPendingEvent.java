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
package de.hybris.platform.ordercancel.events;

import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 *
 */
public class CancelPendingEvent extends AbstractEvent
{
	private final OrderCancelRecordEntryModel cancelRequestRecordEntry;

	public CancelPendingEvent(final OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		super();
		this.cancelRequestRecordEntry = cancelRequestRecordEntry;
	}

	public OrderCancelRecordEntryModel getCancelRequestRecordEntry()
	{
		return cancelRequestRecordEntry;
	}
}
