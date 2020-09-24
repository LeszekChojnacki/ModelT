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
package com.hybris.backoffice.workflow.impl;

import static com.hybris.backoffice.workflow.constants.WorkflowNotificationEventTypes.NOTIFICATION_EVENT_WORKFLOW_DECISION;
import static com.hybris.backoffice.workflow.constants.WorkflowNotificationEventTypes.NOTIFICATION_EVENT_WORKFLOW_TERMINATED;

import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.exceptions.ActivationWorkflowActionException;
import de.hybris.platform.workflow.exceptions.WorkflowActionDecideException;
import de.hybris.platform.workflow.exceptions.WorkflowTerminatedException;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowDecisionMaker;
import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultWorkflowDecisionMaker implements WorkflowDecisionMaker
{
	/**
	 * A parameter passed to global notification context containing an decision that has been made
	 */
	public static final String CONTEXT_PARAMETER_DECISION = "decision";

	private WorkflowProcessingService workflowProcessingService;
	private WorkflowEventPublisher workflowEventPublisher;
	private NotificationService notificationService;

	@Override
	public void makeDecision(final WorkflowActionModel workflowAction, final WorkflowDecisionModel selectedDecision,
			final WidgetInstanceManager widgetInstanceManager)
	{
		Validate.notNull("Selected decision cannot be null", selectedDecision);
		Validate.notNull("Workflow action cannot be null", workflowAction);
		try
		{
			getWorkflowProcessingService().decideAction(workflowAction, selectedDecision);
			getNotificationService().notifyUser(getNotificationSource(widgetInstanceManager), NOTIFICATION_EVENT_WORKFLOW_DECISION,
					NotificationEvent.Level.SUCCESS, selectedDecision, workflowAction);

			final Context eventPublisherContext = new DefaultContext.Builder()
					.attribute(CONTEXT_PARAMETER_DECISION, selectedDecision)
					.attribute(DefaultWorkflowEventPublisher.SHOULD_RELOAD_AFTER_UPDATE, true).build();
			getWorkflowEventPublisher().publishWorkflowUpdatedEvent(workflowAction.getWorkflow(), eventPublisherContext);
		}
		catch (final WorkflowActionDecideException | ActivationWorkflowActionException decideException)
		{
			getNotificationService().notifyUser(getNotificationSource(widgetInstanceManager), NOTIFICATION_EVENT_WORKFLOW_DECISION,
					NotificationEvent.Level.FAILURE, decideException, selectedDecision, workflowAction);
		}
		catch (final WorkflowTerminatedException terminatedException)
		{
			getNotificationService().notifyUser(getNotificationSource(widgetInstanceManager), NOTIFICATION_EVENT_WORKFLOW_TERMINATED,
					NotificationEvent.Level.FAILURE, terminatedException, selectedDecision, workflowAction);
		}
	}

	/**
	 * @deprecated since 6.7, use {@link NotificationService#getWidgetNotificationSource(WidgetInstanceManager)} instead.
	 */
	@Deprecated
	protected String getNotificationSource(final WidgetInstanceManager widgetInstanceManager)
	{
		return getNotificationService().getWidgetNotificationSource(widgetInstanceManager);
	}

	protected WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	@Required
	public void setWorkflowProcessingService(final WorkflowProcessingService workflowProcessingService)
	{
		this.workflowProcessingService = workflowProcessingService;
	}

	protected WorkflowEventPublisher getWorkflowEventPublisher()
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
