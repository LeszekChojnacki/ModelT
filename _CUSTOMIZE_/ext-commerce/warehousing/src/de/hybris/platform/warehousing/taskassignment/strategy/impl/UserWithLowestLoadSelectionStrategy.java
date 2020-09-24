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
package de.hybris.platform.warehousing.taskassignment.strategy.impl;

import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.taskassignment.strategy.UserSelectionStrategy;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link UserSelectionStrategy}
 * UserWithLowestLoadSelectionStrategy selects a user {@link UserModel} which have lowest number of tasks assigned to it
 */
public class UserWithLowestLoadSelectionStrategy implements UserSelectionStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(UserWithLowestLoadSelectionStrategy.class);
	private WorkflowService workflowService;

	@Override
	public UserModel getUserForConsignmentAssignment(final WorkflowModel workflow)
	{
		validateParameterNotNullStandardMessage("workflow", workflow);
		UserModel result = null;
		final Optional<WorkflowActionModel> inProgressAction = workflow.getActions().stream()
				.filter(action -> action.getStatus().equals(WorkflowActionStatus.IN_PROGRESS)).findFirst();
		if (inProgressAction.isPresent())
		{
			final WorkflowActionModel workflowAction = inProgressAction.get();
			if (workflowAction.getPrincipalAssigned() instanceof PrincipalGroupModel)
			{
				result = userWithLowestLoad(workflow, workflowAction);
			}
		}
		else
		{
			LOG.debug("Couldn't find a user to assign for workflow: {}", workflow.getCode());
		}
		return result;
	}

	/**
	 * Finds the {@link UserModel} with the lowest load
	 *
	 * @param workflow
	 * 		the {@link WorkflowModel}
	 * @param workflowAction
	 * 		the {@link WorkflowActionModel}
	 * @return the warehouse agent with lowest load
	 */
	protected UserModel userWithLowestLoad(final WorkflowModel workflow, final WorkflowActionModel workflowAction)
	{
		final PrincipalGroupModel principalGroup = (PrincipalGroupModel) workflowAction.getPrincipalAssigned();
		Integer lowestLoad = null;
		UserModel result = null;

		for (final PrincipalModel principal : getPosEmployees(workflow, principalGroup))
		{
			if (principal instanceof UserModel)
			{
				final Integer assignedWorkflowActions = (int) getWorkflowService()
						.getWorkflowsForTemplateAndUser(workflow.getJob(), (UserModel) principal).stream().filter(
								workflowModel -> !CronJobStatus.ABORTED.equals(workflowModel.getStatus()) && !CronJobStatus.FINISHED
										.equals(workflowModel.getStatus())).count();

				if (lowestLoad == null || lowestLoad > assignedWorkflowActions)
				{
					lowestLoad = assignedWorkflowActions;
					result = (UserModel) principal;
				}
			}
		}
		return result;
	}

	/**
	 * Finds the employees linked to the warehouse which the workflow attached consignment is allocated to.
	 *
	 * @param workflow
	 * 		the given consignment workflow
	 * @param principalGroup
	 * 		the principle group which assigned to the workflow action
	 * @return all employees that belong to the warehouse which the consignment of the workflow is allocated
	 */
	protected Set<PrincipalModel> getPosEmployees(final WorkflowModel workflow, final PrincipalGroupModel principalGroup)
	{
		validateParameterNotNullStandardMessage("workflow", workflow);
		validateParameterNotNullStandardMessage("principalGroup", principalGroup);
		final Set<PrincipalModel> posEmployees = new HashSet<>();
		if (!workflow.getAttachments().isEmpty())
		{
			final WarehouseModel warehouse = ((ConsignmentModel) workflow.getAttachments().iterator().next().getItem())
					.getWarehouse();

			if (warehouse.getPointsOfService() != null)
			{
				warehouse.getPointsOfService()
						.forEach(pos -> pos.getStoreEmployeeGroups().forEach(seg -> posEmployees.addAll(seg.getMembers())));
				posEmployees.retainAll(principalGroup.getMembers());
			}
		}
		return posEmployees;
	}

	protected WorkflowService getWorkflowService()
	{
		return workflowService;
	}

	@Required
	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}
}
