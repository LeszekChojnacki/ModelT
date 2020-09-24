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
package com.hybris.backoffice.renderer;


import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowModel;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Span;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.CockpitComponentsUtils;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public class WorkflowStatusRenderer<PARENT extends HtmlBasedComponent, CONFIG>
		extends AbstractWidgetComponentRenderer<PARENT, CONFIG, WorkflowModel>
{

	private static final String SCLASS_WORKFLOW_STATUS_CONTAINER = "yw-workflow-status-container";
	private static final String SCLASS_WORKFLOW_STATUS_LABEL = "yw-workflow-status-label";
	private static final String SCLASS_WORKFLOW_STATUS_ICON_PATTERN = "yw-workflow-status-icon yw-workflow-status-icon-%s";
	private static final String LABEL_KEY_WORKFLOW_STATUS_PATTERN = "workflow.status.%s";
	protected static final String WORKFLOW_STATUS_UNKNOWN = "UNKNOWN";

	private WorkflowFacade workflowFacade;


	@Override
	public void render(final PARENT parent, final CONFIG config, final WorkflowModel workflow, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final WorkflowStatus workflowStatus = workflowFacade.getWorkflowStatus(workflow);

		final Div statusContainer = new Div();
		UITools.addSClass(statusContainer, SCLASS_WORKFLOW_STATUS_CONTAINER);

		final HtmlBasedComponent icon = createStatusIconComponent(workflowStatus);
		statusContainer.appendChild(icon);
		fireComponentRendered(icon, parent, config, workflow);

		final HtmlBasedComponent workflowStatusLabel = createStatusLabelComponent(workflowStatus, widgetInstanceManager);
		statusContainer.appendChild(workflowStatusLabel);
		fireComponentRendered(workflowStatusLabel, parent, config, workflow);

		parent.appendChild(statusContainer);
		fireComponentRendered(parent, config, workflow);
	}

	protected HtmlBasedComponent createStatusIconComponent(final WorkflowStatus workflowStatus)
	{
		final Span icon = new Span();
		UITools.addSClass(icon, getWorkflowStatusIconCssClass(workflowStatus));
		return icon;
	}

	protected String getWorkflowStatusIconCssClass(final WorkflowStatus workflowStatus)
	{
		return String.format(SCLASS_WORKFLOW_STATUS_ICON_PATTERN, getWorkflowStatusName(workflowStatus));
	}

	protected HtmlBasedComponent createStatusLabelComponent(final WorkflowStatus workflowStatus,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Label workflowStatusLabel = new Label(CockpitComponentsUtils.getLabel(widgetInstanceManager,
				String.format(LABEL_KEY_WORKFLOW_STATUS_PATTERN, getWorkflowStatusName(workflowStatus))));
		UITools.addSClass(workflowStatusLabel, SCLASS_WORKFLOW_STATUS_LABEL);
		return workflowStatusLabel;
	}

	protected String getWorkflowStatusName(final WorkflowStatus workflowStatus)
	{
		return (workflowStatus != null ? workflowStatus.name() : WORKFLOW_STATUS_UNKNOWN).toLowerCase();
	}

	protected WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	@Required
	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}

}
