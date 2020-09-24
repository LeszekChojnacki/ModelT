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

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.workflow.WorkflowConstants;
import com.hybris.cockpitng.common.renderer.AbstractCustomMenuActionRenderer;
import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dnd.DragAndDropStrategy;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;


/**
 * Renders workflows which are not started yet.
 */
public class DefaultPlannedWorkflowsRenderer extends AbstractWorkflowsListRenderer<Listitem, Object, WorkflowModel>
{
	public static final String SCLASS_WORKFLOWS_LIST_DROP = "yw-workflows-droparea";
	public static final String SCLASS_WORKFLOWS_LIST_PLUS_ICON = "yw-workflows-droparea-plus-icon";
	public static final String SCLASS_WORKFLOWS_PLAIN_TEXT_BUTTON = "ye-text-button";
	public static final String SCLASS_WORKFLOWS_LIST_BOTTOM = "yw-workflows-list-bottom";
	public static final String SCLASS_WORKFLOWS_LIST_BOTTOM_START = "yw-workflows-list-bottom-start";
	public static final String SCLASS_WORKFLOWS_LIST_BOTTOM_START_DISABLED = "yw-workflows-list-bottom-start-disabled";

	protected static final String LABEL_WORKFLOWS_START = "workflows.start";
	protected static final String LABEL_WORKFLOWS_ADD = "workflows.add";

	private static final Logger LOG = Logger.getLogger(DefaultPlannedWorkflowsRenderer.class);

	private DragAndDropStrategy dragAndDropStrategy;
	private Function<WorkflowModel, Boolean> workflowStartActionExecutor;
	private AbstractCustomMenuActionRenderer<Div, Object, WorkflowModel> threeDotsRenderer;
	private ObjectFacade objectFacade;
	private LabelService labelService;
	private NotificationService notificationService;

	@Override
	protected Div createContent(final WorkflowModel model, final DataType dataType, final WidgetInstanceManager wim)
	{
		final Div content = new Div();
		final Label noOfAttachmentsLabel = new Label();
		final Div bottomDiv = createBottomContent(wim, model, noOfAttachmentsLabel);
		final Div middleDiv = createMiddleContent(wim, model, noOfAttachmentsLabel);
		middleDiv.addEventListener(-1, Events.ON_DROP, event -> {
			updateNoOfAttachmentsLabel(wim, model, noOfAttachmentsLabel);
			updateStartButtonState(wim, model, (HtmlBasedComponent) bottomDiv.query("." + SCLASS_WORKFLOWS_LIST_BOTTOM_START));
		});
		content.appendChild(middleDiv);
		content.appendChild(bottomDiv);
		return content;
	}

	protected Div createMiddleContent(final WidgetInstanceManager wim, final WorkflowModel model, final Label noOfAttachmentsLabel)
	{
		final Div middleDiv = new Div();
		middleDiv.setSclass(SCLASS_WORKFLOWS_LIST_DROP);

		dragAndDropStrategy.makeDroppable(middleDiv, model, new DefaultCockpitContext());

		final Label plusLabel = new Label();
		final Label addAttachmentsLabel = new Label();
		plusLabel.setValue("+");
		plusLabel.setSclass(SCLASS_WORKFLOWS_LIST_PLUS_ICON);
		addAttachmentsLabel.setValue(Labels.getLabel(LABEL_WORKFLOWS_ADD));

		middleDiv.appendChild(plusLabel);
		middleDiv.appendChild(addAttachmentsLabel);

		return middleDiv;
	}

	protected Div createBottomContent(final WidgetInstanceManager wim, final WorkflowModel workflow,
			final Label noOfAttachmentsLabel)
	{
		final Div bottomDiv = new Div();
		bottomDiv.setSclass(SCLASS_WORKFLOWS_LIST_BOTTOM);

		final Button startWorkflowButton = new Button();
		startWorkflowButton.setSclass(SCLASS_WORKFLOWS_LIST_BOTTOM_START + " " + SCLASS_WORKFLOWS_PLAIN_TEXT_BUTTON);
		startWorkflowButton.setLabel(Labels.getLabel(LABEL_WORKFLOWS_START));

		updateStartButtonState(wim, workflow, startWorkflowButton);

		updateNoOfAttachmentsLabel(wim, workflow, noOfAttachmentsLabel);
		bottomDiv.appendChild(startWorkflowButton);
		bottomDiv.appendChild(noOfAttachmentsLabel);

		return bottomDiv;
	}

	protected void updateStartButtonState(final WidgetInstanceManager wim, final WorkflowModel workflow,
			final HtmlBasedComponent startWorkflowButton)
	{
		final boolean isButtonEnabled = CollectionUtils
				.isNotEmpty(workflow.getAttachments().stream().filter(e -> e.getItem() != null).collect(Collectors.toList()));
		UITools.modifySClass(startWorkflowButton, SCLASS_WORKFLOWS_LIST_BOTTOM_START_DISABLED, !isButtonEnabled);
		if (isButtonEnabled)
		{
			startWorkflowButton.addEventListener(Events.ON_CLICK, getStartButtonEventListener(workflow));
		}
		else
		{
			startWorkflowButton.removeEventListener(Events.ON_CLICK, getStartButtonEventListener(workflow));
		}
	}

	protected EventListener<Event> getStartButtonEventListener(final WorkflowModel workflow)
	{
		final WorkflowModel refreshedWorkflow;
		try
		{
			refreshedWorkflow = objectFacade.reload(workflow);
		}
		catch (final ObjectNotFoundException e)
		{
			LOG.error(e);
			return event -> {
				// NOT APPLICABLE
			};
		}

		return event -> {
			if (CollectionUtils.isNotEmpty(
					refreshedWorkflow.getAttachments().stream().filter(e -> e.getItem() != null).collect(Collectors.toList())))
			{
				getWorkflowStartActionExecutor().apply(refreshedWorkflow);
			}
			else
			{
				getNotificationService().notifyUser(WorkflowConstants.HANDLER_NOTIFICATION_SOURCE,
						WorkflowConstants.EVENT_TYPE_WORKFLOW_WITHOUT_ATTACHMENTS, NotificationEvent.Level.FAILURE);
			}
		};

	}

	@Override
	protected String getWorkflowName(final WorkflowModel workflowModel)
	{
		return getLabelService().getObjectLabel(workflowModel);
	}

	public DragAndDropStrategy getDragAndDropStrategy()
	{
		return dragAndDropStrategy;
	}

	@Required
	public void setDragAndDropStrategy(final DragAndDropStrategy dragAndDropStrategy)
	{
		this.dragAndDropStrategy = dragAndDropStrategy;
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

	public Function<WorkflowModel, Boolean> getWorkflowStartActionExecutor()
	{
		return workflowStartActionExecutor;
	}

	@Required
	public void setWorkflowStartActionExecutor(final Function<WorkflowModel, Boolean> workflowStartActionExecutor)
	{
		this.workflowStartActionExecutor = workflowStartActionExecutor;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
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

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
