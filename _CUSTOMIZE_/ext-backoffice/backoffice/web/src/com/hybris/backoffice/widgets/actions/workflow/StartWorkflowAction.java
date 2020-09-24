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

import com.hybris.backoffice.workflow.renderer.actionexecutors.WorkflowStartActionExecutor;
import com.hybris.backoffice.workflow.renderer.predicates.StartWorkflowActionPredicate;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;


public class StartWorkflowAction implements CockpitAction<WorkflowModel, WorkflowModel>
{

	@Resource
	private WorkflowStartActionExecutor workflowStartActionExecutor;
	@Resource
	private StartWorkflowActionPredicate startWorkflowActionPredicate;

	@Override
	public boolean canPerform(final ActionContext<WorkflowModel> ctx)
	{
		final WorkflowModel workflowModel = ctx.getData();
		if (workflowModel != null)
		{
			return startWorkflowActionPredicate.test(workflowModel);
		}
		return false;
	}

	@Override
	public ActionResult<WorkflowModel> perform(final ActionContext<WorkflowModel> context)
	{
		final WorkflowModel workflowModel = context.getData();
		if (workflowModel != null)
		{
			final boolean started = workflowStartActionExecutor.apply(workflowModel);
			if (started)
			{
				return new ActionResult<>(ActionResult.SUCCESS);
			}
		}
		return new ActionResult<>(ActionResult.ERROR);
	}

}
