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

import de.hybris.platform.core.model.ItemModel;

import java.util.List;
import java.util.function.Consumer;

import com.hybris.backoffice.cockpitng.dnd.DropConsumer;


/**
 * Allows to open collaboration wizard with dropped items.
 */
public class WorkflowsDropConsumer implements DropConsumer<ItemModel>
{
	private final Consumer<List<ItemModel>> wizardOpener;

	public WorkflowsDropConsumer(final Consumer<List<ItemModel>> wizardOpener)
	{
		this.wizardOpener = wizardOpener;
	}

	@Override
	public void itemsDropped(final List<ItemModel> droppedItems)
	{
		if (wizardOpener != null)
		{
			wizardOpener.accept(droppedItems);
		}
	}
}
