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
package com.hybris.backoffice.widgets.processes.renderer;

import com.google.common.collect.Sets;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.time.TimeService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.XulElement;

import com.hybris.backoffice.cronjob.CronJobHistoryFacade;
import com.hybris.backoffice.widgets.processes.ProcessItemRenderingStrategy;
import com.hybris.backoffice.widgets.processes.ProcessesController;
import com.hybris.backoffice.widgets.processes.ProgressData;
import com.hybris.cockpitng.components.ProgressBar;
import com.hybris.cockpitng.components.Stopwatch;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class DefaultProcessesListItemRenderer implements WidgetComponentRenderer<Listitem, Object, CronJobHistoryModel>
{

	protected static final String SCLASS_PROCESSES_LIST_CONTENT = "yw-processes-list-item-content";
	protected static final String SCLASS_PROCESSES_LIST_CODE = "yw-processes-list-item-content-code";
	protected static final String SCLASS_PROCESSES_LIST_PROGRESS = "yw-processes-list-item-content-progress";
	protected static final String SCLASS_PROCESSES_LIST_ITEM_ICON = "yw-processes-list-item-icon";
	protected static final String SCLASS_PREFIX_PROCESSES_LIST_ICON = "yw-processes-list-item-icon yw-processes-list-item-status-";
	protected static final String SCLASS_PREFIX_PROCESSES_LIST_STATUS = "yw-processes-list-item-content-status yw-processes-list-item-status-";
	protected static final String SCLASS_STATUS_INFO_LABEL = "yw-processes-list-item-status-info";
	protected static final String SCLASS_PROCESSES_LIST_EDIT = "yw-processes-list-item-content-edit";
	protected static final String SCLASS_PROCESSES_LIST_CONTENT_DUE = "yw-processes-list-item-content-date";
	protected static final String SCLASS_PROCESSES_MENU_POPUP = "yw-processes-menu-popup yw-pointer-menupopup yw-pointer-menupopup-right";
	protected static final String SCLASS_PROCESSES_NO_ACCESS = "yw-processes-no-access";
	protected static final String SCLASS_MENU_NO_ICON = " ";


	protected static final String LABEL_PROCESS_PROCESSING = "processes.list.item.processing";
	protected static final String LABEL_PROCESS_PAUSED = "processes.list.item.paused";
	protected static final String LABEL_PROCESS_FINISHED = "processes.list.item.finished";
	protected static final String LABEL_PROCESS_ABORTED = "processes.list.item.aborted";
	protected static final String LABEL_PROCESS_UNKNOWN = "processes.list.item.unknown";
	protected static final String LABEL_PROCESS_FAILED = "processes.list.item.failed";
	protected static final String LABEL_PROCESS_SHOW_LOG = "processes.list.item.show.log";
	protected static final String LABEL_PROCESS_RE_RUN = "processes.list.item.re.run";
	protected static final String LABEL_PROCESS_ABORT = "processes.list.item.abort";
	protected static final String LABEL_PROCESS_CONFIRMATION_TITLE = "processes.list.item.abort.title";
	protected static final String LABEL_PROCESS_COMFIRMATION_MESSAGE = "processes.list.item.abort.message";
	protected static final String LABEL_PROCESS_NO_LOGS = "processes.list.item.no.logs";
	protected static final String LABEL_PROCESS_STATUS_INFO = "processes.list.item.title.default";
	protected static final String LABEL_PROCESS_STATUS_INFO_RUNNING = "processes.list.item.title.running";
	protected static final String LABEL_PROCESS_NO_ACCESS = "processes.list.item.noaccess";

	protected static final String END_DATE_PATTERN = "EEE, MMM d, h:mm a";
	protected static final String POPUP_POSITION = "start_before";
	protected static final String STATUS_FAILED = "failed";
	public static final String MIN_Z_INDEX = "minZIndex";
	public static final String MENU_POPUP_ZINDEX = "5001";

	private CockpitUserService cockpitUserService;
	private CronJobHistoryFacade cronJobHistoryFacade;
	private LabelService labelService;
	private PermissionFacade permissionFacade;
	private TimeService timeService;
	private ProcessItemRenderingStrategyRegistry processItemRenderingStrategyRegistry;
	private CronJobService cronJobService;
	private static final Set<CronJobStatus> ABORTABLE_STATUSES = Sets.newHashSet(CronJobStatus.RUNNING, CronJobStatus.UNKNOWN,
			CronJobStatus.PAUSED, CronJobStatus.RUNNINGRESTART);

	@Override
	public void render(final Listitem listitem, final Object configuration, final CronJobHistoryModel cronJobHistory,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final Listcell listcell = new Listcell();
		listitem.appendChild(listcell);

		if (!permissionFacade.canReadInstance(cronJobHistory))
		{
			listcell.appendChild(createNoAccessRow());
		}
		else
		{
			final Div content = new Div();
			content.setSclass(SCLASS_PROCESSES_LIST_CONTENT);

			content.appendChild(createStatusInfoComponent(cronJobHistory));
			content.appendChild(createMiddleContent(cronJobHistory, widgetInstanceManager));
			content.appendChild(createTimeComponent(cronJobHistory));

			listcell.appendChild(createSyncIcon(cronJobHistory, widgetInstanceManager));
			listcell.appendChild(content);
		}

	}

	protected Component createNoAccessRow()
	{
		final Label label = new Label();
		label.setValue(getLabel(LABEL_PROCESS_NO_ACCESS));
		label.setSclass(SCLASS_PROCESSES_NO_ACCESS);
		return label;
	}

	protected Div createMiddleContent(final CronJobHistoryModel cronJobHistory, final WidgetInstanceManager wim)
	{
		final Div middleContent = new Div();
		final Button editButton = createEditButton();
		final Menupopup editPopup = createEditPopup(cronJobHistory, wim);
		editPopup.setParent(middleContent);
		editButton.addEventListener(Events.ON_CLICK, event -> editPopup.open(editButton, POPUP_POSITION));
		middleContent.appendChild(editButton);
		middleContent.appendChild(createJobCodeComponent(cronJobHistory));

		getStrategy(cronJobHistory)
				.filter(processItemRenderingStrategy -> processItemRenderingStrategy.isProgressSupported(cronJobHistory))
				.ifPresent(strategy -> middleContent.appendChild(createProgressBar(cronJobHistory, wim)));
		return middleContent;
	}

	protected Div createStatusInfoComponent(final CronJobHistoryModel cronJobHistory)
	{
		final Label status = new Label(getLabel(getStatusReplacementLabelKey(cronJobHistory)));
		status.setSclass(SCLASS_PREFIX_PROCESSES_LIST_STATUS + getSclassSuffixFor(cronJobHistory));

		final Label jobTitle = new Label();
		getStrategy(cronJobHistory).ifPresent(strategy -> jobTitle.setValue(strategy.getTitle(cronJobHistory)));
		jobTitle.setSclass(SCLASS_STATUS_INFO_LABEL);

		final Div statusDiv = new Div();
		statusDiv.appendChild(status);
		statusDiv.appendChild(jobTitle);
		return statusDiv;
	}

	protected String getSclassSuffixFor(final CronJobHistoryModel cronJobHistoryModel)
	{
		if (isFailed(cronJobHistoryModel))
		{
			return STATUS_FAILED;
		}
		else
		{
			return cronJobHistoryModel.getStatus().toString().toLowerCase();
		}
	}

	protected Label createJobCodeComponent(final CronJobHistoryModel cronJobHistory)
	{
		final Label jobCode = new Label();
		jobCode.setSclass(SCLASS_PROCESSES_LIST_CODE);
		getStrategy(cronJobHistory).ifPresent(strategy -> jobCode.setValue(strategy.getJobTitle(cronJobHistory)));
		return jobCode;
	}

	protected Div createSyncIcon(final CronJobHistoryModel cronJobHistory, final WidgetInstanceManager wim)
	{
		final Div icon = new Div();
		UITools.addSClass(icon, SCLASS_PROCESSES_LIST_ITEM_ICON);
		UITools.addSClass(icon, SCLASS_PREFIX_PROCESSES_LIST_ICON + getSclassSuffixFor(cronJobHistory));
		return icon;
	}

	protected XulElement createProgressBar(final CronJobHistoryModel cronJobHistory, final WidgetInstanceManager wim)
	{
		final Div progressBarContainer = new Div();
		if (CronJobStatus.RUNNING.equals(cronJobHistory.getStatus()))
		{
			final ProgressBar progressBar = new ProgressBar();
			final Map<CronJobHistoryModel, ProgressData> progressDataMap = wim.getModel()
					.getValue(ProcessesController.MODEL_PROGRESS_DATA_MAP, Map.class);
			final ProgressData progressData = progressDataMap != null ? progressDataMap.get(cronJobHistory) : null;
			if (progressData != null)
			{
				progressBar.setLastUpdatePercentage(progressData.getEstimatedCurrentPercentage());
				progressBar.setMaxPercentage(progressData.getMaxPercentage());
				progressBar.setTimeToIncreaseOnePercent(progressData.getTimeToIncreaseOnePercent());
			}
			else
			{
				progressBar.setTimeToIncreaseOnePercent(0);
				progressBar.setMaxPercentage(0);
				progressBar.setLastUpdatePercentage(0);
			}
			progressBarContainer.appendChild(progressBar);

		}

		progressBarContainer.setSclass(SCLASS_PROCESSES_LIST_PROGRESS);
		return progressBarContainer;
	}

	protected XulElement createTimeComponent(final CronJobHistoryModel cronJobHistory)
	{
		switch (cronJobHistory.getStatus())
		{
			case RUNNINGRESTART:
			case RUNNING:
				return createStopwatch(cronJobHistory);
			default:
				return createEndTimeLabel(cronJobHistory);
		}
	}

	private XulElement createStopwatch(final CronJobHistoryModel cronJobHistory)
	{
		final Stopwatch stopwatch = new Stopwatch();
		stopwatch.setSclass(SCLASS_PROCESSES_LIST_CONTENT_DUE);
		stopwatch.setStartTime(cronJobHistory.getStartTime().getTime());
		stopwatch.setCurrentTimeSupplier(() -> Long.valueOf(timeService.getCurrentTime().getTime()));
		stopwatch.setDisplayDays(true);
		stopwatch.start();
		return stopwatch;
	}

	protected Button createEditButton()
	{
		final Button editButton = new Button();
		editButton.setSclass(SCLASS_PROCESSES_LIST_EDIT);
		return editButton;
	}

	protected Menupopup createEditPopup(final CronJobHistoryModel cronJobHistory, final WidgetInstanceManager wim)
	{
		final Menupopup popup = new Menupopup();
		UITools.modifySClass(popup, SCLASS_PROCESSES_MENU_POPUP, true);
		popup.setWidgetOverride(MIN_Z_INDEX, MENU_POPUP_ZINDEX);

		final Menuitem menuitemShowLog = new Menuitem(getLabel(LABEL_PROCESS_SHOW_LOG));
		menuitemShowLog.setIconSclass(SCLASS_MENU_NO_ICON);
		menuitemShowLog.addEventListener(Events.ON_CLICK, event -> showCronJobLog(cronJobHistory, wim));
		popup.appendChild(menuitemShowLog);

		if (isReRunApplicable(cronJobHistory))
		{
			final Menuitem menuitemReRun = new Menuitem(getLabel(LABEL_PROCESS_RE_RUN));
			menuitemReRun.setIconSclass(SCLASS_MENU_NO_ICON);
			menuitemReRun.addEventListener(Events.ON_CLICK, event -> rerunCronJob(cronJobHistory));
			popup.appendChild(menuitemReRun);
		}

		if (isAbortApplicable(cronJobHistory))
		{
			final Menuitem menuitemAbort = new Menuitem(getLabel(LABEL_PROCESS_ABORT));
			menuitemAbort.setIconSclass(SCLASS_MENU_NO_ICON);
			menuitemAbort.addEventListener(Events.ON_CLICK, event -> showAbortConfirmationPopup(cronJobHistory));
			popup.appendChild(menuitemAbort);
		}

		return popup;
	}

	protected int showAbortConfirmationPopup(final CronJobHistoryModel cronJobHistory)
	{
		return Messagebox.show(getLabel(LABEL_PROCESS_COMFIRMATION_MESSAGE), getLabel(LABEL_PROCESS_CONFIRMATION_TITLE),
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				event -> getCronJobService().requestAbortCronJob(cronJobHistory.getCronJob()));
	}

	protected boolean isAbortApplicable(final CronJobHistoryModel cronJobHistory) {
		final CronJobModel cronJob = cronJobHistory.getCronJob();
		return getCronJobService().isAbortable(cronJob) && ABORTABLE_STATUSES.contains(cronJobHistory.getStatus());
	}

	protected void showCronJobLog(final CronJobHistoryModel cronJobHistory, final WidgetInstanceManager wim)
	{
		final Optional<? extends ItemModel> log = cronJobHistoryFacade.findLog(cronJobHistory);
		if (log.isPresent())
		{
			wim.sendOutput(ProcessesController.SOCKET_OUT_SHOW_LOG, log.get());
		}
		else
		{
			Messagebox.show(getLabel(LABEL_PROCESS_NO_LOGS));
		}
	}

	protected void rerunCronJob(final CronJobHistoryModel cronJobHistory)
	{
		getStrategy(cronJobHistory).ifPresent(strategy -> strategy.rerunCronJob(cronJobHistory));
	}

	protected boolean isReRunApplicable(final CronJobHistoryModel cronJobHistory)
	{
		return getStrategy(cronJobHistory)
				.filter(processItemRenderingStrategy -> processItemRenderingStrategy.isRerunApplicable(cronJobHistory)).isPresent();
	}

	protected boolean isFailed(final CronJobHistoryModel cronJobHistoryModel)
	{
		return CronJobStatus.FINISHED.equals(cronJobHistoryModel.getStatus())
				&& (CronJobResult.ERROR.equals(cronJobHistoryModel.getResult())
						|| CronJobResult.FAILURE.equals(cronJobHistoryModel.getResult()));
	}

	protected Label createEndTimeLabel(final CronJobHistoryModel cronJobHistory)
	{
		final String labelKey = getStatusReplacementLabelKey(cronJobHistory);
		final SimpleDateFormat format = new SimpleDateFormat(END_DATE_PATTERN);

		final Date time = cronJobHistory.getEndTime() != null ? cronJobHistory.getEndTime() : cronJobHistory.getStartTime();
		final String timeLabel = time != null ? getLabel(labelKey, format.format(time)) : StringUtils.EMPTY;

		final Label timeInfo = new Label(timeLabel);
		timeInfo.setSclass(SCLASS_PROCESSES_LIST_CONTENT_DUE);
		return timeInfo;
	}

	protected String getStatusReplacementLabelKey(final CronJobHistoryModel cronJobHistoryModel)
	{
		switch (cronJobHistoryModel.getStatus())
		{
			case RUNNINGRESTART:
				return LABEL_PROCESS_PROCESSING;
			case RUNNING:
				return LABEL_PROCESS_PROCESSING;
			case PAUSED:
				return LABEL_PROCESS_PAUSED;
			case ABORTED:
				return LABEL_PROCESS_ABORTED;
			case FINISHED:
				return getFinishedStatusReplacementLabelKey(cronJobHistoryModel);
			default:
				return LABEL_PROCESS_UNKNOWN;
		}
	}

	protected String getFinishedStatusReplacementLabelKey(final CronJobHistoryModel cronJobHistoryModel)
	{
		if (isFailed(cronJobHistoryModel))
		{
			return LABEL_PROCESS_FAILED;
		}
		else if (cronJobHistoryModel.getResult() == CronJobResult.SUCCESS)
		{
			return LABEL_PROCESS_FINISHED;
		}
		return LABEL_PROCESS_UNKNOWN;
	}

	protected String getLabel(final String labelKey)
	{
		return Labels.getLabel(labelKey, new Object[]
		{ StringUtils.EMPTY });
	}

	protected String getLabel(final String labelKey, final String... args)
	{
		return Labels.getLabel(labelKey, args);
	}

	protected String getStatusInfo(final CronJobHistoryModel cronJobHistoryModel)
	{
		final String statusLabel = getLabel(getStatusReplacementLabelKey(cronJobHistoryModel));
		final CronJobStatus status = cronJobHistoryModel.getStatus();
		final String labelKey = (status == CronJobStatus.RUNNING || status == CronJobStatus.RUNNINGRESTART)
				? LABEL_PROCESS_STATUS_INFO_RUNNING : LABEL_PROCESS_STATUS_INFO;
		return getLabel(labelKey, statusLabel.toLowerCase());
	}


	public Optional<ProcessItemRenderingStrategy> getStrategy(final CronJobHistoryModel context)
	{
		return Optional.of(processItemRenderingStrategyRegistry.getStrategy(context));
	}

	@Required
	public void setCronJobHistoryFacade(final CronJobHistoryFacade cronJobHistoryFacade)
	{
		this.cronJobHistoryFacade = cronJobHistoryFacade;
	}

	public CronJobHistoryFacade getCronJobHistoryFacade()
	{
		return cronJobHistoryFacade;
	}


	@Required
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	public CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	public ProcessItemRenderingStrategyRegistry getProcessItemRenderingStrategyRegistry()
	{
		return processItemRenderingStrategyRegistry;
	}

	@Required
	public void setProcessItemRenderingStrategyRegistry(final ProcessItemRenderingStrategyRegistry registry)
	{
		this.processItemRenderingStrategyRegistry = registry;
	}

	public CronJobService getCronJobService() {
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService) {
		this.cronJobService = cronJobService;
	}
}
