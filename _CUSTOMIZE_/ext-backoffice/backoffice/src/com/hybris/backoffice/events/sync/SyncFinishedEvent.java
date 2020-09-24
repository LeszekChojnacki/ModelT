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

import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;


/**
 * @deprecated since 6.6, please use the {@link com.hybris.backoffice.events.processes.ProcessFinishedEvent } instead.
 */
@Deprecated
public class SyncFinishedEvent extends AbstractSyncEvent<AfterCronJobFinishedEvent>
{
	public static final String EVENT_NAME = "com.hybris.backoffice.events.sync.SyncFinishedEvent";

	public SyncFinishedEvent(final AfterCronJobFinishedEvent syncEvent)
	{
		super(syncEvent);
	}
}
