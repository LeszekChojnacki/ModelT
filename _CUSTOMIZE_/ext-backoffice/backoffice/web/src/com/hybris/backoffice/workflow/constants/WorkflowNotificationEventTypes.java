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
package com.hybris.backoffice.workflow.constants;

public class WorkflowNotificationEventTypes
{

	public static final String NOTIFICATION_EVENT_WORKFLOW_DECISION = "WorkflowDecision";
	public static final String NOTIFICATION_EVENT_WORKFLOW_TERMINATED = "WorkflowTerminated";
	public static final String NOTIFICATION_EVENT_WORKFLOW_STARTED = "WorkflowStarted";
	public static final String NOTIFICATION_EVENT_WORKFLOW_DELETED = "WorkflowDeleted";
	public static final String NOTIFICATION_EVENT_WORKFLOW_ATTACHMENT_DELETED = "WorkflowAttachmentDeleted";

	private WorkflowNotificationEventTypes()
	{
	}

}
