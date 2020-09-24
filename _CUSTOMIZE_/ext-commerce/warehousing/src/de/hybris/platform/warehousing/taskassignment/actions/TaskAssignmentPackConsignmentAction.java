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
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Synchronizes the pack consignment action from the Task Assignment Workflow of an employee to the main process engine.
 */
public class TaskAssignmentPackConsignmentAction extends AbstractTaskAssignmentActions
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssignmentPackConsignmentAction.class);

	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String PACK_CONSIGNMENT_CHOICE = "packConsignment";
	protected static final String SHIPPING_DECISION = "Shipping";
	protected static final String PACKING_TEMPLATE_CODE = "NPR_Packing";

	@Override
	public WorkflowDecisionModel perform(final WorkflowActionModel workflowAction)
	{
		final Optional<ItemModel> attachedConsignmentOptional = getAttachedConsignment(workflowAction);
		WorkflowDecisionModel result = null;

		if (attachedConsignmentOptional.isPresent())
		{
			final ConsignmentModel attachedConsignment = (ConsignmentModel) attachedConsignmentOptional.get();
			getWorkflowActionAndAssignPrincipal(workflowAction, attachedConsignment, "packed");
			getConsignmentBusinessProcessService()
					.triggerChoiceEvent(attachedConsignment, CONSIGNMENT_ACTION_EVENT_NAME, PACK_CONSIGNMENT_CHOICE);

			final boolean shippingConsignment = attachedConsignment.getDeliveryPointOfService() == null;

			// determine which decision to return whether the consignment is a pick up or shipping.
			Optional<WorkflowDecisionModel> decisionModel;
			if (shippingConsignment)
			{
				decisionModel = workflowAction.getDecisions().stream()
						.filter(decision -> SHIPPING_DECISION.equals(decision.getToActions().iterator().next().getName())).findFirst();
			}
			else
			{
				decisionModel = workflowAction.getDecisions().stream()
						.filter(decision -> !SHIPPING_DECISION.equals(decision.getToActions().iterator().next().getName())).findFirst();
			}

			result = decisionModel.isPresent() ? decisionModel.get() : null;
		}
		return result;
	}

	protected void getWorkflowActionAndAssignPrincipal(WorkflowActionModel workflowAction, ConsignmentModel attachedConsignment, String actionLabel)
	{
		final WorkflowActionModel packingWorkflowAction = getWarehousingConsignmentWorkflowService()
				.getWorkflowActionForTemplateCode(PACKING_TEMPLATE_CODE, attachedConsignment);

		assignNewPrincipalToAction(workflowAction, packingWorkflowAction);

		LOGGER.info("Consignment: {} has been {} by: {}", attachedConsignment.getCode(), actionLabel,
				packingWorkflowAction.getPrincipalAssigned().getDisplayName());
	}

}
