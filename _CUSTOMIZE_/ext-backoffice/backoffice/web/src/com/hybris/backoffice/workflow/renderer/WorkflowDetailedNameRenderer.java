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

import de.hybris.platform.workflow.WorkflowActionService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.CockpitComponentsUtils;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.collectionbrowser.mold.impl.common.AbstractMoldStrategy;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public class WorkflowDetailedNameRenderer<PARENT extends HtmlBasedComponent, CONFIG>
		extends AbstractWidgetComponentRenderer<PARENT, CONFIG, WorkflowModel>
{

	private static final String SCLASS_WORKFLOW_DETAILED_NAME = "yw-workflow-detailed-name";
	private static final String SCLASS_WORKFLOW_NAME = "yw-workflow-name";
	private static final String SCLASS_WORKFLOW_CURRENT_TASK = "yw-workflow-currenttask";
	private static final String SCLASS_TEXT_BUTTON = "ye-text-button";
	private static final String SCLASS_WORKFLOWS_ACTIONS_MENU_POPUP = "yw-workflows-menu-popup yw-pointer-menupopup yw-pointer-menupopup-top";
	private static final String LABEL_WORKFLOWS_MULTIPLE_PARALLEL_TASKS = "workflows.parallel";
	private static final String LABEL_WORKFLOW_NAME_CURRENTTASK = "workflow.name.currenttask";
	private static final String LABEL_WORKFLOW_NAME_CURRENTTASK_NOTASK = "workflow.name.currenttask.notask";
	private static final String NO_ICON = " ";

	@WireVariable
	private WorkflowActionService workflowActionService;

	@WireVariable
	private LabelService labelService;

	@Override
	public void render(final PARENT parent, final CONFIG configuration, final WorkflowModel workflowModel, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		UITools.addSClass(parent, SCLASS_WORKFLOW_DETAILED_NAME);

		final Label nameLabel = new Label(getLabelService().getObjectLabel(workflowModel));
		nameLabel.setAttribute(AbstractMoldStrategy.ATTRIBUTE_HYPERLINK_CANDIDATE, Boolean.TRUE);
		UITools.addSClass(nameLabel, SCLASS_WORKFLOW_NAME);
		parent.appendChild(nameLabel);
		fireComponentRendered(nameLabel, parent, configuration, workflowModel);

		final List<WorkflowActionModel> actions = getActiveWorkflowActionsFromWorkflow(workflowModel);

		if (actions.size() > 1)
		{
			final Menupopup actionsMenuPopup = createParallelTasksMenuPopup(actions);
			final Button parallelTasksButton = createParallelTasksButton(actionsMenuPopup);
			parent.appendChild(parallelTasksButton);
			parent.appendChild(actionsMenuPopup);
		}
		else
		{
			final WorkflowActionModel currentTask = actions.isEmpty() ? null : actions.get(0);
			final Label descriptionLabel = new Label(getTaskLabel(widgetInstanceManager, currentTask));
			UITools.addSClass(descriptionLabel, SCLASS_WORKFLOW_CURRENT_TASK);
			parent.appendChild(descriptionLabel);
			fireComponentRendered(descriptionLabel, parent, configuration, workflowModel);

		}
		fireComponentRendered(parent, configuration, workflowModel);
	}

	protected List<WorkflowActionModel> getActiveWorkflowActionsFromWorkflow(final WorkflowModel workflowModel)
	{
		if (workflowModel != null && workflowModel.getActions() != null)
		{
			return workflowModel.getActions().stream().filter(getWorkflowActionService()::isActive).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	protected Button createParallelTasksButton(final Menupopup actionsMenuPopup)
	{
		final Button actionButton = new Button();
		UITools.addSClass(actionButton, SCLASS_WORKFLOW_CURRENT_TASK);
		UITools.addSClass(actionButton, SCLASS_TEXT_BUTTON);
		actionButton.addEventListener(Events.ON_CLICK, e -> actionsMenuPopup.open(e.getTarget(), "after_start"));
		actionButton.setLabel(Labels.getLabel(LABEL_WORKFLOWS_MULTIPLE_PARALLEL_TASKS));

		return actionButton;
	}

	protected Menupopup createParallelTasksMenuPopup(final List<WorkflowActionModel> actions)
	{
		final Menupopup menupopup = new Menupopup();
		actions.forEach(action -> {
			final Menuitem menuitem = new Menuitem(getLabelService().getShortObjectLabel(action));
			menuitem.setIconSclass(NO_ICON);
			menuitem.setDisabled(true);
			menupopup.appendChild(menuitem);
		});

		menupopup.setSclass(SCLASS_WORKFLOWS_ACTIONS_MENU_POPUP);

		return menupopup;
	}

	protected String getTaskLabel(final WidgetInstanceManager wim, final WorkflowActionModel task)
	{
		final String taskName = task != null ? getLabelService().getShortObjectLabel(task) : getNoTaskLabel(wim);
		return CockpitComponentsUtils.getLabel(wim, LABEL_WORKFLOW_NAME_CURRENTTASK, taskName);
	}

	protected String getNoTaskLabel(final WidgetInstanceManager wim)
	{
		return CockpitComponentsUtils.getLabel(wim, LABEL_WORKFLOW_NAME_CURRENTTASK_NOTASK);
	}

	protected WorkflowActionService getWorkflowActionService()
	{
		return workflowActionService;
	}

	@Required
	public void setWorkflowActionService(final WorkflowActionService workflowActionService)
	{
		this.workflowActionService = workflowActionService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
