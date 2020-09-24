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
package com.hybris.backoffice.widgets.workflowactions.renderer;


import de.hybris.platform.workflow.model.WorkflowActionModel;

import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * Renderer which is responsible for rendering workflow action on the list.
 */
public class DashboardWorkflowActionsRenderer extends DefaultWorkflowActionsRenderer
{

	@Override
	public void render(final Listitem listitem, final Object configuration, final WorkflowActionModel data,
			final DataType dataType, final WidgetInstanceManager wim)
	{
		final Div mainDiv = new Div();
		mainDiv.setSclass(SCLASS_WORKFLOW_ACTIONS_LIST_CONTENT);
		mainDiv.appendChild(createTopContent(createTopContentTitle(data), data, wim));

		final Div middleContent = createMiddleContent(wim, data);
		middleContent.setSclass(SCLASS_WORKFLOW_ACTIONS_LIST_CONTENT_MIDDLE);
		mainDiv.appendChild(middleContent);

		final Listcell listcell = new Listcell();
		listcell.appendChild(mainDiv);

		listitem.setSclass(SCLASS_WORKFLOW_ACTIONS_LIST_ITEM);
		listitem.appendChild(listcell);
	}

	@Override
	protected Div createTopContent(final String title, final WorkflowActionModel data, final WidgetInstanceManager wim)
	{
		final Div topContent = new Div();
		topContent.setSclass(SCLASS_WORKFLOW_ACTIONS_LIST_CONTENT_TOP);
		final Button button = createTitleButton(title, data, wim);
		topContent.appendChild(button);

		final Label dateLabel = createDateLabel(data);
		topContent.appendChild(dateLabel);

		final Label noOfAttachmentsLabel = createNoOfAttachmentsLabel(wim, data);
		topContent.appendChild(noOfAttachmentsLabel);

		return topContent;
	}

}
