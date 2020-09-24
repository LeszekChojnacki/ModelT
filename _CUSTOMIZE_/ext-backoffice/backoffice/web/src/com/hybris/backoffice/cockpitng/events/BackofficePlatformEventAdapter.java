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
package com.hybris.backoffice.cockpitng.events;

import de.hybris.platform.servicelayer.event.EventSender;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.events.DefaultBackofficeEventSender;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.core.util.CockpitProperties;


/**
 * {@link EventSender} implementation in the cockpit domain. It wraps application event with the {@link CockpitEvent}
 * and publishes it in the {@link CockpitEventQueue}. The implementation has to be registered in the
 * {@link DefaultBackofficeEventSender}.
 *
 */
public class BackofficePlatformEventAdapter implements EventSender, InitializingBean
{
	private static final Logger LOG = Logger.getLogger(BackofficePlatformEventAdapter.class);

	private CockpitEventQueue cockpitEventQueue;
	private CockpitProperties cockpitProperties;

	private EventSender backofficeEventSender;

	@Override
	public void sendEvent(final AbstractEvent event)
	{
		forwardCockpitEvent(event);
	}

	@Override
	public void afterPropertiesSet()
	{
		if (backofficeEventSender instanceof DefaultBackofficeEventSender)
		{
			((DefaultBackofficeEventSender) backofficeEventSender).setBackofficeEventsAdapter(this);
		}
	}

	/**
	 * Creates new {@link DefaultCockpitEvent} based on the given application event. The new cockpit event's name is the
	 * <code>event</code>'s runtime class name. Publishes the event in the {@link CockpitEventQueue}.
	 *
	 * @param event
	 *           incoming application event.
	 */
	protected void forwardCockpitEvent(final AbstractEvent event)
	{
		if (Boolean.parseBoolean(getCockpitProperties().getProperty("cockpitng.globaleventtimer.enabled")))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Publishing cockpit event for : " + event.getClass().getName());
			}
			final CockpitEvent cockpitEvent = new DefaultCockpitEvent(event.getClass().getName(), event, event.getSource());
			getCockpitEventPublisher().publishEvent(cockpitEvent);
		}
	}

	protected CockpitEventQueue getCockpitEventPublisher()
	{
		return cockpitEventQueue;
	}

	@Required
	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}

	protected CockpitProperties getCockpitProperties()
	{
		return cockpitProperties;
	}

	@Required
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

	@Required
	public void setBackofficeEventSender(final EventSender backofficeEventSender)
	{
		this.backofficeEventSender = backofficeEventSender;
	}
}
