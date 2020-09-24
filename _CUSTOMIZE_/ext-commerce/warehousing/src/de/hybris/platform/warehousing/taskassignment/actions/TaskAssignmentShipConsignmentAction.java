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

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Synchronizes the ship consignment action from the Task Assignment Workflow of an employee to the main process engine.
 */
public class TaskAssignmentShipConsignmentAction extends AbstractTaskAssignmentActions
{
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String CONFIRM_SHIP_CONSIGNMENT_CHOICE = "confirmShipConsignment";
	protected static final String SHIPPING_TEMPLATE_CODE = "NPR_Shipping";

	@Override
	public WorkflowDecisionModel perform(final WorkflowActionModel workflowAction)
	{
		WorkflowDecisionModel result = null;

		if (getAttachedConsignment(workflowAction).isPresent())
		{
			final ConsignmentModel attachedConsignment = (ConsignmentModel) getAttachedConsignment(workflowAction).get();
			getWorkflowActionAndAssignPrincipal(SHIPPING_TEMPLATE_CODE, workflowAction, attachedConsignment, "shipped");
			getConsignmentBusinessProcessService()
					.triggerChoiceEvent(attachedConsignment, CONSIGNMENT_ACTION_EVENT_NAME, CONFIRM_SHIP_CONSIGNMENT_CHOICE);

			result = workflowAction.getDecisions().isEmpty() ? null : workflowAction.getDecisions().iterator().next();
		}
		return result;
	}
}
