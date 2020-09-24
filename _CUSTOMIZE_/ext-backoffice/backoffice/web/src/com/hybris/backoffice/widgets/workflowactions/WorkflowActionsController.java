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
package com.hybris.backoffice.widgets.workflowactions;


import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.admin.CockpitMainWindowComposer;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


/**
 * Widget controller which displays workflow actions assigned to current user.
 */
public class WorkflowActionsController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(WorkflowActionsController.class);

	public static final String SOCKET_OUT_WORKFLOW_ACTION_SELECTED = "workflowActionSelected";
	public static final String SOCKET_OUT_NUMBER_OF_WORKFLOW_ACTIONS = "numberOfWorkflowActions";
	public static final String SETTING_RENDERER = "workflowActionsRenderer";

	@Wire
	private Listbox workflowActionsListbox;
	@WireVariable
	private transient WorkflowFacade workflowFacade;
	@WireVariable
	private transient TypeFacade typeFacade;
	@WireVariable
	private transient ObjectFacade objectFacade;
	private transient ListModelList<WorkflowActionModel> workflowActionsListModel;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		initializeListboxModel();
		initializeContentRenderer();
		updateWorkflowActions();
		sendUpdatedNumberOfTasks(workflowActionsListModel.getSize());
	}

	protected void initializeListboxModel()
	{
		workflowActionsListModel = new ListModelList<>();
		workflowActionsListbox.setModel(workflowActionsListModel);
	}

	protected <T> void initializeContentRenderer()
	{
		final String rendererName = getRendererNameFromSettings(SETTING_RENDERER);
		final WidgetComponentRenderer renderer = getRenderer(rendererName);
		final DataType dataType = loadDataType(WorkflowActionModel._TYPECODE);
		final WidgetInstanceManager wim = getWidgetInstanceManager();
		workflowActionsListbox.setItemRenderer((final Listitem listItem, final T data, final int index) -> {
			if (data != null)
			{
				renderer.render(listItem, null, data, dataType, wim);
			}
		});
	}

	@GlobalCockpitEvent(eventName = ObjectFacade.OBJECTS_DELETED_EVENT, scope = CockpitEvent.SESSION)
	public void reloadActionsWhenAttachedItemsAreDeleted(final CockpitEvent cockpitEvent)
	{
		final Collection<Object> deletedObjects = cockpitEvent.getDataAsCollection();
		if (CollectionUtils.isNotEmpty(deletedObjects))
		{
			for (int index = 0; index < workflowActionsListModel.size(); index++)
			{
				final WorkflowActionModel workflowAction = workflowActionsListModel.get(index);
				if (CollectionUtils.containsAny(workflowAction.getAttachmentItems(), deletedObjects))
				{
					reloadActionAtIndex(index);
				}
			}
		}
	}

	protected void reloadActionAtIndex(final int index)
	{
		try
		{
			final WorkflowActionModel workflowAction = workflowActionsListModel.get(index);
			final WorkflowActionModel reloaded = objectFacade.reload(workflowAction);
			workflowActionsListModel.set(index, reloaded);
		}
		catch (final ObjectNotFoundException e)
		{
			LOG.error("Cannot reload object", e);
		}
	}

	@GlobalCockpitEvent(eventName = CockpitMainWindowComposer.HEARTBEAT_EVENT, scope = CockpitEvent.SESSION)
	public void onApplicationHeartbeat(final CockpitEvent cockpitEvent)
	{
		updateWorkflowActions();
	}

	protected void updateWorkflowActions()
	{
		final List<WorkflowActionModel> refreshedWorkflowActionsModel = workflowFacade.getWorkflowActions();
		if (!CollectionUtils.isEqualCollection(refreshedWorkflowActionsModel, workflowActionsListModel))
		{
			workflowActionsListModel.clear();
			workflowActionsListModel.addAll(refreshedWorkflowActionsModel);
			sendUpdatedNumberOfTasks(refreshedWorkflowActionsModel.size());
		}
	}

	protected void sendUpdatedNumberOfTasks(final int numberOfTasks)
	{
		sendOutput(SOCKET_OUT_NUMBER_OF_WORKFLOW_ACTIONS, numberOfTasks);
	}

	protected String getRendererNameFromSettings(final String settingKey)
	{
		return ObjectUtils.defaultIfNull(getWidgetSettings().getString(settingKey), settingKey);
	}

	protected WidgetComponentRenderer getRenderer(final String name)
	{
		return BackofficeSpringUtil.getBean(name, WidgetComponentRenderer.class);
	}

	protected DataType loadDataType(final String typeCode)
	{
		try
		{
			return typeFacade.load(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn("type " + typeCode + " not found", e);
		}
		return null;
	}

	public Listbox getWorkflowActionsListbox()
	{
		return workflowActionsListbox;
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	public ListModelList<WorkflowActionModel> getWorkflowActionsListModel()
	{
		return workflowActionsListModel;
	}
}
