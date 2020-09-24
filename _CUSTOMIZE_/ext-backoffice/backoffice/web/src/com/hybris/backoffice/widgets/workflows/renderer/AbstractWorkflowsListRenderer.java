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
package com.hybris.backoffice.widgets.workflows.renderer;

import de.hybris.platform.workflow.model.WorkflowModel;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import com.hybris.backoffice.widgets.workflows.WorkflowsController;
import com.hybris.cockpitng.common.renderer.AbstractCustomMenuActionRenderer;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;


public abstract class AbstractWorkflowsListRenderer<PARENT extends Listitem, CONFIG, DATA extends WorkflowModel>
		extends AbstractWidgetComponentRenderer<PARENT, CONFIG, DATA>
{
	protected static final String SCLASS_WORKFLOWS_LIST_CONTENT = "yw-workflows-list-content";
	protected static final String SCLASS_WORKFLOWS_LIST_CONTENT_BOTTOM = "yw-workflows-list-content-bottom";
	protected static final String SCLASS_WORKFLOWS_LIST_CONTENT_TOP = "yw-workflows-list-content-top";
	protected static final String SCLASS_WORKFLOWS_LIST_ITEM = "yw-workflows-list-item";
	protected static final String SCLASS_WORKFLOWS_LIST_TITLE = "yw-workflows-list-title";
	protected static final String SCLASS_TEXT_BUTTON = "ye-text-button";

	protected static final String LABEL_WORKFLOWS_ATTACHMENT = "workflows.attachment";
	protected static final String LABEL_WORKFLOWS_ATTACHMENTS = "workflows.attachments";

	@Override
	public void render(final PARENT parent, final CONFIG config, final DATA data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Div mainDiv = new Div();
		mainDiv.setSclass(SCLASS_WORKFLOWS_LIST_CONTENT);
		mainDiv.appendChild(createTitle(parent, config, data, dataType, widgetInstanceManager));
		final Div content = createContent(data, dataType, widgetInstanceManager);
		content.setSclass(SCLASS_WORKFLOWS_LIST_CONTENT_BOTTOM);
		mainDiv.appendChild(content);

		final Listcell listcell = new Listcell();
		listcell.appendChild(mainDiv);

		parent.setSclass(SCLASS_WORKFLOWS_LIST_ITEM);
		parent.appendChild(listcell);
	}

	protected abstract Div createContent(final WorkflowModel data, final DataType dataType, final WidgetInstanceManager wim);

	protected void renderThreeDots(final PARENT parent, final CONFIG config, final DATA data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager, final Div div)
	{
		new ProxyRenderer<>(this, parent, config, data).render(getThreeDotsRenderer(), div, config, data, dataType,
				widgetInstanceManager);
	}

	protected abstract AbstractCustomMenuActionRenderer<Div, CONFIG, DATA> getThreeDotsRenderer();

	protected HtmlBasedComponent createTitle(final PARENT parent, final CONFIG config, final DATA data, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Div topContent = new Div();
		topContent.setSclass(SCLASS_WORKFLOWS_LIST_CONTENT_TOP);
		final Button button = createTitleButton(data, widgetInstanceManager);

		final Div threeDots = new Div();
		renderThreeDots(parent, config, data, dataType, widgetInstanceManager, threeDots);
		topContent.appendChild(threeDots);
		topContent.appendChild(button);
		return topContent;
	}

	protected Button createTitleButton(final DATA data, final WidgetInstanceManager widgetInstanceManager)
	{
		final Button button = new Button();
		button.setSclass(SCLASS_WORKFLOWS_LIST_TITLE + " " + SCLASS_TEXT_BUTTON);
		button.setLabel(getWorkflowName(data));
		button.addEventListener(Events.ON_CLICK, event -> onTitleClick(data, widgetInstanceManager));
		return button;
	}

	protected String getWorkflowName(final DATA data)
	{
		return data.getName();
	}

	protected void onTitleClick(final WorkflowModel data, final WidgetInstanceManager wim)
	{
		wim.sendOutput(WorkflowsController.SOCKET_OUT_WORKFLOW_SELECTED, data);
	}

	protected void updateNoOfAttachmentsLabel(final WidgetInstanceManager wim, final WorkflowModel model,
			final Label noOfAttachmentsLabel)
	{
		final long attachmentsSize = model.getAttachments().stream().filter(e -> e.getItem() != null).count();
		final String labelKey = attachmentsSize == 1 ? LABEL_WORKFLOWS_ATTACHMENT : LABEL_WORKFLOWS_ATTACHMENTS;
		noOfAttachmentsLabel.setValue(getAttachmentsLabelValue(labelKey, Long.valueOf(attachmentsSize)));
	}

	protected String getAttachmentsLabelValue(final String labelKey, final Long noOfAttachments)
	{
		return Labels.getLabel(labelKey, new String[]
		{ String.valueOf(noOfAttachments) });
	}
}
