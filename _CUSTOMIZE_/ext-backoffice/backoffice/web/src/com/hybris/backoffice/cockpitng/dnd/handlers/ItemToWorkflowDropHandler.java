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
package com.hybris.backoffice.cockpitng.dnd.handlers;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Iterables;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropHandler;
import com.hybris.cockpitng.dnd.DropOperationData;


public class ItemToWorkflowDropHandler implements DropHandler<ItemModel, WorkflowModel>
{

	protected static final String APPEND_NOTIFICATION_KEY = "dragAndDropItemToWorkflowAppend";
	protected static final String NOTIFICATION_KEY_WARNING_SUFFIX = "Warning";
	protected static final String NOTIFICATION_KEY_SINGLE_SUFFIX = "Single";
	protected static final String NOTIFICATION_KEY_MULTIPLE_SUFFIX = "Multiple";

	private WorkflowFacade workflowFacade;
	private WorkflowsTypeFacade workflowsTypeFacade;
	private NotificationService notificationService;

	@Override
	public List<String> findSupportedTypes()
	{
		return workflowsTypeFacade.getSupportedAttachmentTypeCodes();
	}

	@Override
	public List<DropOperationData<ItemModel, WorkflowModel, Object>> handleDrop(final List<ItemModel> dragged,
			final WorkflowModel workflowModel, final DragAndDropContext context)
	{
		final List<DropOperationData<ItemModel, WorkflowModel, Object>> result = new ArrayList<>();

		final Collection<ItemModel> existed = CollectionUtils.intersection(
				workflowModel.getAttachments().stream().map(WorkflowItemAttachmentModel::getItem).collect(Collectors.toList()),
				dragged);
		final Collection<ItemModel> notExisted = CollectionUtils.subtract(dragged, existed);

		if (CollectionUtils.isNotEmpty(existed))
		{
			if (existed.size() > 1)
			{
				notifyMultiple(existed);
			}
			else
			{
				notifySingle(existed);
			}
		}

		for (final ItemModel draggedItem : notExisted)
		{
			final List<WorkflowItemAttachmentModel> attachments = workflowFacade.addItems(workflowModel, Arrays.asList(draggedItem));
			workflowModel.setAttachments(attachments);

			final DropOperationData<ItemModel, WorkflowModel, Object> singleResult = new DropOperationData<>(draggedItem,
					workflowModel, draggedItem, context, APPEND_NOTIFICATION_KEY);

			result.add(singleResult);
		}

		return result;
	}

	protected void notifySingle(final Collection<ItemModel> existed)
	{
		getNotificationService().notifyUser(
				APPEND_NOTIFICATION_KEY + NOTIFICATION_KEY_WARNING_SUFFIX + NOTIFICATION_KEY_SINGLE_SUFFIX,
				NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.WARNING, Iterables.get(existed, 0));
	}

	protected void notifyMultiple(final Collection<ItemModel> existed)
	{
		final String suffix = existed.size() > 1 ? NOTIFICATION_KEY_MULTIPLE_SUFFIX : NOTIFICATION_KEY_SINGLE_SUFFIX;
		getNotificationService().notifyUser(APPEND_NOTIFICATION_KEY + NOTIFICATION_KEY_WARNING_SUFFIX + suffix,
				NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.WARNING, existed);
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

	public WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}

	@Required
	public void setWorkflowsTypeFacade(final WorkflowsTypeFacade workflowsTypeFacade)
	{
		this.workflowsTypeFacade = workflowsTypeFacade;
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
