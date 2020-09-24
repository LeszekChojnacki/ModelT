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
package com.hybris.backoffice.workflow.renderer.actionexecutors;

import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.backoffice.workflow.WorkflowFacade;


public class WorkflowTerminateActionExecutor implements Function<WorkflowModel, Boolean>
{
	private WorkflowFacade workflowFacade;
	private WorkflowEventPublisher workflowEventPublisher;
	private NotificationService notificationService;

	@Override
	public Boolean apply(final WorkflowModel workflow)
	{
		final boolean terminated = getWorkflowFacade().terminateWorkflow(workflow);
		if (terminated)
		{
			getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE,
					WorkflowConstants.EVENT_TYPE_WORKFLOW_TERMINATED, NotificationEvent.Level.SUCCESS, getReferenceObject(workflow));
			getWorkflowEventPublisher().publishWorkflowUpdatedEvent(workflow);
			getWorkflowEventPublisher().publishWorkflowActionsUpdatedEvent(workflow.getActions());
		}
		else
		{
			getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE,
					WorkflowConstants.EVENT_TYPE_WORKFLOW_TERMINATED, NotificationEvent.Level.FAILURE, getReferenceObject(workflow));
		}
		return terminated;
	}

	protected Map<String, Object> getReferenceObject(final WorkflowModel workflow)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put(WorkflowConstants.EVENT_REFERENCE_OBJECT_WORKFLOW_KEY, workflow);
		map.put(WorkflowConstants.WIZARD_WORKFLOW_CTX_DESTINATION, WorkflowConstants.EVENT_LINK_WORKFLOW_DETAILS_DESTINATION);
		return map;
	}

	/**
	 * @deprecated since 6.7, use the
	 *             {@link NotificationService#notifyUser(String , String, NotificationEvent.Level, Object...)} instead.
	 */
	@Deprecated
	protected void notifyUser(final WorkflowModel workflow, final String eventType, final NotificationEvent.Level level)
	{
		getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE, eventType, level, workflow);
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	@Required
	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}

	public WorkflowEventPublisher getWorkflowEventPublisher()
	{
		return workflowEventPublisher;
	}

	@Required
	public void setWorkflowEventPublisher(final WorkflowEventPublisher workflowEventPublisher)
	{
		this.workflowEventPublisher = workflowEventPublisher;
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
