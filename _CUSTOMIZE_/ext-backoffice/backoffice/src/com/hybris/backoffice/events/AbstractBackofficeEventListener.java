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


import de.hybris.platform.servicelayer.event.events.AbstractEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


/**
 * Abstract listener for platform events, has to be extended for every concrete event type
 */
public abstract class AbstractBackofficeEventListener<T extends AbstractEvent> extends AbstractEventListener<T>
{

	private static final Logger LOGGER = Logger.getLogger(AbstractBackofficeEventListener.class);

	private final Collection<ExternalEventCallback<T>> callbacks = new HashSet<>();


	public AbstractBackofficeEventListener()
	{
		super();
	}

	public void registerCallback(final ExternalEventCallback<T> callback)
	{
		if (callback != null)
		{
			callbacks.add(callback);
		}
		else
		{
			LOGGER.warn("Skipping registration, because callback is null.",
					new IllegalArgumentException("callback must not be null"));
		}
	}

	public void unregisterCallback(final ExternalEventCallback<T> callback)
	{
		callbacks.remove(callback);
	}

	public boolean isCallbackRegistered(final ExternalEventCallback<T> callback)
	{
		return callbacks.contains(callback);
	}

	@Override
	protected void onEvent(final T event)
	{
		for (final ExternalEventCallback<T> callback : callbacks)
		{
			callback.onEvent(event);
		}
	}
}
