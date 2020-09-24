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
package com.hybris.backoffice.workflow;

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.List;

import com.hybris.cockpitng.dataaccess.context.Context;


/**
 * Used to send global cockpit notification about updates done on Workflows and their Actions
 */
public interface WorkflowEventPublisher
{
	/**
	 * Sends globally an event which informs that Workflow has been updated.
	 *
	 * @param workflowModel
	 *           updated Workflow
	 * @param context
	 *           passed context
	 */
	void publishWorkflowUpdatedEvent(final WorkflowModel workflowModel, final Context context);

	/**
	 * Sends globally an event which informs that Workflow has been updated.
	 * 
	 * @param workflowModel
	 *           updated Workflow
	 */
	void publishWorkflowUpdatedEvent(final WorkflowModel workflowModel);

	/**
	 * Sends globally an event that WorkflowActions have been updated.
	 *
	 * @param workflowActions
	 *           updated WorkflowActions
	 */
	void publishWorkflowActionsUpdatedEvent(final List<WorkflowActionModel> workflowActions);

	/**
	 * Sends globally an event that WorkflowActions have been updated.
	 *
	 * @param workflowActions
	 *           deleted WorkflowActions
	 */
	void publishWorkflowActionsDeletedEvent(final List<WorkflowActionModel> workflowActions);

	/**
	 * Sends globally an event that WorkflowAttachment has been deleted.
	 * @param workflowAttachment
	 */
	void publishWorkflowAttachmentDeletedEvent(WorkflowItemAttachmentModel workflowAttachment);

}
