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
package com.hybris.backoffice.widgets.workflowdetails.renderer;

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.workflows.WorkflowsController;
import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.config.summaryview.jaxb.Attribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.summaryview.renderer.AbstractSummaryViewItemWithIconRenderer;


public class DefaultWorkflowDetailsSummaryCurrentTasksRenderer extends AbstractSummaryViewItemWithIconRenderer<WorkflowModel>
{

	@WireVariable
	private WorkflowFacade workflowFacade;

	protected static final String LABEL_WORKFLOW_DETAILS_NOACTIVETASK = "workflow.name.currenttask.notask";
	protected static final String SCLASS_ACTION_ROW_BUTTON = "ye-text-button yw-summaryview-workflow-task-button";

	@Override
	protected String getIconStatusSClass(final HtmlBasedComponent iconContainer, final Attribute attributeConfiguration,
			final WorkflowModel data, final DataAttribute dataAttribute, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		return getIconStatusSClass("workflows", "tasks");
	}

	@Override
	protected void renderValue(final Div attributeContainer, final Attribute attributeConfiguration, final WorkflowModel data,
			final DataAttribute dataAttribute, final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		final List<WorkflowActionModel> actions = workflowFacade.getCurrentTasks(data);
		if (actions.isEmpty())
		{
			attributeContainer.appendChild(new Label(Labels.getLabel(LABEL_WORKFLOW_DETAILS_NOACTIVETASK)));
			return;
		}

		actions.forEach((final WorkflowActionModel t) -> attributeContainer.appendChild(getActionRow(t, widgetInstanceManager)));
	}

	protected Button getActionRow(final WorkflowActionModel workflowAction, final WidgetInstanceManager widgetInstanceManager)
	{
		final Button button = new Button(workflowAction.getName());
		button.setSclass(SCLASS_ACTION_ROW_BUTTON);
		button.addEventListener(Events.ON_CLICK,
				e -> widgetInstanceManager.sendOutput(WorkflowsController.SOCKET_OUT_WORKFLOW_ACTION_SELECTED, workflowAction));
		return button;
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

}
