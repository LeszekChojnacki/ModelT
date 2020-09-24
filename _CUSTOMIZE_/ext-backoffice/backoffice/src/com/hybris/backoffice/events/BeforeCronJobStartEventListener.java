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

import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.servicelayer.event.events.AbstractCronJobPerformEvent;
import de.hybris.platform.servicelayer.event.events.BeforeCronJobStartEvent;

import com.hybris.backoffice.events.processes.ProcessStartEvent;


/**
 * Event listener for cron jobs. If start cron job is included in backoffice processes then event is send to entire
 * cluster with this information @{link {@link ProcessStartEvent}}
 */
public class BeforeCronJobStartEventListener extends AbstractBackofficeCronJobEventListener<BeforeCronJobStartEvent>
{

	@Override
	protected void onEvent(final BeforeCronJobStartEvent event)
	{
		if (isProcessUpdateEvent(event))
		{
			getEventService().publishEvent(new ProcessStartEvent(event));
		}
	}

	/**
	 * @deprecated since 6.6, please use the {@link #isProcessUpdateEvent(AbstractCronJobPerformEvent)} instead.
	 */
	@Deprecated
	protected boolean isSyncJob(final BeforeCronJobStartEvent event)
	{
		return typesMatch(event.getJobType(), SyncItemJobModel._TYPECODE);
	}

}
