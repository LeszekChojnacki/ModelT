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
package com.hybris.backoffice.widgets.actions.workflow;

import de.hybris.platform.workflow.model.WorkflowModel;

import javax.annotation.Resource;

import com.hybris.backoffice.workflow.renderer.actionexecutors.WorkflowTerminateActionExecutor;
import com.hybris.backoffice.workflow.renderer.predicates.TerminateWorkflowActionPredicate;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.labels.LabelService;


public class TerminateWorkflowAction implements CockpitAction<WorkflowModel, WorkflowModel>
{

	@Resource
	private WorkflowTerminateActionExecutor workflowTerminateActionExecutor;
	@Resource
	private TerminateWorkflowActionPredicate terminateWorkflowActionPredicate;
	@Resource
	private LabelService labelService;

	protected static final String LABEL_WORKFLOW_ACTION_CONFIRMATION_TERMINATE = "workflow.action.confirmation.message.terminate";

	@Override
	public boolean canPerform(final ActionContext<WorkflowModel> ctx)
	{
		final WorkflowModel workflowModel = ctx.getData();
		if (workflowModel != null)
		{
			return terminateWorkflowActionPredicate.test(workflowModel);
		}
		return false;
	}

	@Override
	public ActionResult<WorkflowModel> perform(final ActionContext<WorkflowModel> context)
	{
		final WorkflowModel workflowModel = context.getData();
		if (workflowModel != null)
		{
			final boolean terminated = workflowTerminateActionExecutor.apply(workflowModel);
			if (terminated)
			{
				return new ActionResult<>(ActionResult.SUCCESS);
			}
		}
		return new ActionResult<>(ActionResult.ERROR);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<WorkflowModel> context)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<WorkflowModel> ctx)
	{
		return ctx.getLabel(LABEL_WORKFLOW_ACTION_CONFIRMATION_TERMINATE, new String[]
		{ labelService.getObjectLabel(ctx.getData()) });
	}

}
