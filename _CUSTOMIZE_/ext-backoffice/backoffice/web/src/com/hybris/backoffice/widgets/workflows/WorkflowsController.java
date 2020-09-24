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
package com.hybris.backoffice.widgets.workflows;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.backoffice.workflow.WorkflowSearchData;
import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.backoffice.workflow.wizard.CollaborationWorkflowWizardForm;
import com.hybris.backoffice.workflow.wizard.WorkflowsDropConsumer;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.list.LazyPageableListModel;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;


public class WorkflowsController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowsController.class);
	public static final String SOCKET_OUT_WORKFLOW_SELECTED = "workflowSelected";
	public static final String SOCKET_OUT_WORKFLOW_ACTION_SELECTED = "workflowActionSelected";
	public static final String SOCKET_IN_REFRESH = "refresh";
	protected static final String SOCKET_OUT_CREATE_WORKFLOW = "createWorkflowContext";
	protected static final String COMP_ID_ADD_WORKFLOW = "createWorkflowButton";

	public static final String SETTING_WORKFLOW_RENDERER = "workflowRenderer";
	public static final String SETTING_IS_WORKFLOW_PLANNED_ENABLED = "isWorkflowPlannedEnabled";
	public static final String SETTING_IS_WORKFLOW_RUNNING_ENABLED = "isWorkflowRunningEnabled";
	public static final String SETTING_IS_WORKFLOW_FINISHED_ENABLED = "isWorkflowFinishedEnabled";
	public static final String SETTING_IS_WORKFLOW_TERMINATED_ENABLED = "isWorkflowTerminatedEnabled";
	public static final String SETTING_IS_WORKFLOW_DROP_AREA_ENABLED = "isWorkflowDropAreaEnabled";
	public static final String SETTING_PAGE_SIZE = "pageSize";
	public static final int DEFAULT_PAGE_SIZE = 20;

	private static final Map<String, WorkflowStatus> allWorkflowStatuses;

	static
	{
		final Map<String, WorkflowStatus> tempMap = new HashMap<>();
		tempMap.put(SETTING_IS_WORKFLOW_PLANNED_ENABLED, WorkflowStatus.PLANNED);
		tempMap.put(SETTING_IS_WORKFLOW_RUNNING_ENABLED, WorkflowStatus.RUNNING);
		tempMap.put(SETTING_IS_WORKFLOW_FINISHED_ENABLED, WorkflowStatus.FINISHED);
		tempMap.put(SETTING_IS_WORKFLOW_TERMINATED_ENABLED, WorkflowStatus.TERMINATED);
		allWorkflowStatuses = Collections.unmodifiableMap(tempMap);
	}

	@Wire
	private Listbox workflowListBox;
	@Wire
	private Div createWorkflowDropArea;

	@WireVariable
	private transient WorkflowFacade workflowFacade;
	@WireVariable
	private transient TypeFacade typeFacade;
	@WireVariable
	private transient WorkflowsTypeFacade workflowsTypeFacade;

	private LazyPageableListModel<WorkflowModel> workflowListModel;

	@Override
	public void initialize(final Component component)
	{
		super.initialize(component);

		final List<WorkflowStatus> workflowStatuses = getWorkflowStatusesFromSettings();
		workflowListModel = createWorkflowsListModel(workflowStatuses);
		workflowListBox.setModel(workflowListModel);
		workflowListBox.setVflex(true);
		initializeContentRenderer();
		if (getWidgetSettings().getBoolean(SETTING_IS_WORKFLOW_DROP_AREA_ENABLED))
		{
			initializeWorkflowCreateDropArea();
		}
	}

	protected String getRendererNameFromSettings(final String settingKey)
	{
		return ObjectUtils.defaultIfNull(getWidgetSettings().getString(settingKey), settingKey);
	}

	protected List<WorkflowStatus> getWorkflowStatusesFromSettings()
	{
		return getAllWorkflowStatuses().entrySet().stream().filter(entry -> getWidgetSettings().getBoolean(entry.getKey()))
				.map(Map.Entry::getValue).collect(Collectors.toList());
	}

	protected LazyPageableListModel<WorkflowModel> createWorkflowsListModel(final List<WorkflowStatus> workflowStatuses)
	{
		final WorkflowSearchData workflowSearchData = new WorkflowSearchData(getPageSize(), workflowStatuses);
		final Pageable<WorkflowModel> pageable = workflowFacade.getWorkflows(workflowSearchData);
		return new LazyPageableListModel<>(pageable);
	}

	protected <T> void initializeContentRenderer()
	{
		final WidgetComponentRenderer renderer = getRenderer(getRendererNameFromSettings(SETTING_WORKFLOW_RENDERER));
		final DataType dataType = loadDataType(WorkflowModel._TYPECODE);
		final WidgetInstanceManager wim = getWidgetInstanceManager();
		workflowListBox.setItemRenderer((final Listitem listItem, final T data, final int index) -> {
			if (data != null)
			{
				renderer.render(listItem, null, data, dataType, wim);
			}
		});
	}

	protected void initializeWorkflowCreateDropArea()
	{
		getDragAndDropStrategy().makeDroppable(createWorkflowDropArea, new WorkflowsDropConsumer(this::openNewWorkflowWizard),
				new DefaultCockpitContext());
	}

	@ViewEvent(componentID = COMP_ID_ADD_WORKFLOW, eventName = Events.ON_CLICK)
	public void onCreateWorkflowClick()
	{
		openNewWorkflowWizard(null);
	}

	protected void openNewWorkflowWizard(final List<ItemModel> items)
	{
		final Map<String, Object> contextMap = new HashMap<>();
		contextMap.put(WorkflowConstants.WIZARD_WORKFLOW_CTX_ATTACHMENTS, items);
		contextMap.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), CollaborationWorkflowWizardForm.class.getName());
		contextMap.put(WorkflowConstants.WIZARD_WORKFLOW_CTX_ATTACHMENT_TYPE,
				workflowsTypeFacade.findCommonAttachmentType(items).orElse(null));
		contextMap.put(WorkflowConstants.WIZARD_WORKFLOW_CTX_DESTINATION,
				WorkflowConstants.EVENT_LINK_WORKFLOW_DETAILS_DESTINATION);
		sendOutput(SOCKET_OUT_CREATE_WORKFLOW, contextMap);
	}

	@SocketEvent(socketId = SOCKET_IN_REFRESH)
	public void onRefreshSocket()
	{
		workflowListModel.refresh();
	}

	@GlobalCockpitEvent(eventName = ObjectFacade.OBJECTS_UPDATED_EVENT, scope = CockpitEvent.SESSION)
	public void onWorkflowUpdated(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getDataAsCollection().stream().anyMatch(WorkflowModel.class::isInstance))
		{
			workflowListModel.refresh();
		}
	}

	@GlobalCockpitEvent(eventName = ObjectFacade.OBJECTS_DELETED_EVENT, scope = CockpitEvent.SESSION)
	public void onWorkflowDeleted(final CockpitEvent cockpitEvent)
	{
		if (cockpitEvent.getDataAsCollection().stream().anyMatch(WorkflowModel.class::isInstance))
		{
			workflowListModel.refresh();
		}
	}

	protected int getPageSize()
	{
		final int pageSize = getWidgetSettings().getInt(SETTING_PAGE_SIZE);
		return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
	}

	protected WidgetComponentRenderer getRenderer(final String name)
	{
		return BackofficeSpringUtil.getBean(name, WidgetComponentRenderer.class);
	}

	public Div getCreateWorkflowDropArea()
	{
		return createWorkflowDropArea;
	}

	protected DataType loadDataType(final String typeCode)
	{
		try
		{
			return getTypeFacade().load(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn("type " + typeCode + " not found", e);
		}
		return null;
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	public static Map<String, WorkflowStatus> getAllWorkflowStatuses()
	{
		return allWorkflowStatuses;
	}

	public Listbox getWorkflowListBox()
	{
		return workflowListBox;
	}

	public WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}
}
