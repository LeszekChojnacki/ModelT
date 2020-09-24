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
package de.hybris.platform.warehousing.taskassignment.services;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;


/**
 * Warehousing service that creates and starts a consignment {@link ConsignmentModel} workflow.
 */
public interface WarehousingConsignmentWorkflowService
{
	/**
	 * Starts a workflow for the given consignment {@link ConsignmentModel}.
	 *
	 * @param consignment
	 * 		{@link ConsignmentModel} for which a workflow needs to be started
	 */
	void startConsignmentWorkflow(ConsignmentModel consignment);

	/**
	 * Terminates a workflow assigned for the given consignment {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		{@link ConsignmentModel} which we want to terminate the workflow for
	 */
	void terminateConsignmentWorkflow(ConsignmentModel consignment);

	/**
	 * Decides which {@link WorkflowActionModel} to trigger from a given {@link ConsignmentModel} and moves consignment's business process
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} on which your action happened
	 * @param templateCode
	 * 		the {@link de.hybris.platform.workflow.model.WorkflowTemplateModel#CODE}
	 * @param choice
	 * 		the string code of the consignment's {@link de.hybris.platform.processengine.model.BusinessProcessModel} choice to be processed
	 */
	void decideWorkflowAction(ConsignmentModel consignment, String templateCode, String choice);

	/**
	 * Returns the {@link WorkflowActionModel} of the given {@link ConsignmentModel} for the given {@link de.hybris.platform.workflow.model.WorkflowTemplateModel#CODE}
	 *
	 * @param templateCode
	 * 		the {@link de.hybris.platform.workflow.model.WorkflowTemplateModel#CODE} to retrieve
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which the {@link WorkflowActionModel} is required
	 * @return the {@link WorkflowActionModel}
	 */
	WorkflowActionModel getWorkflowActionForTemplateCode(String templateCode, ConsignmentModel consignment);
}
