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

import java.util.function.Function;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectDeletionException;


public class WorkflowDeleteActionExecutor implements Function<WorkflowModel, Boolean>
{
	private static final Logger LOGGER = Logger.getLogger(WorkflowDeleteActionExecutor.class);

	private WorkflowFacade workflowFacade;
	private WorkflowEventPublisher workflowEventPublisher;
	private NotificationService notificationService;

	@Override
	public Boolean apply(final WorkflowModel workflow)
	{
		try
		{
			getWorkflowFacade().deleteWorkflow(workflow);
			notifyUser(workflow, WorkflowConstants.EVENT_TYPE_WORKFLOW_DELETED, NotificationEvent.Level.SUCCESS);
			getWorkflowEventPublisher().publishWorkflowActionsDeletedEvent(workflow.getActions());
			return true;
		}
		catch (final ObjectDeletionException ex)
		{
			LOGGER.error(ex);
			notifyUser(workflow, WorkflowConstants.EVENT_TYPE_WORKFLOW_DELETED, NotificationEvent.Level.FAILURE);
			return false;
		}
	}

	/**
	 * @deprecated since 6.7, please use the
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
