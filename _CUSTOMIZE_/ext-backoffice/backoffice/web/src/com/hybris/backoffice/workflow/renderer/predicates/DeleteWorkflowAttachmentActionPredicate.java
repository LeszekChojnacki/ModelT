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
package com.hybris.backoffice.workflow.renderer.predicates;

import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;

import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.core.util.Validate;


public class DeleteWorkflowAttachmentActionPredicate implements Predicate<WorkflowItemAttachmentModel>
{
	private WorkflowFacade workflowFacade;

	@Override
	public boolean test(final WorkflowItemAttachmentModel workflowItemAttachmentModelModel)
	{
		Validate.notNull("Workflow item attachment model must not be null", workflowItemAttachmentModelModel);
		return WorkflowStatus.PLANNED.equals(workflowFacade.getWorkflowStatus(workflowItemAttachmentModelModel.getWorkflow()));
	}

	@Required
	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}
}
