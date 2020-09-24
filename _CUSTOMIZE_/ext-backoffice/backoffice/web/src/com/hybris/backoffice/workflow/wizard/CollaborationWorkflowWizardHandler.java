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
package com.hybris.backoffice.workflow.wizard;

import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class CollaborationWorkflowWizardHandler implements FlowActionHandler
{
	protected static final String MODEL_WORKFLOW_FORM = "workflowForm";
	protected static final String PARAM_START_WORKFLOW = "startWorkflow";
	protected static final String EVENT_TYPE_WORKFLOW_INCORRECT_ASSIGNEE = "AdHocWorkflowInCorrectAssignee";

	private WorkflowFacade workflowFacade;
	private WorkflowEventPublisher workflowEventPublisher;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final CollaborationWorkflowWizardForm wizardForm = getWizardFormFrom(adapter);

		if (CollectionUtils.isEmpty(wizardForm.getAttachments()) && shouldStartWorkflow(parameters))
		{
			notifyUser(null, WorkflowConstants.EVENT_TYPE_WORKFLOW_WITHOUT_ATTACHMENTS, NotificationEvent.Level.FAILURE);
			return;
		}

		final Optional<WorkflowModel> workflow = createWorkflow(wizardForm);

		if (workflow.isPresent())
		{
			final Map<String, Object> result = new HashMap<>();
			result.put(MODEL_WORKFLOW_FORM, workflow.get());
			adapter.getWidgetInstanceManager().sendOutput(ConfigurableFlowController.SOCKET_WIZARD_RESULT, result);
			adapter.custom();

			final boolean started = startWorkflow(workflow.get(), parameters);
			if (started)
			{
				getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE,
						WorkflowConstants.EVENT_TYPE_WORKFLOW_CREATED_AND_STARTED, NotificationEvent.Level.SUCCESS, getReferenceObject(workflow.get(), getDestination(adapter)));
			}
			else
			{
				getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE,
						WorkflowConstants.EVENT_TYPE_WORKFLOW_CREATED, NotificationEvent.Level.SUCCESS, getReferenceObject(workflow.get(), getDestination(adapter)));
			}

			workflowEventPublisher.publishWorkflowUpdatedEvent(workflow.get());
			workflowEventPublisher.publishWorkflowActionsUpdatedEvent(workflow.get().getActions());
		}
	}

	protected Optional<WorkflowModel> createWorkflow(final CollaborationWorkflowWizardForm wizardForm)
	{

		if (workflowFacade.isAdHocTemplate(wizardForm.getWorkflowTemplate()))
		{
			return createAdHocWorkflow(wizardForm);
		}
		else
		{
			return workflowFacade.createWorkflow(wizardForm.getWorkflowTemplate(), wizardForm.getName(), wizardForm.getDescription(),
					wizardForm.getAttachments());
		}
	}

	protected Optional<WorkflowModel> createAdHocWorkflow(final CollaborationWorkflowWizardForm wizardForm)
	{
		if (workflowFacade.isCorrectAdHocAssignee(wizardForm.getAssignee()))
		{
			final Optional<WorkflowModel> adHocWorkflow = workflowFacade.createAdHocWorkflow(wizardForm.getAssignee(),
					wizardForm.getName(), wizardForm.getDescription(), wizardForm.getAttachments());

			if (adHocWorkflow.isPresent())
			{
				return Optional.of(adHocWorkflow.get());
			}
			else
			{
				notifyUser(null, WorkflowConstants.EVENT_TYPE_WORKFLOW_CREATED, NotificationEvent.Level.FAILURE);
			}
		}
		else
		{
			notifyUser(null, EVENT_TYPE_WORKFLOW_INCORRECT_ASSIGNEE, NotificationEvent.Level.FAILURE);
		}
		return Optional.empty();
	}

	protected boolean shouldStartWorkflow(final Map<String, String> parameters)
	{
		return Boolean.parseBoolean(parameters.getOrDefault(PARAM_START_WORKFLOW, Boolean.FALSE.toString()));
	}

	protected boolean startWorkflow(final WorkflowModel workflow, final Map<String, String> parameters)
	{
		final boolean startWorkflow = shouldStartWorkflow(parameters);
		if (startWorkflow)
		{
			final boolean workflowCanBeStarted = workflowFacade.canBeStarted(workflow);
			if (workflowCanBeStarted && workflowFacade.startWorkflow(workflow))
			{
				return true;
			}
			else
			{
				notifyUser(workflow, WorkflowConstants.EVENT_TYPE_WORKFLOW_STARTED, NotificationEvent.Level.FAILURE);
			}
		}
		return false;
	}

	protected Map<String, Object> getReferenceObject(final WorkflowModel workflow, final String destination)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put(WorkflowConstants.EVENT_REFERENCE_OBJECT_WORKFLOW_KEY, workflow);
		map.put(WorkflowConstants.WIZARD_WORKFLOW_CTX_DESTINATION, destination);
		return map;
	}

	protected String getDestination(final FlowActionHandlerAdapter adapter)
	{
		final Object destination = adapter.getWidgetInstanceManager().getModel().getValue("ctx", Map.class)
				.get(WorkflowConstants.WIZARD_WORKFLOW_CTX_DESTINATION);
		if (destination instanceof String)
		{
			return (String) destination;
		}

		return WorkflowConstants.EVENT_LINK_DEFAULT_DESTINATION;
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

	protected CollaborationWorkflowWizardForm getWizardFormFrom(final FlowActionHandlerAdapter adapter)
	{
		return adapter.getWidgetInstanceManager().getModel().getValue(MODEL_WORKFLOW_FORM, CollaborationWorkflowWizardForm.class);
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
