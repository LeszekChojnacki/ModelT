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

import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.backoffice.workflow.constants.WorkflowNotificationEventTypes;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectDeletionException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;


public class WorkflowAttachmentDeleteActionExecutor implements Consumer<WorkflowItemAttachmentModel>
{
	private static final Logger LOGGER = Logger.getLogger(WorkflowAttachmentDeleteActionExecutor.class);

	private ObjectFacade objectFacade;
	private WorkflowEventPublisher workflowEventPublisher;
	private NotificationService notificationService;

	@Override
	public void accept(final WorkflowItemAttachmentModel workflowAttachment)
	{
		try
		{
			final WorkflowModel workflow = workflowAttachment.getWorkflow();
			getObjectFacade().delete(workflowAttachment);
			notifyUser(workflowAttachment, WorkflowNotificationEventTypes.NOTIFICATION_EVENT_WORKFLOW_ATTACHMENT_DELETED,
					NotificationEvent.Level.SUCCESS);
			getWorkflowEventPublisher().publishWorkflowAttachmentDeletedEvent(workflowAttachment);
			getWorkflowEventPublisher().publishWorkflowUpdatedEvent(getObjectFacade().reload(workflow));
		}
		catch (final ObjectDeletionException | ObjectNotFoundException e)
		{
			LOGGER.error(e);
			notifyUser(workflowAttachment, WorkflowNotificationEventTypes.NOTIFICATION_EVENT_WORKFLOW_ATTACHMENT_DELETED,
					NotificationEvent.Level.FAILURE);
		}
	}

	/**
	 * @deprecated since 6.7, use the
	 *             {@link #NotificationService.notifyUser(String , String, NotificationEvent.Level, Object...) } instead.
	 */
	@Deprecated
	protected void notifyUser(final WorkflowItemAttachmentModel workflowAttachment, final String eventType,
			final NotificationEvent.Level level)
	{
		getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE, eventType, level,
				workflowAttachment.getItem());
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
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
