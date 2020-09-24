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
package de.hybris.platform.warehousingbackoffice.actions.printpacklabel;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action responsible to generate a packing label
 */
public class PrintPackLabelAction implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(PrintPackLabelAction.class);

	protected static final String PACK_CONSIGNMENT_CHOICE = "packConsignment";
	protected static final String PACKING_TEMPLATE_CODE = "NPR_Packing";
	protected static final String CAPTURE_PAYMENT_ON_CONSIGNMENT = "warehousing.capturepaymentonconsignment";

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintPackSlipStrategy;
	@Resource
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;
	@Resource
	private ConfigurationService configurationService;

	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{

		final ConsignmentModel consignment = consignmentModelActionContext.getData();

		LOG.info("Generating Pack Label for consignment {}", consignment.getCode());

		// check if this button has already been clicked. If so do not invoke the decideWorkflowAction method
		final WorkflowActionModel packWorkflowAction = getWarehousingConsignmentWorkflowService()
				.getWorkflowActionForTemplateCode(PACKING_TEMPLATE_CODE, consignment);
		if (packWorkflowAction != null && !WorkflowActionStatus.COMPLETED.equals(packWorkflowAction.getStatus()))
		{
			// Trigger pack action in the consignment process.
			try
			{
				getWarehousingConsignmentWorkflowService()
						.decideWorkflowAction(consignment, PACKING_TEMPLATE_CODE, PACK_CONSIGNMENT_CHOICE);
			}
			catch (final BusinessProcessException e) //NOSONAR
			{
				LOG.info("Unable to trigger pack consignment process for consignment: {}", consignment.getCode());
			}
		}

		// Display pack slip to user
		getConsignmentPrintPackSlipStrategy().printDocument(consignment);

		return new ActionResult<>(ActionResult.SUCCESS);
	}

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final Object data = consignmentModelActionContext.getData();
		return (data instanceof ConsignmentModel) && getConfigurationService().getConfiguration()
				.getBoolean(CAPTURE_PAYMENT_ON_CONSIGNMENT, false) && !ConsignmentStatus.CANCELLED
				.equals(((ConsignmentModel) data).getStatus()) && (((ConsignmentModel) data).getFulfillmentSystemConfig() == null);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return null;
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintPackSlipStrategy()
	{
		return consignmentPrintPackSlipStrategy;
	}

	protected WarehousingConsignmentWorkflowService getWarehousingConsignmentWorkflowService()
	{
		return warehousingConsignmentWorkflowService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}
}
