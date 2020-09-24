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

import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;


/**
 * Event which informs that cron job which run synchronization has finished.
 */
public class ProcessFinishedEvent extends AbstractProcessEvent<AfterCronJobFinishedEvent>
{
	public static final String EVENT_NAME = "com.hybris.backoffice.events.processes.ProcessFinishedEvent";
	public ProcessFinishedEvent(final AfterCronJobFinishedEvent syncEvent)
	{
		super(syncEvent);
	}
}
