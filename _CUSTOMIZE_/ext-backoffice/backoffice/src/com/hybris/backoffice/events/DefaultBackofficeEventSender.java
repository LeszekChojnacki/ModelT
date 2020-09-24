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
package com.hybris.backoffice.events;

import de.hybris.platform.servicelayer.event.EventSender;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import org.apache.log4j.Logger;


/**
 * Forwards application event to another {@link EventSender} implementation that is registered in the web context
 * (cockpit domain). The event sender from web context must register itself in <code>THIS</code> bean.
 */
public class DefaultBackofficeEventSender implements EventSender
{

	private static final Logger LOG = Logger.getLogger(DefaultBackofficeEventSender.class);
	private EventSender backofficeEventsAdapter;

	@Override
	public void sendEvent(final AbstractEvent event)
	{
		if (backofficeEventsAdapter == null)
		{
			if(LOG.isDebugEnabled())
			{
				LOG.debug(EventSender.class.getName() + " is not registered");
			}
			return;
		}
		backofficeEventsAdapter.sendEvent(event);
	}

	/**
	 * @param backofficeEventsAdapter
	 *           the backofficeEventsAdapter to set
	 */
	public void setBackofficeEventsAdapter(final EventSender backofficeEventsAdapter)
	{
		this.backofficeEventsAdapter = backofficeEventsAdapter;
	}
}
