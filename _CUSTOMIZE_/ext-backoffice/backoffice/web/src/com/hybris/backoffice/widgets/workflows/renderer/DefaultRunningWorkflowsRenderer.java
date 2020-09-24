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

import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.backoffice.renderer.utils.UIDateRendererProvider;
import com.hybris.backoffice.widgets.workflows.WorkflowsController;
import com.hybris.cockpitng.common.renderer.AbstractCustomMenuActionRenderer;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;


/**
 * Renders workflows which are already started=running.
 */
public class DefaultRunningWorkflowsRenderer extends AbstractWorkflowsListRenderer<Listitem, Object, WorkflowModel>
{
	protected static final String SCLASS_WORKFLOWS_LIST_INFO = "yw-workflows-list-info";
	protected static final String SCLASS_WORKFLOWS_LIST_INFO_CONTAINER = "yw-workflows-list-info-container";
	protected static final String SCLASS_WORKFLOWS_LIST_INFO_PARALLEL = "yw-workflows-list-parallel";
	protected static final String SCLASS_WORKFLOWS_LIST_MIDDLE = "yw-workflows-list-middle";
	protected static final String SCLASS_WORKFLOWS_LIST_SUBTITLE = "yw-workflows-list-subtitle ye-text-button";
	protected static final String SCLASS_WORKFLOWS_ACTIONS_MENU_POPUP = "yw-workflows-menu-popup yw-pointer-menupopup yw-pointer-menupopup-top";
	protected static final String NO_ICON = " ";

	protected static final String LABEL_WORKFLOWS_MULTIPLE_PARALLEL_TASKS = "workflows.parallel";
	protected static final String ACTIONS_POPUP_POSITION = "after_start";

	private TimeService timeService;
	private AbstractCustomMenuActionRenderer<Div, Object, WorkflowModel> threeDotsRenderer;
	private LabelService labelService;
	private UIDateRendererProvider uiDateRendererProvider;

	@Override
	protected Div createContent(final WorkflowModel workflowModel, final DataType dataType, final WidgetInstanceManager wim)
	{
		final Div content = new Div();
		final Div middleContent = createMiddleContent(wim, workflowModel);
		middleContent.setSclass(SCLASS_WORKFLOWS_LIST_MIDDLE);
		content.appendChild(middleContent);
		final Div bottomContent = createBottomContent(wim, workflowModel);
		bottomContent.setSclass(SCLASS_WORKFLOWS_LIST_INFO_CONTAINER);
		content.appendChild(bottomContent);
		return content;
	}

	protected Div createMiddleContent(final WidgetInstanceManager wim, final WorkflowModel workflowModel)
	{
		final Div middleContent = new Div();
		final List<WorkflowActionModel> actions = workflowModel.getActions() //
				.stream() //
				.filter(e -> ObjectUtils.equals(WorkflowActionStatus.IN_PROGRESS, e.getStatus())) //
				.collect(Collectors.toList());

		if (actions.size() == 1)
		{
			middleContent.appendChild(createSingleActionButton(wim, actions.get(0)));
		}
		else if (actions.size() > 1)
		{
			final Menupopup menupopup = createParallelActionsPopup(wim, actions);
			middleContent.appendChild(menupopup);
			middleContent.appendChild(createMultipleActionsButton(wim, menupopup));
		}
		return middleContent;
	}

	private Button createSingleActionButton(final WidgetInstanceManager wim, final WorkflowActionModel workflowActionModel)
	{
		final Button actionButton = new Button();
		actionButton.setLabel(workflowActionModel.getName());
		actionButton.addEventListener(Events.ON_CLICK,
				e -> wim.sendOutput(WorkflowsController.SOCKET_OUT_WORKFLOW_ACTION_SELECTED, workflowActionModel));
		UITools.addSClass(actionButton, SCLASS_WORKFLOWS_LIST_SUBTITLE);
		return actionButton;
	}

	private Button createMultipleActionsButton(final WidgetInstanceManager wim, final Menupopup menupopup)
	{
		final Button actionButton = new Button();
		actionButton.setLabel(Labels.getLabel(LABEL_WORKFLOWS_MULTIPLE_PARALLEL_TASKS));
		actionButton.addEventListener(Events.ON_CLICK,
				e -> menupopup.open(e.getTarget(), ACTIONS_POPUP_POSITION));
		UITools.addSClass(actionButton, SCLASS_WORKFLOWS_LIST_INFO_PARALLEL);
		UITools.addSClass(actionButton, SCLASS_WORKFLOWS_LIST_SUBTITLE);
		return actionButton;
	}

	protected Menupopup createParallelActionsPopup(final WidgetInstanceManager wim, final List<WorkflowActionModel> actions)
	{
		final Menupopup menupopup = new Menupopup();
		for (final WorkflowActionModel action : actions)
		{
			final Menuitem menuitem = new Menuitem(action.getName());
			menuitem.setIconSclass(NO_ICON);
			menuitem.addEventListener(Events.ON_CLICK,
					e -> wim.sendOutput(WorkflowsController.SOCKET_OUT_WORKFLOW_ACTION_SELECTED, action));
			menupopup.appendChild(menuitem);
		}

		menupopup.setSclass(SCLASS_WORKFLOWS_ACTIONS_MENU_POPUP);
		return menupopup;
	}

	protected Div createBottomContent(final WidgetInstanceManager wim, final WorkflowModel workflowModel)
	{
		final Div bottomContent = new Div();
		final Label dateLabel = createDateLabel(workflowModel);
		dateLabel.setSclass(SCLASS_WORKFLOWS_LIST_INFO);
		final Label noOfAttachmentsLabel = createNoOfAttachmentsLabel(wim, workflowModel);
		noOfAttachmentsLabel.setSclass(SCLASS_WORKFLOWS_LIST_INFO);
		bottomContent.appendChild(dateLabel);
		bottomContent.appendChild(noOfAttachmentsLabel);
		return bottomContent;
	}

	protected Label createDateLabel(final WorkflowModel workflowModel)
	{
		final UIDateRendererProvider rendererProvider = getUiDateRendererProvider();
		final String dateLabelValue = rendererProvider.getFormattedDateLabel(getTimeService().getCurrentTime(),
				workflowModel.getModifiedtime());
		final Label dateLabel = new Label(dateLabelValue);

		return dateLabel;
	}

	protected Label createNoOfAttachmentsLabel(final WidgetInstanceManager wim, final WorkflowModel workflowModel)
	{
		final Label noOfAttachmentsLabel = new Label();
		updateNoOfAttachmentsLabel(wim, workflowModel, noOfAttachmentsLabel);

		return noOfAttachmentsLabel;
	}

	@Override
	protected String getWorkflowName(final WorkflowModel workflowModel)
	{
		return getLabelService().getObjectLabel(workflowModel);
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	@Override
	public AbstractCustomMenuActionRenderer<Div, Object, WorkflowModel> getThreeDotsRenderer()
	{
		return threeDotsRenderer;
	}

	@Required
	public void setThreeDotsRenderer(final AbstractCustomMenuActionRenderer<Div, Object, WorkflowModel> threeDotsRenderer)
	{
		this.threeDotsRenderer = threeDotsRenderer;
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

	public UIDateRendererProvider getUiDateRendererProvider()
	{
		return uiDateRendererProvider;
	}

	@Required
	public void setUiDateRendererProvider(final UIDateRendererProvider uiDateRendererProvider)
	{
		this.uiDateRendererProvider = uiDateRendererProvider;
	}
}
