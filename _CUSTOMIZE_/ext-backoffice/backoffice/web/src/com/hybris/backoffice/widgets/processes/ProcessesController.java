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
package com.hybris.backoffice.widgets.processes;


import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;
import de.hybris.platform.servicelayer.event.events.AfterCronJobFinishedEvent;
import de.hybris.platform.servicelayer.event.events.BeforeCronJobStartEvent;
import de.hybris.platform.servicelayer.time.TimeService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;

import com.hybris.backoffice.cronjob.CronJobHistoryDataQuery;
import com.hybris.backoffice.cronjob.CronJobHistoryFacade;
import com.hybris.backoffice.events.processes.ProcessFinishedEvent;
import com.hybris.backoffice.events.processes.ProcessStartEvent;
import com.hybris.backoffice.widgets.processes.settings.ProcessesSettingsManager;
import com.hybris.backoffice.widgets.processes.settings.TimeRange;
import com.hybris.backoffice.widgets.processes.updater.ProcessesUpdatersRegistry;
import com.hybris.cockpitng.admin.CockpitMainWindowComposer;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class ProcessesController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(ProcessesController.class);
	protected static final String SETTING_SHOW_GLOBAL_JOBS = "showGlobalJobs";
	protected static final String SETTING_SHOW_FINISHED_JOBS = "showFinishedJobs";
	protected static final String SETTING_TIME_RANGES = "timeRanges";
	protected static final String SETTING_LISTITEM_RENDERER = "listitemRenderer";
	protected static final String SOCKET_IN_UPDATE_CRON_JOB = "updateCronJob";
	public static final String SOCKET_OUT_SHOW_LOG = "showLog";
	protected static final String YW_PROCESSES_OPENING_BTN_SPIN = "yw-processes-open-btn-spin";
	protected static final String YW_PROCESSES_UNSEEN = "yw-processes-unseen";
	public static final String MODEL_UNSEEN_PROCESSES = "unseenProcesses";
	public static final String COMP_ID_OPEN_BTN = "openBtn";
	public static final String COMP_ID_CLOSE_BTN = "closeBtn";
	public static final String COMP_ID_AUTO_CLOSE_COMPONENT = "autoCloseComponent";
	protected static final String DEFAULT_LISTITEM_RENDERER = "processesListItemRenderer";
	public static final String MODEL_PROGRESS_DATA_MAP = "progressDataMap";
	public static final String GLOBAL_EVENT_UPDATE_PROCESS_FOR_CRON_JOB = "updateProcessForCronJob";

	@Wire
	private Label finishedJobsStateLabel;
	@Wire
	private Label globalJobsStateLabel;
	@Wire
	private Checkbox globalJobsCheckbox;
	@Wire
	private Checkbox finishedJobsCheckbox;
	@Wire
	private Listbox timeRangeList;
	@Wire
	private Label timeRangeStateLabel;
	@Wire
	private Toolbarbutton openBtn;
	@Wire
	private Listbox processesListbox;
	@Wire
	private Timer startedCronJobLookupTimer;

	@WireVariable
	private transient CockpitUserService cockpitUserService;
	@WireVariable
	private transient TypeFacade typeFacade;
	@WireVariable
	private transient LabelService labelService;
	@WireVariable
	private transient TimeService timeService;
	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	@WireVariable
	private transient ProcessesUpdatersRegistry processesUpdatersRegistry;
	@WireVariable
	private transient CronJobHistoryFacade cronJobHistoryFacade;
	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	@WireVariable
	private transient List<ProcessesQueryDecorator> processesQueryDecorators;

	private transient ProcessesSettingsManager settingsManager;
	private ListModelList<CronJobHistoryModel> processesListModel;
	private boolean dataLoaded;
	private final Set<String> startedCronJobsToLookup = new HashSet<>();


	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		initSettingsManager();
		initProcessesListbox();
		UITools.modifySClass(openBtn, YW_PROCESSES_UNSEEN, hasAnyUnseen());
		startedCronJobLookupTimer.addEventListener(Events.ON_TIMER, event -> onStartedCronJobLookup());
	}

	protected boolean hasAnyUnseen()
	{
		return CollectionUtils.isNotEmpty(getUnseenProcesses());
	}

	protected void initSettingsManager()
	{
		final ProcessesSettingsManager settings = getSettingsManager();
		settings.addBooleanSetting(SETTING_SHOW_GLOBAL_JOBS, globalJobsCheckbox, globalJobsStateLabel);
		settings.addBooleanSetting(SETTING_SHOW_FINISHED_JOBS, finishedJobsCheckbox, finishedJobsStateLabel);
		settings.addTimeRangeSetting(SETTING_TIME_RANGES, timeRangeList, timeRangeStateLabel);
		settings.addSettingChangedListener(this::onProcessesSettingChanged);
	}

	protected void onProcessesSettingChanged(final String settingName, final Object updatedValue)
	{
		getUnseenProcesses().clear();
		fetchProcesses();
	}

	protected void initProcessesListbox()
	{
		final DataType dataType = loadDataType();
		final WidgetComponentRenderer<Listitem, Object, CronJobHistoryModel> renderer = getRenderer();
		processesListbox.setModel(getProcessesListModel());
		processesListbox.setItemRenderer((final Listitem listitem, final CronJobHistoryModel cronJobHistory, final int i) -> {
			renderer.render(listitem, getConfig(), cronJobHistory, dataType, getWidgetInstanceManager());
			handleUnseenMarkers(listitem, cronJobHistory);
		});
	}

	protected void handleUnseenMarkers(final Listitem listcell, final CronJobHistoryModel cronJobHistory)
	{
		final Set<CronJobHistoryModel> unseenProcesses = getUnseenProcesses();
		if (unseenProcesses.contains(cronJobHistory))
		{
			UITools.addSClass(listcell, YW_PROCESSES_UNSEEN);
		}
	}

	/**
	 * @deprecated since 6.5 please use {@link #handleUnseenMarkers(Listitem, CronJobHistoryModel)}
	 */
	@Deprecated
	protected void handleUnseenMarkers(final Listitem listcell, final Object cronJobHistory)
	{
		handleUnseenMarkers(listcell, (CronJobHistoryModel) cronJobHistory);
	}

	protected Map<String, Object> getConfig()
	{
		return new HashMap<>();
	}

	protected WidgetComponentRenderer<Listitem, Object, CronJobHistoryModel> getRenderer()
	{
		final String listItemRenderer = StringUtils.defaultIfBlank(getWidgetSettings().getString(SETTING_LISTITEM_RENDERER),
				DEFAULT_LISTITEM_RENDERER);
		return BackofficeSpringUtil.getBean(listItemRenderer, WidgetComponentRenderer.class);
	}

	protected void fetchProcesses()
	{
		final CronJobHistoryDataQuery query = getProcessesDataQuery();
		final List<CronJobHistoryModel> processes = cronJobHistoryFacade.getCronJobHistory(query);
		setProcesses(processes);
	}

	protected CronJobHistoryDataQuery getProcessesDataQuery()
	{
		final TimeRange timeRange = getSettingsManager().getSettingValue(SETTING_TIME_RANGES, TimeRange.class);
		final Duration duration = timeRange != null ? timeRange.getDuration() : null;
		return decorateQuery(new CronJobHistoryDataQuery(duration, isShowGlobalJobs(), isShowFinishedJobs()));
	}

	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	protected CronJobHistoryDataQuery decorateQuery(final CronJobHistoryDataQuery query)
	{
		return query;
	}

	protected void setProcesses(final List<CronJobHistoryModel> processes)
	{
		getProcessesListModel().clear();
		if (CollectionUtils.isNotEmpty(processes))
		{
			dataLoaded = true;
			getProcessesListModel().addAll(processes);
			sortProcessesByStartTime();
			UITools.modifySClass(openBtn, YW_PROCESSES_OPENING_BTN_SPIN, hasAnyRunning());
			resetAndUpdateProgressData(getAllRunningCronJobHistories());
		}
	}

	@GlobalCockpitEvent(eventName = GLOBAL_EVENT_UPDATE_PROCESS_FOR_CRON_JOB, scope = CockpitEvent.SESSION)
	public void updateProcessForCronJobOnEvent(final CockpitEvent event)
	{
		if (event.getData() instanceof String)
		{
			updateProcessForCronJob((String) event.getData());
		}
	}

	@SocketEvent(socketId = SOCKET_IN_UPDATE_CRON_JOB)
	public void updateProcessForCronJob(final String cronJobCode)
	{
		final List<CronJobHistoryModel> processes = cronJobHistoryFacade.getCronJobHistory(cronJobCode);
		if (CollectionUtils.isNotEmpty(processes))
		{
			updateProcesses(processes);
		}
		else
		{
			lookupCronJobsProcess(cronJobCode);
		}
	}

	protected void lookupCronJobsProcess(final String cronJobCode)
	{
		startedCronJobsToLookup.add(cronJobCode);
		if (!startedCronJobLookupTimer.isRunning())
		{
			startedCronJobLookupTimer.start();
		}
	}

	protected void onStartedCronJobLookup()
	{
		if (CollectionUtils.isNotEmpty(startedCronJobsToLookup))
		{
			updateProcesses(cronJobHistoryFacade.getCronJobHistory(new ArrayList<>(startedCronJobsToLookup)));
		}
	}

	protected void updateProcesses(final List<CronJobHistoryModel> processes)
	{
		if (CollectionUtils.isNotEmpty(processes))
		{
			processes.forEach(this::updateProcess);
			UITools.modifySClass(openBtn, YW_PROCESSES_OPENING_BTN_SPIN, hasAnyRunning());
			sortProcessesByStartTime();
		}
	}

	protected void updateProcess(final CronJobHistoryModel process)
	{
		stopCronJobLookupIfLastFound(process);
		handleUserUnseenChanges(process);
		addProcessToListModel(process);
	}

	protected void stopCronJobLookupIfLastFound(final CronJobHistoryModel process)
	{
		if (startedCronJobsToLookup.contains(process.getCronJobCode()))
		{
			startedCronJobsToLookup.remove(process.getCronJobCode());
			if (CollectionUtils.isEmpty(startedCronJobsToLookup) && startedCronJobLookupTimer.isRunning())
			{
				startedCronJobLookupTimer.stop();
			}
		}
	}

	protected void addProcessToListModel(final CronJobHistoryModel process)
	{
		if (process != null && (isShowGlobalJobs() || isExecutedByCurrentUser(process)))
		{
			final ListModelList<CronJobHistoryModel> listModel = getProcessesListModel();
			if (CronJobStatus.FINISHED.equals(process.getStatus()) && !isShowFinishedJobs())
			{
				listModel.remove(process);
			}
			else
			{
				final int index = listModel.indexOf(process);
				if (index >= 0)
				{
					listModel.set(index, process);
				}
				else
				{
					listModel.add(0, process);
				}
			}
		}
	}

	protected void handleUserUnseenChanges(final CronJobHistoryModel process)
	{
		boolean hasUnseenChanges = false;

		final boolean newProcess = !getProcessesListModel().contains(process);
		if (newProcess)
		{
			hasUnseenChanges = true;
		}
		else
		{
			// check for status change
			final int index = getProcessesListModel().indexOf(process);
			final CronJobHistoryModel oldProcess = getProcessesListModel().get(index);
			final boolean statusChanged = !Objects.equals(oldProcess.getStatus(), process.getStatus());
			if (statusChanged)
			{
				hasUnseenChanges = true;
			}
		}

		if (hasUnseenChanges)
		{
			getUnseenProcesses().add(process);
			UITools.addSClass(openBtn, YW_PROCESSES_UNSEEN);
		}
	}

	@GlobalCockpitEvent(eventName = CockpitMainWindowComposer.HEARTBEAT_EVENT, scope = CockpitEvent.SESSION)
	public void onApplicationHeartbeat(final CockpitEvent cockpitEvent)
	{
		if (startedCronJobLookupTimer.isRunning())
		{
			startedCronJobLookupTimer.stop();
		}
		onStartedCronJobLookup();
		refreshRunningProcesses();
	}

	public void refreshRunningProcesses()
	{
		final List<String> allRunningCronJobCodes = getAllRunningCronJobCodes();
		final List<CronJobHistoryModel> processes = cronJobHistoryFacade.getCronJobHistory(allRunningCronJobCodes);
		if (CollectionUtils.isNotEmpty(processes))
		{
			processes.forEach(this::updateProcess);
			resetAndUpdateProgressData(getAllRunningCronJobHistories());
		}
	}

	protected List<String> getAllRunningCronJobCodes()
	{
		return getAllRunningCronJobHistories().stream().map(CronJobHistoryModel::getCronJobCode).distinct()
				.collect(Collectors.toList());
	}

	protected List<CronJobHistoryModel> getAllRunningCronJobHistories()
	{
		return getProcessesListModel().stream().filter(process -> CronJobStatus.RUNNING.equals(process.getStatus()))
				.collect(Collectors.toList());
	}

	protected void resetAndUpdateProgressData(final List<CronJobHistoryModel> processes)
	{
		final Map<CronJobHistoryModel, ProgressData> newProgressData = new HashMap<>();
		final Map<CronJobHistoryModel, ProgressData> currentProgressData = getProgressDataMap();
		for (final CronJobHistoryModel process : processes)
		{
			if (CronJobStatus.RUNNING.equals(process.getStatus()))
			{
				ProgressData storedProgressData = currentProgressData.get(process);
				if (storedProgressData == null)
				{
					storedProgressData = createProgressData();
				}
				storedProgressData.updateProgress(process, getTimeService().getCurrentTime());
				newProgressData.put(process, storedProgressData);
			}
		}

		setValue(MODEL_PROGRESS_DATA_MAP, newProgressData);
	}

	protected ProgressData createProgressData()
	{
		return new DefaultProgressData();
	}

	protected void sortProcessesByStartTime()
	{
		final Comparator<Date> nullSafeReverseOrder = Comparator.nullsLast(Comparator.reverseOrder());
		processesListModel.sort(Comparator.comparing(CronJobHistoryModel::getStartTime, nullSafeReverseOrder), true);
	}

	protected boolean isExecutedByCurrentUser(final CronJobHistoryModel process)
	{
		return StringUtils.equals(process.getUserUid(), cockpitUserService.getCurrentUser());
	}

	protected boolean hasAnyRunning()
	{
		return getProcessesListModel().stream().anyMatch(process -> CronJobStatus.RUNNING.equals(process.getStatus()));
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = COMP_ID_OPEN_BTN)
	public void onOpenBtn()
	{
		UITools.removeSClass(openBtn, YW_PROCESSES_UNSEEN);

		if (!isDataLoaded())
		{
			fetchProcesses();
		}
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = COMP_ID_AUTO_CLOSE_COMPONENT)
	public void onAutoCloseComponent()
	{
		onCloseBtn();
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = COMP_ID_CLOSE_BTN)
	public void onCloseBtn()
	{
		UITools.removeSClass(openBtn, YW_PROCESSES_UNSEEN);
		setValue(MODEL_UNSEEN_PROCESSES, null);

		final Iterable<Component> unseenItems = processesListbox.queryAll('.' + YW_PROCESSES_UNSEEN);
		unseenItems.forEach(component -> UITools.removeSClass((HtmlBasedComponent) component, YW_PROCESSES_UNSEEN));
	}

	protected boolean isShowGlobalJobs()
	{
		return BooleanUtils.isTrue(getSettingsManager().getSettingValue(SETTING_SHOW_GLOBAL_JOBS, Boolean.class));
	}

	protected boolean isShowFinishedJobs()
	{
		return BooleanUtils.isTrue(getSettingsManager().getSettingValue(SETTING_SHOW_FINISHED_JOBS, Boolean.class));
	}


	public ProcessesSettingsManager getSettingsManager()
	{
		if (settingsManager == null)
		{
			settingsManager = new ProcessesSettingsManager(getWidgetInstanceManager());
		}
		return settingsManager;
	}

	protected DataType loadDataType()
	{
		try
		{
			return typeFacade.load(CronJobHistoryModel._TYPECODE);
		}
		catch (final TypeNotFoundException e)
		{
			final String message = String.format("Cannot load type for given type code %s", CronJobHistoryModel._TYPECODE);
			if (LOG.isDebugEnabled())
			{
				LOG.warn(message, e);
			}
			else
			{
				LOG.warn(message);
			}
			return null;
		}
	}

	@GlobalCockpitEvent(eventName = ProcessFinishedEvent.EVENT_NAME, scope = CockpitEvent.APPLICATION)
	public void processFinished(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getData() instanceof ProcessFinishedEvent)
		{
			final AfterCronJobFinishedEvent processEvent = ((ProcessFinishedEvent) cockpitEvent.getData()).getProcessEvent();
			if (StringUtils.isNotBlank(processEvent.getCronJob()))
			{
				updateProcessForCronJob(processEvent.getCronJob());
			}
		}
	}

	@GlobalCockpitEvent(eventName = ProcessStartEvent.EVENT_NAME, scope = CockpitEvent.APPLICATION)
	public void processStarted(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getData() instanceof ProcessStartEvent)
		{
			final BeforeCronJobStartEvent processEvent = ((ProcessStartEvent) cockpitEvent.getData()).getProcessEvent();
			if (StringUtils.isNotBlank(processEvent.getCronJob()))
			{
				updateProcessForCronJob(processEvent.getCronJob());
			}
		}
	}

	public Label getFinishedJobsStateLabel()
	{
		return finishedJobsStateLabel;
	}

	public Label getGlobalJobsStateLabel()
	{
		return globalJobsStateLabel;
	}

	public Checkbox getGlobalJobsCheckbox()
	{
		return globalJobsCheckbox;
	}

	public Checkbox getFinishedJobsCheckbox()
	{
		return finishedJobsCheckbox;
	}

	public Listbox getTimeRangeList()
	{
		return timeRangeList;
	}

	public Label getTimeRangeStateLabel()
	{
		return timeRangeStateLabel;
	}

	public Listbox getProcessesListbox()
	{
		return processesListbox;
	}

	public Toolbarbutton getOpenBtn()
	{
		return openBtn;
	}

	public boolean isDataLoaded()
	{
		return dataLoaded;
	}

	public ListModelList<CronJobHistoryModel> getProcessesListModel()
	{
		if (processesListModel == null)
		{
			processesListModel = new ListModelList<>();
		}
		return processesListModel;
	}

	public Map<CronJobHistoryModel, ProgressData> getProgressDataMap()
	{
		Map<CronJobHistoryModel, ProgressData> progressDataMap = getValue(MODEL_PROGRESS_DATA_MAP, Map.class);
		if (progressDataMap == null)
		{
			progressDataMap = new HashMap<>();
			setValue(MODEL_PROGRESS_DATA_MAP, progressDataMap);
		}
		return progressDataMap;
	}

	protected Set<CronJobHistoryModel> getUnseenProcesses()
	{
		Set<CronJobHistoryModel> unseenProcesses = getValue(MODEL_UNSEEN_PROCESSES, Set.class);
		if (unseenProcesses == null)
		{
			unseenProcesses = new HashSet<>();
			setValue(MODEL_UNSEEN_PROCESSES, unseenProcesses);
		}
		return unseenProcesses;
	}

	public CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	public ProcessesUpdatersRegistry getProcessesUpdatersRegistry()
	{
		return processesUpdatersRegistry;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	public CronJobHistoryFacade getCronJobHistoryFacade()
	{
		return cronJobHistoryFacade;
	}

	public List<ProcessesQueryDecorator> getProcessesQueryDecorators()
	{
		return processesQueryDecorators;
	}

	public Timer getStartedCronJobLookupTimer()
	{
		return startedCronJobLookupTimer;
	}

	public Set<String> getStartedCronJobsToLookup()
	{
		return startedCronJobsToLookup;
	}
}
