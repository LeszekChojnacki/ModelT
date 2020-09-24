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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.dnd.BackofficeDragAndDropContext;
import com.hybris.cockpitng.dnd.DragAndDropActionType;
import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropHandler;
import com.hybris.cockpitng.dnd.DropOperationData;
import com.hybris.cockpitng.mouse.MouseKeys;
import com.hybris.cockpitng.services.dnd.DragAndDropConfigurationService;


/**
 * Helper class facilitates implementation of drag and drop handlers. It provides ability to distinguish the type of
 * drag and drop action performed.
 *
 * @param <DRAGGED>
 *           dragged objects type.
 * @param <TARGET>
 *           target object type.
 * @see com.hybris.cockpitng.dnd.DragAndDropActionType
 */
public abstract class AbstractReferenceDropHandler<DRAGGED, TARGET> implements DropHandler<DRAGGED, TARGET>
{
	private DragAndDropConfigurationService dragAndDropConfigurationService;

	@Override
	public List<DropOperationData<DRAGGED, TARGET, Object>> handleDrop(final List<DRAGGED> dragged, final TARGET target,
			final DragAndDropContext context)
	{
		final DragAndDropActionType actionType = resolveActionType(context);

		if (context instanceof BackofficeDragAndDropContext)
		{
			((BackofficeDragAndDropContext) context).setActionType(actionType);
		}

		if (DragAndDropActionType.REPLACE.equals(actionType))
		{
			return handleReplace(dragged, target, context);
		}
		else
		{
			return handleAppend(dragged, target, context);
		}
	}

	protected DragAndDropActionType resolveActionType(final DragAndDropContext context)
	{
		final Collection<MouseKeys> keys = context.getKeys();
		DragAndDropActionType action = null;
		if (keys != null && keys.contains(MouseKeys.ALT_KEY))
		{
			if (keys.contains(MouseKeys.CTRL_KEY) || keys.contains(MouseKeys.META_KEY))
			{
				action = DragAndDropActionType.REPLACE;
			}
			else
			{
				action = DragAndDropActionType.APPEND;
			}
		}
		return Optional.ofNullable(action).orElseGet(getDragAndDropConfigurationService()::getDefaultActionType);
	}

	/**
	 * Returns the result of appending of the dragged object to the target.
	 *
	 * @param dragged
	 *           dragged objects.
	 * @param target
	 *           target object on which dragged objects are dropped.
	 * @param context
	 *           context of drag&drop operation.
	 */
	protected abstract List<DropOperationData<DRAGGED, TARGET, Object>> handleAppend(List<DRAGGED> dragged, TARGET target,
			DragAndDropContext context);

	/**
	 * Returns the result of replacing of the dragged object in the target.
	 *
	 * @param dragged
	 *           dragged objects.
	 * @param target
	 *           target object on which dragged objects are dropped.
	 * @param context
	 *           context of drag&drop operation.
	 */
	protected abstract List<DropOperationData<DRAGGED, TARGET, Object>> handleReplace(List<DRAGGED> dragged, TARGET target,
			DragAndDropContext context);

	protected DragAndDropConfigurationService getDragAndDropConfigurationService()
	{
		return dragAndDropConfigurationService;
	}

	@Required
	public void setDragAndDropConfigurationService(final DragAndDropConfigurationService dragAndDropConfigurationService)
	{
		this.dragAndDropConfigurationService = dragAndDropConfigurationService;
	}
}
