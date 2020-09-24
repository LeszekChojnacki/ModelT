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
package com.hybris.backoffice.widgets.syncpopup;

import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;

import com.google.common.collect.Lists;
import com.hybris.backoffice.sync.SyncTask;
import com.hybris.backoffice.sync.SyncTaskExecutionInfo;
import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class SyncPopupController extends DefaultWidgetController
{
	public static final String SOCKET_STARTED_SYNC_CRON_JOB = "startedSyncCronJob";
	protected static final String SOCKET_IN_INPUT_OBJECT = "inputObject";
	protected static final String SOCKET_IN_INPUT_OBJECTS = "inputObjects";
	protected static final String SOCKET_OUT_EXECUTED_SYNC_TASK = "syncTaskExecutionInfo";
	protected static final String SOCKET_OUTPUT_CANCEL = "cancel";
	protected static final String SETTING_SEARCH_VISIBILITY_THRESHOLD = "searchVisibilityThreshold";
	protected static final String SETTING_LISTITEM_RENDERER = "listitemRenderer";
	protected static final String LABEL_TITLE_SINGLE = "title.single";
	protected static final String LABEL_TITLE_WINDOW = "title.window";
	protected static final String LABEL_TITLE_MANY = "title.many";
	protected static final String CANCEL_BUTTON_ID = "cancel";
	protected static final String SYNC_BUTTON_ID = "sync";
	protected static final String SEARCHBOX_ID = "searchbox";
	protected static final String PUSHLIST_ID = "pushList";
	protected static final String PULLLIST_ID = "pullList";
	protected static final String MODEL_SYNC_ITEMS = "modelSyncItems";

	private ListModelList<SyncPopupViewModel> pullListModel;
	private ListModelList<SyncPopupViewModel> pushListModel;

	@WireVariable
	protected transient LabelService labelService;
	@WireVariable
	protected transient SynchronizationFacade synchronizationFacade;
	@Wire
	private Listbox pullList;
	@Wire
	private Listbox pushList;
	@Wire
	private Tabbox tabbox;
	@Wire
	private Tab pushTab;
	@Wire
	private Tab pullTab;
	@Wire
	private Label title;
	@Wire
	private Button sync;
	@Wire
	private Textbox searchbox;
	@WireVariable
	private transient NotificationService notificationService;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		setWidgetTitle(getLabel(LABEL_TITLE_WINDOW));
		setupRenderers();
		initializeLists();
		final List<ItemModel> syncItems = getSyncItems();
		if (CollectionUtils.isNotEmpty(syncItems))
		{
			prepareView(syncItems);
		}
	}

	protected void initializeLists()
	{
		pullListModel = new ListModelList<>();
		pushListModel = new ListModelList<>();
		pullList.setModel(pullListModel);
		pushList.setModel(pushListModel);
	}

	protected void setupRenderers()
	{
		final String listItemRenderer = getWidgetSettings().getString(SETTING_LISTITEM_RENDERER);
		if (StringUtils.isNotBlank(listItemRenderer))
		{
			pullList.setItemRenderer(BackofficeSpringUtil.getBean(listItemRenderer, ListitemRenderer.class));
			pushList.setItemRenderer(BackofficeSpringUtil.getBean(listItemRenderer, ListitemRenderer.class));
		}
	}

	protected List<ItemModel> getSyncItems()
	{
		List<ItemModel> items = getValue(MODEL_SYNC_ITEMS, List.class);
		if (items == null)
		{
			items = new ArrayList<>();
			setValue(MODEL_SYNC_ITEMS, items);
		}
		return items;
	}

	protected void prepareView(final List<ItemModel> itemsToSync)
	{
		final SyncJobsLoader syncJobsLoader = createSyncDataLoader(itemsToSync);

		if (syncJobsLoader.getLoadingStatus().isOK())
		{
			loadPushJobs(syncJobsLoader.getPushJobs());
			loadPullJobs(syncJobsLoader.getPullJobs());
			adjustTitle(itemsToSync);
			setSearchboxVisibility();
			hideListIfEmpty();
		}
		else
		{
			clearLoadedJobs();
			closeSyncPopup();
			showWarningMessageBox("sync.cannot.perform", syncJobsLoader.getLoadingStatus().getMsgLabelKey());
		}
	}

	protected void loadPushJobs(final List<SyncItemJobModel> jobs)
	{
		pushListModel.clear();
		pushListModel.addAll(packModels(jobs, SyncJobType.PUSH));
	}

	protected void loadPullJobs(final List<SyncItemJobModel> jobs)
	{
		pullListModel.clear();
		pullListModel.addAll(packModels(jobs, SyncJobType.PULL));
	}

	protected SyncJobsLoader createSyncDataLoader(final List<ItemModel> itemsToSync)
	{
		return new SyncJobsLoader(itemsToSync);
	}

	protected void adjustTitle(final List<ItemModel> itemsToSync)
	{
		String titleText = null;
		String toolTip = null;
		if (CollectionUtils.isNotEmpty(itemsToSync))
		{
			if (itemsToSync.size() == 1)
			{
				titleText = getLabel(LABEL_TITLE_SINGLE, new Object[]
				{ getLabelService().getObjectLabel(itemsToSync.get(0)) });
				toolTip = titleText;
			}
			else
			{
				titleText = getLabel(LABEL_TITLE_MANY, new Object[]
				{ Integer.valueOf(itemsToSync.size()) });
			}
		}
		title.setValue(titleText);
		title.setTooltiptext(toolTip);
	}

	protected void setSearchboxVisibility()
	{
		final boolean showSearchbox = pullListModel.getSize() + pushListModel.getSize() >= getWidgetSettings()
				.getInt(SETTING_SEARCH_VISIBILITY_THRESHOLD);
		searchbox.setVisible(showSearchbox);
	}

	protected void showWarningMessageBox(final String titleKey, final String msgKey)
	{
		Messagebox.show(getLabel(msgKey), getLabel(titleKey), Messagebox.OK, Messagebox.EXCLAMATION);
	}

	protected void hideListIfEmpty()
	{
		final boolean pullListEmpty = pullListModel.isEmpty();
		final boolean pushListEmpty = pushListModel.isEmpty();
		pullTab.setVisible(!pullListEmpty);
		pushTab.setVisible(!pushListEmpty);
		tabbox.setSelectedTab(pullListEmpty ? pushTab : pullTab);
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = PUSHLIST_ID)
	public void onPushListSelect()
	{
		pullListModel.clearSelection();
		sync.setDisabled(pushListModel.getSelection().isEmpty());
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = PULLLIST_ID)
	public void onPullListSelect()
	{
		pushListModel.clearSelection();
		sync.setDisabled(pullListModel.getSelection().isEmpty());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = CANCEL_BUTTON_ID)
	public void closeSyncPopup()
	{
		sendOutput(SOCKET_OUTPUT_CANCEL, null);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = SYNC_BUTTON_ID)
	public void onSyncButtonClick()
	{
		final Optional<SyncItemJobModel> selectedSyncJob = getSelectedSyncJob();
		if (selectedSyncJob.isPresent())
		{

			final SyncTask syncTask = createSyncTask(getSyncItems(), selectedSyncJob.get());
			final Optional<String> cronJobCode = getSynchronizationFacade().performSynchronization(syncTask);
			if (cronJobCode.isPresent())
			{
				sendOutput(SOCKET_STARTED_SYNC_CRON_JOB, cronJobCode.get());
				sendOutput(SOCKET_OUT_EXECUTED_SYNC_TASK, new SyncTaskExecutionInfo(syncTask, cronJobCode.get()));
			}
			else
			{
				getNotificationService().notifyUser("syncPopup", "syncCannotRun", NotificationEvent.Level.FAILURE);
			}
		}
	}

	@ViewEvent(eventName = Events.ON_CHANGING, componentID = SEARCHBOX_ID)
	public void filterLists(final InputEvent event)
	{
		final Consumer<Listitem> listItemFilter = item -> item.setVisible(itemMatches(event.getValue(), item));
		pullList.getItems().forEach(listItemFilter);
		pushList.getItems().forEach(listItemFilter);
	}

	protected boolean itemMatches(final String value, final Listitem item)
	{
		for (final Component label : item.queryAll("label"))
		{
			if (StringUtils.containsIgnoreCase(((Label) label).getValue(), value))
			{
				return true;
			}
		}
		return false;
	}

	@SocketEvent(socketId = SOCKET_IN_INPUT_OBJECT)
	public void showSyncJobsForInputObject(final ItemModel data)
	{
		final List<ItemModel> items = data != null ? Lists.newArrayList(data) : Collections.emptyList();
		setValue(MODEL_SYNC_ITEMS, items);
		prepareView(items);
	}

	@SocketEvent(socketId = SOCKET_IN_INPUT_OBJECTS)
	public void showSyncJobsForInputObjects(final List<ItemModel> items)
	{
		setValue(MODEL_SYNC_ITEMS, items);
		prepareView(items);
	}

	protected void clearLoadedJobs()
	{
		pullListModel.clear();
		pushListModel.clear();
	}

	protected SyncTask createSyncTask(final Collection<ItemModel> item, final SyncItemJobModel syncJob)
	{
		final SyncTask syncTask = new SyncTask(Lists.newArrayList(item), syncJob);
		syncTask.setParameters(createSyncCtx());
		return syncTask;
	}

	protected List<SyncPopupViewModel> packModels(final List<SyncItemJobModel> itemJobModels, final SyncJobType type)
	{
		final List<ItemModel> syncItems = getSyncItems();
		final Map<String, Object> ctx = createSyncCtx();
		return itemJobModels.stream().map(item -> {
			final Optional<Boolean> inSync = getSynchronizationFacade().isInSync(syncItems, item, ctx);
			return new SyncPopupViewModel(item, type, inSync.isPresent() ? inSync.get() : null, getCatalogName(item));
		}).collect(Collectors.toList());
	}

	protected String getCatalogName(final SyncItemJobModel syncItemJobModel)
	{
		return labelService.getObjectLabel(syncItemJobModel.getSourceVersion().getCatalog()) + " "
				+ labelService.getObjectLabel(syncItemJobModel.getSourceVersion().getCatalog().getVersion());
	}

	protected Map<String, Object> createSyncCtx()
	{
		return new HashMap<>();
	}

	protected List<SyncItemJobModel> unpackModels(final Collection<SyncPopupViewModel> viewModels)
	{
		return viewModels.stream().map(SyncPopupViewModel::getJobModel).collect(Collectors.toList());
	}


	protected Optional<SyncItemJobModel> getSelectedSyncJob()
	{
		final Set<SyncPopupViewModel> pullSelection = pullListModel.getSelection();
		if (CollectionUtils.isNotEmpty(pullSelection))
		{
			return Optional.ofNullable(pullSelection.iterator().next().getJobModel());
		}

		final Set<SyncPopupViewModel> pushSelection = pushListModel.getSelection();
		if (CollectionUtils.isNotEmpty(pushSelection))
		{
			return Optional.ofNullable(pushSelection.iterator().next().getJobModel());
		}
		return Optional.empty();
	}

	public Listbox getPullList()
	{
		return pullList;
	}

	public Listbox getPushList()
	{
		return pushList;
	}

	public Tabbox getTabbox()
	{
		return tabbox;
	}

	public Tab getPullTab()
	{
		return pullTab;
	}

	public Tab getPushTab()
	{
		return pushTab;
	}

	public Button getSync()
	{
		return sync;
	}

	public Textbox getSearchbox()
	{
		return searchbox;
	}

	public Label getTitle()
	{
		return title;
	}

	public ListModelList<SyncPopupViewModel> getPullListModel()
	{
		return pullListModel;
	}

	public ListModelList<SyncPopupViewModel> getPushListModel()
	{
		return pushListModel;
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	protected SynchronizationFacade getSynchronizationFacade()
	{
		return synchronizationFacade;
	}

	public void setSynchronizationFacade(final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
