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
package com.hybris.backoffice.workflow.renderer;

import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Map;
import java.util.Optional;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.core.model.ValueObserver;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.YTestTools;
import com.hybris.cockpitng.widgets.configurableflow.renderer.DefaultCustomViewRenderer;


public class WorkflowShowFlowViewRenderer extends DefaultCustomViewRenderer implements ValueObserver
{
	private static final String LABEL_WORKFLOWS_ACTION_SHOW_FLOW = "workflow.action.showflow";
	private static final String SOCKET_OUTPUT_SHOW_FLOW = "showFlow";
	private static final String MODEL_VALUE_NAME = "workflowForm.workflowTemplate";
	private static final String MODEL_LINK = "WorkflowShowFlowViewRenderer_LINK";
	private static final String SCLASS_WORKFLOW_VIEW_LINK = "yw-workflowview-link";
	private static final String SCLASS_WORKFLOW_VIEW_LINK_CONTAINER = "yw-workflowview-link-container";

	private WidgetInstanceManager widgetInstanceManager;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;

		final Button link = getOrCreateLink(widgetInstanceManager);

		final Div linkContainer = new Div();
		linkContainer.setSclass(SCLASS_WORKFLOW_VIEW_LINK_CONTAINER);
		linkContainer.appendChild(link);

		parent.appendChild(linkContainer);

		widgetInstanceManager.getModel().addObserver(MODEL_VALUE_NAME, this); // multiple observer registrations are not performed
	}

	@Override
	public void modelChanged()
	{
		if (widgetInstanceManager == null)
		{
			return;
		}
		final Button link = getLink(widgetInstanceManager);
		if (link != null)
		{
			disableLinkOnNullWorkflowTemplate(link);
		}
	}

	protected void disableLinkOnNullWorkflowTemplate(final Button link)
	{
		link.setDisabled(getWorkflowTemplate() == null);
	}

	protected Button getOrCreateLink(final WidgetInstanceManager widgetInstanceManager)
	{
		return Optional //
				.ofNullable(getLink(widgetInstanceManager)) //
				.orElseGet(() -> createLink(widgetInstanceManager));
	}

	protected Button getLink(final WidgetInstanceManager widgetInstanceManager)
	{
		return widgetInstanceManager.getModel().getValue(MODEL_LINK, Button.class);
	}

	protected Button createLink(final WidgetInstanceManager widgetInstanceManager)
	{
		final Button link = new Button(Labels.getLabel(LABEL_WORKFLOWS_ACTION_SHOW_FLOW));
		disableLinkOnNullWorkflowTemplate(link);
		link.addEventListener(Events.ON_CLICK, ignoredEvent -> onLinkClick());
		link.setSclass(SCLASS_WORKFLOW_VIEW_LINK);
		YTestTools.modifyYTestId(link, SCLASS_WORKFLOW_VIEW_LINK);
		widgetInstanceManager.getModel().setValue(MODEL_LINK, link);
		return link;
	}

	protected void onLinkClick()
	{
		widgetInstanceManager.sendOutput(SOCKET_OUTPUT_SHOW_FLOW, getWorkflowTemplate());
	}

	protected WorkflowTemplateModel getWorkflowTemplate()
	{
		return widgetInstanceManager.getModel().getValue(MODEL_VALUE_NAME, WorkflowTemplateModel.class);
	}
}
