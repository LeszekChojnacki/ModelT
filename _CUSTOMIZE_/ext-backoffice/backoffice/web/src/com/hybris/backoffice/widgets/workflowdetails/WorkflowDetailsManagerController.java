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
package com.hybris.backoffice.widgets.workflowdetails;

import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.Objects;
import java.util.Optional;

import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Manager which allows to manage/refresh workflow details view.
 */
public class WorkflowDetailsManagerController extends DefaultWidgetController
{

	@WireVariable
	private transient ModelService modelService;

	public static final String MODEL_CURRENT_WORKFLOW = "currentWorkflow";
	public static final String SOCKET_IN_SHOW_WORKFLOW = "showWorkflow";
	public static final String SOCKET_OUT_SELECTED_WORKFLOW_UPDATED = "selectedWorkflowUpdated";
	public static final String SOCKET_OUT_SELECTED_WORKFLOW_DELETED = "selectedWorkflowDeleted";

	@SocketEvent(socketId = SOCKET_IN_SHOW_WORKFLOW)
	public void openWorkflow(final WorkflowModel workflow)
	{
		if (workflow != null)
		{
			modelService.refresh(workflow);
		}
		updateSelectedWorkflow(workflow);
	}

	protected void updateSelectedWorkflow(final WorkflowModel workflow)
	{
		setValue(MODEL_CURRENT_WORKFLOW, workflow);
		sendOutput(SOCKET_OUT_SELECTED_WORKFLOW_UPDATED, workflow);
	}

	protected void selectedWorkflowDeleted(final WorkflowModel workflow)
	{
		setValue(MODEL_CURRENT_WORKFLOW, null);
		sendOutput(SOCKET_OUT_SELECTED_WORKFLOW_DELETED, workflow);
	}

	@GlobalCockpitEvent(eventName = ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, scope = CockpitEvent.SESSION)
	public void onWorkflowUpdated(final CockpitEvent event)
	{
		getCurrentWorkflow().ifPresent(currentWorkflow -> //
		event.getDataAsCollection()//
				.stream()//
				.filter(updatedObject -> Objects.equals(updatedObject, currentWorkflow))//
				.findAny()//
				.ifPresent(workflow -> updateSelectedWorkflow((WorkflowModel) workflow)));

	}

	@GlobalCockpitEvent(eventName = ObjectCRUDHandler.OBJECTS_DELETED_EVENT, scope = CockpitEvent.SESSION)
	public void onWorkflowDeleted(final CockpitEvent event)
	{
		getCurrentWorkflow().ifPresent(currentWorkflow -> handleWorkflowDeletion(currentWorkflow, event));
	}

	protected void handleWorkflowDeletion(final WorkflowModel currentWorkflow, final CockpitEvent event)
	{
		event.getDataAsCollection()//
				.stream()//
				.filter(updatedObject -> Objects.equals(updatedObject, currentWorkflow))//
				.findAny()//
				.ifPresent(workflow -> selectedWorkflowDeleted((WorkflowModel) workflow));
	}

	protected Optional<WorkflowModel> getCurrentWorkflow()
	{
		return Optional.ofNullable(getValue(MODEL_CURRENT_WORKFLOW, WorkflowModel.class));
	}

	public ModelService getModelService()
	{
		return modelService;
	}
}
