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

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowEventPublisher;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.util.CockpitGlobalEventPublisher;


/**
 * Default implementation for WorkflowEventPublisher
 */
public class DefaultWorkflowEventPublisher implements WorkflowEventPublisher
{
	private CockpitGlobalEventPublisher eventPublisher;
	static final String SHOULD_RELOAD_AFTER_UPDATE = "shouldReloadAfterUpdate";

	@Override
	public void publishWorkflowUpdatedEvent(final WorkflowModel workflowModel, final Context context)
	{
		getEventPublisher().publish(ObjectFacade.OBJECTS_UPDATED_EVENT, workflowModel, context);
		getEventPublisher().publish(ObjectFacade.OBJECTS_UPDATED_EVENT, workflowModel.getActions(), context);
	}

	@Override
	public void publishWorkflowUpdatedEvent(final WorkflowModel workflowModel)
	{
		final Context context = new DefaultContext.Builder().attribute(SHOULD_RELOAD_AFTER_UPDATE, true).build();
		publishWorkflowUpdatedEvent(workflowModel, context);
	}

	@Override
	public void publishWorkflowActionsUpdatedEvent(final List<WorkflowActionModel> workflowActions)
	{
		getEventPublisher().publish(ObjectFacade.OBJECTS_UPDATED_EVENT, workflowActions, new DefaultContext.Builder().build());
	}

	@Override
	public void publishWorkflowActionsDeletedEvent(final List<WorkflowActionModel> workflowActions)
	{
		getEventPublisher().publish(ObjectFacade.OBJECTS_DELETED_EVENT, workflowActions, new DefaultContext.Builder().build());
	}

	@Override
	public void publishWorkflowAttachmentDeletedEvent(final WorkflowItemAttachmentModel workflowAttachment)
	{
		getEventPublisher().publish(ObjectFacade.OBJECTS_DELETED_EVENT, workflowAttachment, new DefaultContext.Builder().build());
	}

	protected CockpitGlobalEventPublisher getEventPublisher()
	{
		return eventPublisher;
	}

	@Required
	public void setEventPublisher(final CockpitGlobalEventPublisher eventPublisher)
	{
		this.eventPublisher = eventPublisher;
	}
}
