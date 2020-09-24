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
package com.hybris.backoffice.events.processes;

import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * Abstract event for sync events.
 */
public class AbstractProcessEvent<T extends AbstractEvent> extends AbstractEvent implements ClusterAwareEvent
{
	private final T processEvent;

	public AbstractProcessEvent(final T processEvent)
	{
		this.processEvent = processEvent;
	}

	public T getProcessEvent()
	{
		return processEvent;
	}

	@Override
	public boolean publish(final int sourceNodeId, final int targetNodeId)
	{
		return true;
	}
}
