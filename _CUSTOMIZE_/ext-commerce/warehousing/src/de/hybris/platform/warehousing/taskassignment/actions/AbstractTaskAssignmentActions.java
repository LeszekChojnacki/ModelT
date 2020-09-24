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

package de.hybris.platform.warehousing.taskassignment.actions;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;
import de.hybris.platform.workflow.jobs.AutomatedWorkflowTemplateJob;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract class that have the common functionality of TaskConsignment actions
 */

public abstract class AbstractTaskAssignmentActions implements AutomatedWorkflowTemplateJob
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskAssignmentActions.class);

	private WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;
	private ModelService modelService;
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;

	/**
	 * Fetches the consignment {@link ConsignmentModel} associated to the received {@link WorkflowActionModel}
	 *
	 * @param action
	 * 		the {@link WorkflowActionModel} that contains the {@link ConsignmentModel}
	 * @return the attached {@link ConsignmentModel}
	 */
	protected Optional<ItemModel> getAttachedConsignment(final WorkflowActionModel action)
	{
		return action.getAttachmentItems().stream().filter(item -> item instanceof ConsignmentModel).findFirst();
	}

	/**
	 * Fetches the {@link AdvancedShippingNoticeModel} associated to the received {@link WorkflowActionModel}
	 *
	 * @param action
	 * 		the {@link WorkflowActionModel} that contains the {@link AdvancedShippingNoticeModel}
	 * @return the attached {@link AdvancedShippingNoticeModel}
	 */
	protected Optional<ItemModel> getAttachedAsn(final WorkflowActionModel action)
	{
		return action.getAttachmentItems().stream().filter(item -> item instanceof AdvancedShippingNoticeModel).findFirst();
	}

	/**
	 * Aligns {@link WorkflowActionModel#PRINCIPALASSIGNED} between action and automated action.
	 *
	 * @param automatedWorkflowAction
	 * 		the automated {@link WorkflowActionModel}
	 * @param workflowAction
	 * 		the {@link WorkflowActionModel}
	 */
	protected void assignNewPrincipalToAction(final WorkflowActionModel automatedWorkflowAction,
			final WorkflowActionModel workflowAction)
	{
		if (!automatedWorkflowAction.getPrincipalAssigned().equals(workflowAction.getPrincipalAssigned()))
		{
			automatedWorkflowAction.setPrincipalAssigned(workflowAction.getPrincipalAssigned());
			getModelService().save(automatedWorkflowAction);
		}
	}

	/**
	 * Gets the {@link WorkflowActionModel} and assigns the proper principal to it.
	 *
	 * @param templateCode
	 * 		the {@link de.hybris.platform.workflow.model.WorkflowActionTemplateModel#CODE}
	 * @param currentWorkflowAction
	 * 		the current {@link WorkflowActionModel}
	 * @param attachedConsignment
	 * 		the attached {@link ConsignmentModel}
	 * @param actionLabel
	 * 		the string to be displayed by the logger to describe the action.
	 */
	protected void getWorkflowActionAndAssignPrincipal(final String templateCode, final WorkflowActionModel currentWorkflowAction,
			final ConsignmentModel attachedConsignment, final String actionLabel)
	{
		final WorkflowActionModel workflowAction = getWarehousingConsignmentWorkflowService()
				.getWorkflowActionForTemplateCode(templateCode, attachedConsignment);

		assignNewPrincipalToAction(currentWorkflowAction, workflowAction);

		LOGGER.info("Consignment: {} has been {} by: {}", attachedConsignment.getCode(), actionLabel,
				workflowAction.getPrincipalAssigned().getDisplayName());
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected WarehousingConsignmentWorkflowService getWarehousingConsignmentWorkflowService()
	{
		return warehousingConsignmentWorkflowService;
	}

	@Required
	public void setWarehousingConsignmentWorkflowService(
			final WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService)
	{
		this.warehousingConsignmentWorkflowService = warehousingConsignmentWorkflowService;
	}
}
