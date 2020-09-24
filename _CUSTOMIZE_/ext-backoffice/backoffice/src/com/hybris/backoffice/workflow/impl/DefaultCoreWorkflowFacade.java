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
package com.hybris.backoffice.workflow.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.workflow.WorkflowAttachmentService;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.WorkflowTemplateService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.CoreWorkflowFacade;


/**
 * Default implementation of facade to handle core workflow functionality (independent of the web context)
 */
public class DefaultCoreWorkflowFacade implements CoreWorkflowFacade
{

	private WorkflowAttachmentService workflowAttachmentService;
	private WorkflowTemplateService workflowTemplateService;
	private WorkflowService workflowService;
	private WorkflowProcessingService workflowProcessingService;

	@Override
	public List<WorkflowItemAttachmentModel> addItems(final WorkflowModel workflow, final List<? extends ItemModel> itemsToAdd)
	{
		return getWorkflowAttachmentService().addItems(workflow, itemsToAdd);
	}

	@Override
	public WorkflowTemplateModel getWorkflowTemplateForCode(final String code)
	{
		return getWorkflowTemplateService().getWorkflowTemplateForCode(code);
	}

	@Override
	public WorkflowTemplateModel getAdHocWorkflowTemplate()
	{
		return getWorkflowTemplateService().getAdhocWorkflowTemplate();
	}

	@Override
	public WorkflowModel createWorkflow(final String name, final WorkflowTemplateModel template, final List<ItemModel> itemsToAdd,
			final UserModel owner)
	{
		return getWorkflowService().createWorkflow(name, template, itemsToAdd, owner);
	}

	@Override
	public boolean startWorkflow(final WorkflowModel workflow)
	{
		return getWorkflowProcessingService().startWorkflow(workflow);
	}

	@Override
	public boolean canBeStarted(final WorkflowModel workflow)
	{
		return workflowService.isPlanned(workflow) && workflowService.canBeStarted(workflow) && CollectionUtils
				.isNotEmpty(workflow.getAttachments().stream().filter(e -> e.getItem() != null).collect(Collectors.toList()));
	}

	@Override
	public boolean isAdHocTemplate(final WorkflowTemplateModel template)
	{
		return Objects.equals(template, getWorkflowTemplateService().getAdhocWorkflowTemplate());
	}

	@Override
	public boolean isCorrectAdHocAssignee(final PrincipalModel adHocAssignedUser)
	{
		return adHocAssignedUser != null
				&& !Objects.equals(adHocAssignedUser, getWorkflowTemplateService().getAdhocWorkflowTemplateDummyOwner());
	}

	@Override
	public WorkflowStatus getWorkflowStatus(final WorkflowModel workflowModel)
	{
		if (workflowModel.getActions().isEmpty())
		{
			return null;
		}
		if (getWorkflowService().isFinished(workflowModel))
		{
			return WorkflowStatus.FINISHED;
		}
		if (getWorkflowService().isTerminated(workflowModel))
		{
			return WorkflowStatus.TERMINATED;
		}
		if (getWorkflowService().isRunning(workflowModel) || getWorkflowService().isPaused(workflowModel))
		{
			return WorkflowStatus.RUNNING;
		}
		if (getWorkflowService().isPlanned(workflowModel))
		{
			return WorkflowStatus.PLANNED;
		}
		return null;
	}

	@Override
	public boolean terminateWorkflow(final WorkflowModel workflow)
	{
		return workflowProcessingService.terminateWorkflow(workflow);
	}

	@Override
	public List<WorkflowActionModel> getCurrentTasks(final WorkflowModel workflowModel)
	{
		return workflowModel.getActions().stream().filter(a -> WorkflowActionStatus.IN_PROGRESS.equals(a.getStatus()))
				.collect(Collectors.toList());
	}

	@Override
	public int countDecisions(final WorkflowModel workflowModel)
	{
		return (int) workflowModel.getActions().stream().filter(a -> a.getDecisions() != null)
				.mapToLong(a -> a.getDecisions().size()).sum();
	}

	@Override
	public Date getWorkflowStartTime(final WorkflowModel workflow)
	{
		return workflowService.getStartTime(workflow);
	}

	public WorkflowAttachmentService getWorkflowAttachmentService()
	{
		return workflowAttachmentService;
	}

	@Required
	public void setWorkflowAttachmentService(final WorkflowAttachmentService workflowAttachmentService)
	{
		this.workflowAttachmentService = workflowAttachmentService;
	}

	public WorkflowTemplateService getWorkflowTemplateService()
	{
		return workflowTemplateService;
	}

	@Required
	public void setWorkflowTemplateService(final WorkflowTemplateService workflowTemplateService)
	{
		this.workflowTemplateService = workflowTemplateService;
	}

	public WorkflowService getWorkflowService()
	{
		return workflowService;
	}

	@Required
	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}

	public WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	@Required
	public void setWorkflowProcessingService(final WorkflowProcessingService workflowProcessingService)
	{
		this.workflowProcessingService = workflowProcessingService;
	}
}
