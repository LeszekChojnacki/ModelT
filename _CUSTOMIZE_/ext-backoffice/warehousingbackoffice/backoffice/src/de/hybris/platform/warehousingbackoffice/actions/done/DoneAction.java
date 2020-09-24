/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.actions.done;

import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.impl.DefaultWorkflowService;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.hybris.backoffice.navigation.impl.SimpleNode;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import org.apache.commons.collections.CollectionUtils;
import org.zkoss.zul.Messagebox;


/**
 * Action to mark the selected {@link WorkflowActionModel} as done and move to the next step of the Task Assignment Workflow.
 */
public class DoneAction extends AbstractComponentWidgetAdapterAware
		implements CockpitAction<Collection<WorkflowActionModel>, Collection<WorkflowActionModel>>
{
	protected static final String SOCKET_OUT_CONTEXT = "nodeSelected";
	protected static final String SOCKET_OUT_DESELECT = "deselectItems";
	protected static final String NODE_SELECTED = "warehousing.treenode.taskassignment.inbox";
	protected static final String CANCELLED_TASKS_MESSAGE = "warehousingbackoffice.taskassignment.done.cancelledtasks.message";
	protected static final String CANCELLED_TASKS_TITLE = "warehousingbackoffice.taskassignment.done.cancelledtasks.title";

	@Resource
	private DefaultWorkflowService newestWorkflowService;
	@Resource
	private WorkflowProcessingService workflowProcessingService;

	@Override
	public boolean canPerform(final ActionContext<Collection<WorkflowActionModel>> actionContext)
	{
		boolean isFulfillmentInternal = false;

		try
		{
			if (actionContext != null && CollectionUtils.isNotEmpty(actionContext.getData()))
			{
				final Collection<WorkflowActionModel> workflowActions = actionContext.getData();
				isFulfillmentInternal = workflowActions.stream().allMatch(workflowActionModel ->
						((ConsignmentModel) workflowActionModel.getAttachmentItems().iterator().next()).getFulfillmentSystemConfig()
								== null);
			}
		}
		catch (final ClassCastException exception) //NOSONAR
		{
			// Do nothing and will return false
		}

		return isFulfillmentInternal;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<Collection<WorkflowActionModel>> actionContext)
	{
		return null;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<Collection<WorkflowActionModel>> actionContext)
	{
		return false;
	}

	@Override
	public ActionResult<Collection<WorkflowActionModel>> perform(
			final ActionContext<Collection<WorkflowActionModel>> actionContext)
	{
		final Collection<WorkflowActionModel> tasks = new ArrayList<>();
		tasks.addAll(actionContext.getData());
		final Collection<WorkflowActionModel> cancelledTasks = tasks.stream().filter(
				task -> getNewestWorkflowService().getWorkflowForCode(task.getWorkflow().getCode()).getStatus()
						.equals(CronJobStatus.ABORTED)).collect(Collectors.toList());
		if (!cancelledTasks.isEmpty())
		{
			displayCancellationMessageBox(cancelledTasks, actionContext);
		}

		sendOutput(SOCKET_OUT_DESELECT, tasks);
		actionContext.getData().clear();

		tasks.removeAll(cancelledTasks);
		tasks.forEach(task -> getWorkflowProcessingService().decideAction(task, task.getDecisions().iterator().next()));

		final SimpleNode inboxSimpleNode = new SimpleNode(NODE_SELECTED);
		sendOutput(SOCKET_OUT_CONTEXT, inboxSimpleNode);

		final ActionResult<Collection<WorkflowActionModel>> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		return actionResult;
	}

	/**
	 * Displays a message box to notify the user that the given {@link Collection<WorkflowActionModel>} are cancelled.
	 *
	 * @param cancelledTasks
	 * 		the cancelled {@link Collection<WorkflowActionModel>}
	 * @param actionContext
	 * 		the {@link ActionContext} which will be used to get labels to be displayed to the user
	 */
	protected void displayCancellationMessageBox(final Collection<WorkflowActionModel> cancelledTasks,
			final ActionContext<Collection<WorkflowActionModel>> actionContext)
	{
		final StringBuilder cancellationMessage = new StringBuilder(actionContext.getLabel(CANCELLED_TASKS_MESSAGE));
		cancelledTasks.forEach(
				task -> cancellationMessage.append(" ").append(((ConsignmentModel) task.getAttachmentItems().get(0)).getCode())
						.append(","));
		cancellationMessage.setCharAt(cancellationMessage.length() - 1, '.');

		Messagebox.show(cancellationMessage.toString(), actionContext.getLabel(CANCELLED_TASKS_TITLE), Messagebox.OK,
				Messagebox.EXCLAMATION, null);
	}

	protected WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	protected DefaultWorkflowService getNewestWorkflowService()
	{
		return newestWorkflowService;
	}

}
