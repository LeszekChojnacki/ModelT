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

import de.hybris.platform.servicelayer.event.events.BeforeCronJobStartEvent;


/**
 * Event which informs about sync cron job start
 */
public class ProcessStartEvent extends AbstractProcessEvent<BeforeCronJobStartEvent>
{
	public static final String EVENT_NAME = "com.hybris.backoffice.events.processes.ProcessStartEvent";

	public ProcessStartEvent(final BeforeCronJobStartEvent syncEvent)
	{
		super(syncEvent);
	}
}
