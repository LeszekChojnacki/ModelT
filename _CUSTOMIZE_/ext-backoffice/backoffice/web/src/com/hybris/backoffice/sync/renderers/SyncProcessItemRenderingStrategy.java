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
package com.hybris.backoffice.sync.renderers;

import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncCronJobHistoryModel;
import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncCronJobModel;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Strings;
import org.zkoss.util.resource.Labels;

import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.backoffice.widgets.processes.renderer.DefaultProcessItemRenderingStrategy;
import com.hybris.cockpitng.core.user.CockpitUserService;


public class SyncProcessItemRenderingStrategy extends DefaultProcessItemRenderingStrategy
{
	private static final String LABEL_PROCESSES_TITLE_SYNC_FULL = "processes.title.sync.full";
	private static final String LABEL_PROCESSES_TITLE_SYNC_ITEMS = "processes.title.sync.items";

	private CockpitUserService cockpitUserService;
	private SynchronizationFacade synchronizationFacade;
	private NotificationService notificationService;

	@Override
	public boolean canHandle(final CronJobHistoryModel cronJobHistory)
	{
		return cronJobHistory.getCronJob() != null && cronJobHistory.getCronJob().getJob() instanceof SyncItemJobModel;
	}

	@Override
	public boolean isRerunApplicable(final CronJobHistoryModel cronJobHistory)
	{
		if (isFailed(cronJobHistory))
		{
			final String userUid = cronJobHistory.getUserUid();
			return StringUtils.equals(cockpitUserService.getCurrentUser(), userUid) || cockpitUserService.isAdmin(userUid);
		}
		return false;
	}

	@Override
	public void rerunCronJob(final CronJobHistoryModel cronJobHistory)
	{
		try
		{
			synchronizationFacade.reRunCronJob((CatalogVersionSyncCronJobModel) cronJobHistory.getCronJob());
		}
		catch (final Exception e)
		{
			getNotificationService().notifyUser("processesWidget", NotificationEventTypes.EVENT_TYPE_GENERAL,
					NotificationEvent.Level.FAILURE, e);
		}
	}

	@Override
	public boolean isProgressSupported(final CronJobHistoryModel cronJobHistory)
	{
		return true;
	}

	@Override
	public String getTitle(final CronJobHistoryModel cronJobHistory)
	{
		if (cronJobHistory instanceof CatalogVersionSyncCronJobHistoryModel)
		{
			final CatalogVersionSyncCronJobHistoryModel catalogVersionSyncCronJobHistory = (CatalogVersionSyncCronJobHistoryModel) cronJobHistory;

			return catalogVersionSyncCronJobHistory.getFullSync() ? Labels.getLabel(LABEL_PROCESSES_TITLE_SYNC_FULL)
					: getItemsSyncLabel(catalogVersionSyncCronJobHistory);
		}
		return super.getTitle(cronJobHistory);
	}

	protected String getItemsSyncLabel(final CatalogVersionSyncCronJobHistoryModel catalogVersionSyncCronJobHistoryModel)
	{
		return Labels.getLabel(LABEL_PROCESSES_TITLE_SYNC_ITEMS, new String[]
		{ (catalogVersionSyncCronJobHistoryModel.getScheduledItemsCount() != null
				? String.format(" (%d)", catalogVersionSyncCronJobHistoryModel.getScheduledItemsCount())
				: Strings.EMPTY) });
	}

	public CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	@Required
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	public SynchronizationFacade getSynchronizationFacade()
	{
		return synchronizationFacade;
	}

	@Required
	public void setSynchronizationFacade(final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
