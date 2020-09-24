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
package com.hybris.backoffice.widgets.synctracker;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.cronjob.CronJobHistoryFacade;
import com.hybris.backoffice.events.processes.ProcessFinishedEvent;
import com.hybris.backoffice.sync.SyncTaskExecutionInfo;
import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.UITools;


public class SyncTrackerController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(SyncTrackerController.class);
	protected static final String SOCKET_IN_SYNC_TASK = "syncTaskExecutionInfo";
	protected static final String SOCKET_OUT_SYNCED_ITEMS = "synchronizedItems";
	protected static final String MODEL_TRACKED_SYNCHRONIZATIONS = "trackedSynchronizations";
	protected static final String SETTING_FIND_SYNC_COUNTERPARTS = "findSyncCounterparts";
	protected static final String SETTING_SEND_GLOBAL_EVENT = "sendGlobalEvent";
	@WireVariable
	private transient ObjectFacade objectFacade;
	@WireVariable
	private transient CockpitEventQueue cockpitEventQueue;
	@WireVariable
	private transient CronJobHistoryFacade cronJobHistoryFacade;
	@WireVariable
	private transient SynchronizationFacade synchronizationFacade;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		if (isTrackingProcesses())
		{
			UITools.postponeExecution(comp, this::updateTrackedProcesses);
		}
	}

	protected void updateTrackedProcesses()
	{
		getCronJobHistoryFacade().getCronJobHistory(new ArrayList<>(getTrackingMap().keySet())).stream()
				.filter(cjh -> cjh.getEndTime() != null).forEach(cjh -> finishTracking(cjh.getCronJobCode()));
	}

	@SocketEvent(socketId = SOCKET_IN_SYNC_TASK)
	public void onSyncStarted(final SyncTaskExecutionInfo executionInfo)
	{
		if (executionInfo != null && executionInfo.getSyncTask() != null
				&& StringUtils.isNotBlank(executionInfo.getSyncCronJobCode()))
		{
			startTrackingSynchronization(executionInfo.getSyncCronJobCode(),
					withCounterparts(executionInfo.getSyncTask().getItems(), executionInfo.getSyncTask().getSyncItemJob()));
		}
	}

	protected List<? extends ItemModel> withCounterparts(final List<? extends ItemModel> items, final SyncItemJobModel syncItemJob)
	{
		if (CollectionUtils.isEmpty(items) || !getWidgetSettings().getBoolean(SETTING_FIND_SYNC_COUNTERPARTS))
		{
			return items;
		}

		final List<ItemModel> itemsWithCounterParts = new ArrayList<>(items);
		items.forEach(item -> findCounterpart(item, syncItemJob).ifPresent(itemsWithCounterParts::add));
		return itemsWithCounterParts;
	}

	protected Optional<ItemModel> findCounterpart(final ItemModel item, final SyncItemJobModel syncItemJob)
	{
		if (item instanceof CatalogVersionModel)
		{
			if (Objects.equals(item, syncItemJob.getTargetVersion()))
			{
				return Optional.ofNullable(syncItemJob.getSourceVersion());
			}
			else if (Objects.equals(item, syncItemJob.getSourceVersion()))
			{
				return Optional.ofNullable(syncItemJob.getTargetVersion());
			}
		}
		else
		{
			return getSynchronizationFacade().findSyncCounterpart(item, syncItemJob);
		}

		return Optional.empty();
	}

	@GlobalCockpitEvent(eventName = ProcessFinishedEvent.EVENT_NAME, scope = CockpitEvent.APPLICATION)
	public void onProcessFinished(final CockpitEvent cockpitEvent)
	{
		if (isTrackingProcesses() && cockpitEvent.getData() instanceof ProcessFinishedEvent)
		{
			final AfterCronJobFinishedEvent processEvent = ((ProcessFinishedEvent) cockpitEvent.getData()).getProcessEvent();
			if (isTrackedProcess(processEvent.getCronJob()))
			{
				finishTracking(processEvent.getCronJob());
			}
		}
	}

	protected void finishTracking(final String cronJobCode)
	{
		notifySyncFinished(cronJobCode);
		stopTrackingProcess(cronJobCode);
	}

	protected void notifySyncFinished(final String cronJobCode)
	{
		final Set<String> syncedItems = getTrackingMap().get(cronJobCode);
		if (CollectionUtils.isNotEmpty(syncedItems))
		{
			final List<Object> items = loadSyncedItems(syncedItems);
			sendOutput(SOCKET_OUT_SYNCED_ITEMS, items);
			if (getWidgetSettings().getBoolean(SETTING_SEND_GLOBAL_EVENT))
			{
				getCockpitEventQueue().publishEvent(new DefaultCockpitEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, items, null));
			}
		}
	}

	protected boolean isTrackingProcesses()
	{
		return !getTrackingMap().isEmpty();
	}

	protected boolean isTrackedProcess(final String cronJobCode)
	{
		return StringUtils.isNotBlank(cronJobCode) && getTrackingMap().containsKey(cronJobCode);
	}

	protected Set<String> stopTrackingProcess(final String cronJobCode)
	{
		return getTrackingMap().remove(cronJobCode);
	}

	protected void startTrackingSynchronization(final String syncJobCode, final List<? extends ItemModel> items)
	{
		if (StringUtils.isNotBlank(syncJobCode) && CollectionUtils.isNotEmpty(items))
		{
			final Set<String> pks = items.stream().map(item -> item.getPk().toString()).collect(Collectors.toSet());
			getTrackingMap().put(syncJobCode, pks);
		}
	}

	protected List<Object> loadSyncedItems(final Set<String> syncedItems)
	{
		return syncedItems.stream().map(pk -> {
			try
			{
				return getObjectFacade().load(pk);
			}
			catch (ObjectNotFoundException e)
			{
				LOG.error("Cannot load items for PK " + pk, e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	protected Map<String, Set<String>> getTrackingMap()
	{
		Map trackedSynchronizations = getValue(MODEL_TRACKED_SYNCHRONIZATIONS, Map.class);
		if (trackedSynchronizations == null)
		{
			trackedSynchronizations = new HashMap<>();
			setValue(MODEL_TRACKED_SYNCHRONIZATIONS, trackedSynchronizations);
		}
		return trackedSynchronizations;
	}


	public CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	public CronJobHistoryFacade getCronJobHistoryFacade()
	{
		return cronJobHistoryFacade;
	}

	public SynchronizationFacade getSynchronizationFacade()
	{
		return synchronizationFacade;
	}
}
