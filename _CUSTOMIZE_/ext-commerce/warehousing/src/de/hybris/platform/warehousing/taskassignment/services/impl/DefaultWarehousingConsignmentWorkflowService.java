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
package de.hybris.platform.warehousing.taskassignment.services.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;
import de.hybris.platform.warehousing.taskassignment.strategy.UserSelectionStrategy;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.WorkflowTemplateService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService}
 */
public class DefaultWarehousingConsignmentWorkflowService implements WarehousingConsignmentWorkflowService
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultWarehousingConsignmentWorkflowService.class);

	protected static final String CONSIGNMENT_TEMPLATE_NAME = "warehousing.consignment.workflow.template";
	protected static final String WORKFLOW_OF_CONSIGNMENT = "consignmentworkflow_";
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";

	private ModelService modelService;
	private UserSelectionStrategy userSelectionStrategy;
	private WorkflowService workflowService;
	private WorkflowTemplateService workflowTemplateService;
	private WorkflowProcessingService workflowProcessingService;
	private UserService userService;
	private ConfigurationService configurationService;
	private WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;

	@Override
	public void startConsignmentWorkflow(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);
		final String consignmentWorkflowName = getConfigurationService().getConfiguration().getString(CONSIGNMENT_TEMPLATE_NAME);

		try
		{
			final WorkflowTemplateModel workflowTemplate = getWorkflowTemplateService()
					.getWorkflowTemplateForCode(consignmentWorkflowName);
			if (workflowTemplate != null)
			{
				final WorkflowModel workflow = getWorkflowService()
						.createWorkflow(WORKFLOW_OF_CONSIGNMENT + consignment.getCode(), workflowTemplate,
								Collections.singletonList(consignment), getUserService().getAdminUser());
				getModelService().save(workflow);
				consignment.setTaskAssignmentWorkflow(workflow.getCode());
				getModelService().save(consignment);
				workflow.getActions().forEach(action -> getModelService().save(action));
				getWorkflowProcessingService().startWorkflow(workflow);
				final UserModel finalSelectedUser = getUserSelectionStrategy().getUserForConsignmentAssignment(workflow);
				workflow.setOwner(finalSelectedUser);
				workflow.getActions().forEach(action -> {
					action.setPrincipalAssigned(finalSelectedUser);
					getModelService().save(action);
				});
				getModelService().save(workflow);
				LOGGER.info("Employee: {} is assigned to consignment: {}.", finalSelectedUser.getDisplayName(),
						consignment.getCode());
			}
			else
			{
				LOGGER.debug(
						"No workflow template found, task assignment workflow will not be created but order will still be sourced.");
			}
		}
		catch (final UnknownIdentifierException | IllegalArgumentException e)  //NOSONAR
		{
			LOGGER.debug("No WorkflowTemplate found for code: {} There will be no workflow assigned for this consignment",
					consignmentWorkflowName);
		}
		catch (final ModelSavingException e)  //NOSONAR
		{
			LOGGER.debug("No PrincipalAssigned available for this order. A consignment will still be created.");
		}
	}

	@Override
	public void terminateConsignmentWorkflow(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);
		if (consignment.getTaskAssignmentWorkflow() != null)
		{
			try
			{
				// synchronize with the Task Assignment Workflow
				terminateWorkflow(getWorkflowService().getWorkflowForCode(consignment.getTaskAssignmentWorkflow()));
			}
			catch (final UnknownIdentifierException e)  //NOSONAR
			{
				LOGGER.debug(
						"No synchronization with a task assignment workflow because consignment {} has no workflow assigned to it.",
						consignment.getCode());
			}
		}
	}

	@Override
	public void decideWorkflowAction(final ConsignmentModel consignment, final String templateCode, final String choice)
	{
		final WorkflowActionModel taskAction = getWorkflowActionForTemplateCode(templateCode, consignment);

		if (taskAction != null)
		{
			taskAction.setPrincipalAssigned(getUserService().getCurrentUser());
			getModelService().save(taskAction);
			getWorkflowProcessingService().decideAction(taskAction, taskAction.getDecisions().iterator().next());
		}
		else if (!StringUtils.isEmpty(choice))
		{
			getConsignmentBusinessProcessService().triggerChoiceEvent(consignment, CONSIGNMENT_ACTION_EVENT_NAME, choice);
		}
	}

	@Override
	public WorkflowActionModel getWorkflowActionForTemplateCode(final String templateCode, final ConsignmentModel consignment)
	{
		WorkflowActionModel result = null;

		if (consignment.getTaskAssignmentWorkflow() != null)
		{
			final WorkflowModel consignmentWorkflow = getWorkflowService()
					.getWorkflowForCode(consignment.getTaskAssignmentWorkflow());

			result = consignmentWorkflow.getActions().stream().filter(action -> action.getTemplate().getCode().equals(templateCode))
					.findFirst().get();
		}

		return result;
	}

	/**
	 * Terminates the given {@link WorkflowModel} as well as all of its related {@link WorkflowActionModel}
	 *
	 * @param taskAssignmentWorkflow
	 * 		the workflow to be terminated
	 */
	protected void terminateWorkflow(final WorkflowModel taskAssignmentWorkflow)
	{
		if (taskAssignmentWorkflow != null)
		{
			getWorkflowProcessingService().terminateWorkflow(taskAssignmentWorkflow);

			// terminate actions in case they are not terminated yet
			taskAssignmentWorkflow.getActions().stream()
					.filter(action -> !WorkflowActionStatus.TERMINATED.equals(action.getStatus())).forEach(action -> {
				action.setStatus(WorkflowActionStatus.TERMINATED);
				getModelService().save(action);
			});
		}
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

	protected WorkflowTemplateService getWorkflowTemplateService()
	{
		return workflowTemplateService;
	}

	@Required
	public void setWorkflowTemplateService(final WorkflowTemplateService workflowTemplateService)
	{
		this.workflowTemplateService = workflowTemplateService;
	}

	protected WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	@Required
	public void setWorkflowProcessingService(final WorkflowProcessingService workflowProcessingService)
	{
		this.workflowProcessingService = workflowProcessingService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected UserSelectionStrategy getUserSelectionStrategy()
	{
		return userSelectionStrategy;
	}

	@Required
	public void setUserSelectionStrategy(final UserSelectionStrategy userSelectionStrategy)
	{
		this.userSelectionStrategy = userSelectionStrategy;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected WarehousingBusinessProcessService<ConsignmentModel> getConsignmentBusinessProcessService()
	{
		return consignmentBusinessProcessService;
	}

	@Required
	public void setConsignmentBusinessProcessService(
			final WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService)
	{
		this.consignmentBusinessProcessService = consignmentBusinessProcessService;
	}
}

