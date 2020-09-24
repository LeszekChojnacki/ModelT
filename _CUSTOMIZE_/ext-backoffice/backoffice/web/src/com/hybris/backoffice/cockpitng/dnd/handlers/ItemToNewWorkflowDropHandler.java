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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dnd.DropConsumer;
import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DropHandler;
import com.hybris.cockpitng.dnd.DropOperationData;


public class ItemToNewWorkflowDropHandler implements DropHandler<ItemModel, DropConsumer<ItemModel>>
{
	private WorkflowsTypeFacade workflowsTypeFacade;

	@Override
	public List<String> findSupportedTypes()
	{
		return workflowsTypeFacade.getSupportedAttachmentTypeCodes();
	}

	@Override
	public List<DropOperationData<ItemModel, DropConsumer<ItemModel>, Object>> handleDrop(final List<ItemModel> droppedItems,
			final DropConsumer<ItemModel> dropConsumer, final DragAndDropContext dragAndDropContext)
	{
		dropConsumer.itemsDropped(droppedItems);
		return new ArrayList<>();
	}

	@Required
	public void setWorkflowsTypeFacade(final WorkflowsTypeFacade workflowsTypeFacade)
	{
		this.workflowsTypeFacade = workflowsTypeFacade;
	}

	public WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}
}
