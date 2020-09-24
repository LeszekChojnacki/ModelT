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
package com.hybris.backoffice.sync.updaters;

import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.hybris.backoffice.events.sync.SyncFinishedEvent;
import com.hybris.backoffice.widgets.processes.updater.ProcessesUpdater;
import com.hybris.cockpitng.core.events.CockpitEvent;


/**
 * @deprecated since 6.6, no longer used.
 */
@Deprecated
public class SyncFinishedUpdater implements ProcessesUpdater
{
	@Override
	public String getEventName()
	{
		return SyncFinishedEvent.EVENT_NAME;
	}

	@Override
	public List<String> onEvent(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getData() instanceof SyncFinishedEvent)
		{
			final AfterCronJobFinishedEvent syncEvent = ((SyncFinishedEvent) cockpitEvent.getData()).getSyncEvent();
			if (StringUtils.isNotBlank(syncEvent.getCronJob()))
			{
				return Lists.newArrayList(syncEvent.getCronJob());
			}
		}
		return Collections.emptyList();
	}

	@Override
	public String getEventScope()
	{
		return StringUtils.EMPTY;
	}
}
