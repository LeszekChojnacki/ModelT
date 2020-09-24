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
package com.hybris.backoffice.events.sync;

import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * @deprecated since 6.6, please use the {@link com.hybris.backoffice.events.processes.AbstractProcessEvent } instead.
 */
@Deprecated
public class AbstractSyncEvent<T extends AbstractEvent> extends AbstractEvent implements ClusterAwareEvent
{
	private final T syncEvent;

	public AbstractSyncEvent(final T syncEvent)
	{
		this.syncEvent = syncEvent;
	}

	public T getSyncEvent()
	{
		return syncEvent;
	}

	@Override
	public boolean publish(final int sourceNodeId, final int targetNodeId)
	{
		return true;
	}
}
