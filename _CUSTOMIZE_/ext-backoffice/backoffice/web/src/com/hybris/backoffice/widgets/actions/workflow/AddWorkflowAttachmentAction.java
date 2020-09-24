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
package com.hybris.backoffice.widgets.actions.workflow;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectCRUDHandler;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.events.SocketEvent;


/**
 * Action which allows to add attachments to the workflow.
 */
public class AddWorkflowAttachmentAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<WorkflowModel, Object>
{
	protected static final String APPEND_NOTIFICATION_KEY = "dragAndDropItemToWorkflowAppend";
	protected static final String NOTIFICATION_KEY_WARNING_SUFFIX = "Warning";
	protected static final String NOTIFICATION_KEY_SINGLE_SUFFIX = "Single";
	protected static final String NOTIFICATION_KEY_SUCCESS_SUFFIX = "Success";
	protected static final String NOTIFICATION_KEY_MULTIPLE_SUFFIX = "Multiple";

	protected static final String SOCKET_OUT_REFERENCE_SEARCH_CTX = "referenceSearchCtx";
	protected static final String SOCKET_OUT_UPDATED_WORKFLOW = "updatedWorkflow";
	protected static final String SOCKET_IN_CHOSEN_ATTACHMENTS = "referenceSearchResult";
	public static final String PARAM_ATTACHMENT_TYPE = "attachmentType";

	@Resource
	private WorkflowFacade workflowFacade;
	@Resource
	private CockpitEventQueue cockpitEventQueue;
	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<WorkflowModel> actionContext)
	{
		final WorkflowModel workflow = actionContext.getData();
		final String attachmentType = (String) actionContext.getParameter(PARAM_ATTACHMENT_TYPE);
		if (workflow == null || StringUtils.isEmpty(attachmentType))
		{
			return new ActionResult<>(ActionResult.ERROR);
		}

		addSocketInputEventListener(SOCKET_IN_CHOSEN_ATTACHMENTS,
				(final SocketEvent socketEvent) -> onChosenAttachments(workflow, (List<ItemModel>) socketEvent.getData()));
		sendOutput(SOCKET_OUT_REFERENCE_SEARCH_CTX, createSearchContext(workflow, attachmentType));
		return new ActionResult<>(ActionResult.SUCCESS);
	}

	protected TypeAwareSelectionContext<ItemModel> createSearchContext(final WorkflowModel workflow, final String attachmentType)
	{
		final TypeAwareSelectionContext<ItemModel> typeAwareSelectionContext = new TypeAwareSelectionContext<>(null,
				getItemsFromAttachments(workflow));

		typeAwareSelectionContext.setTypeCode(attachmentType);
		typeAwareSelectionContext.setMultiSelect(true);
		return typeAwareSelectionContext;
	}

	public void onChosenAttachments(final WorkflowModel workflow, final List<ItemModel> attachments)
	{
		if (CollectionUtils.isNotEmpty(attachments))
		{
			final List<ItemModel> toBeAttached = getNotAttachedItems(workflow, attachments);

			if (CollectionUtils.isNotEmpty(toBeAttached))
			{
				getWorkflowFacade().addItems(workflow, toBeAttached);
				sendOutput(SOCKET_OUT_UPDATED_WORKFLOW, workflow);
				notifyAttachmentsOperation(toBeAttached, NotificationEvent.Level.SUCCESS);
				getCockpitEventQueue().publishEvent(new DefaultCockpitEvent(ObjectCRUDHandler.OBJECTS_UPDATED_EVENT, workflow, null));
			}
		}
	}

	protected List<ItemModel> getNotAttachedItems(final WorkflowModel workflow, final List<ItemModel> toBeAttached)
	{
		final List<ItemModel> attached = getItemsFromAttachments(workflow);

		final Collection<ItemModel> duplicates = CollectionUtils.intersection(attached, toBeAttached);
		final Collection<ItemModel> distinct = CollectionUtils.subtract(toBeAttached, duplicates);

		notifyAttachmentsOperation(duplicates, NotificationEvent.Level.WARNING);
		return new ArrayList<>(distinct);
	}

	protected void notifyAttachmentsOperation(final Collection<ItemModel> duplicates, final NotificationEvent.Level level)
	{
		if (CollectionUtils.isNotEmpty(duplicates))
		{
			if (duplicates.size() > 1)
			{
				notifyMultiple(duplicates, level);
			}
			else
			{
				notifySingle(Iterables.get(duplicates, 0), level);
			}
		}
	}

	protected List<ItemModel> getItemsFromAttachments(final WorkflowModel workflow)
	{
		return workflow.getAttachments().stream().map(WorkflowItemAttachmentModel::getItem).collect(Collectors.toList());
	}

	protected void notifySingle(final ItemModel existed, final NotificationEvent.Level level)
	{
		final String source = APPEND_NOTIFICATION_KEY + getLevelSourceSuffix(level) + NOTIFICATION_KEY_SINGLE_SUFFIX;
		getNotificationService().notifyUser(source, NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, level, existed);
	}

	protected void notifyMultiple(final Collection<ItemModel> existed, final NotificationEvent.Level level)
	{
		final String source = APPEND_NOTIFICATION_KEY + getLevelSourceSuffix(level) + NOTIFICATION_KEY_MULTIPLE_SUFFIX;
		getNotificationService().notifyUser(source, NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, level, existed);
	}

	protected String getLevelSourceSuffix(final NotificationEvent.Level level)
	{
		return level == NotificationEvent.Level.WARNING ? NOTIFICATION_KEY_WARNING_SUFFIX : NOTIFICATION_KEY_SUCCESS_SUFFIX;
	}

	@Override
	public boolean canPerform(final ActionContext<WorkflowModel> ctx)
	{
		return ctx.getData() != null && getWorkflowFacade().getWorkflowStatus(ctx.getData()) == WorkflowStatus.PLANNED;
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
