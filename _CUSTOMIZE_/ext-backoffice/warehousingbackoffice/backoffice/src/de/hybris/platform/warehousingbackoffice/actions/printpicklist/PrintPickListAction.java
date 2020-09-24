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
package de.hybris.platform.warehousingbackoffice.actions.printpicklist;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.exceptions.WorkflowActionDecideException;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.warehousingbackoffice.constants.WarehousingBackofficeConstants;

import javax.annotation.Resource;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Strings;


/**
 * Action responsible to generate a picking list
 */
public class PrintPickListAction implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(PrintPickListAction.class);

	protected static final String PICKING_TEMPLATE_CODE = "NPR_Picking";

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintPickSlipStrategy;
	@Resource
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;
	@Resource
	private NotificationService notificationService;


	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final ActionResult<ConsignmentModel> result = new ActionResult<>(ActionResult.SUCCESS);
		final ConsignmentModel consignment = consignmentModelActionContext.getData();

		// check if this button has already been clicked. If so do not invoke the decideWorkflowAction method
		final WorkflowActionModel pickWorkflowAction = getWarehousingConsignmentWorkflowService()
				.getWorkflowActionForTemplateCode(PICKING_TEMPLATE_CODE, consignment);

		if (pickWorkflowAction != null && !WorkflowActionStatus.COMPLETED.equals(pickWorkflowAction.getStatus()))
		{
			try
			{
				getWarehousingConsignmentWorkflowService().decideWorkflowAction(consignment, PICKING_TEMPLATE_CODE, null);
			}
			catch (final WorkflowActionDecideException e)
			{
				getNotificationService()
						.notifyUser(Strings.EMPTY, WarehousingBackofficeConstants.NOTIFICATION_TYPE, NotificationEvent.Level.FAILURE,
								"");
			}
		}

		LOG.info("Generating Pick Label for consignment {}", consignment.getCode());

		getConsignmentPrintPickSlipStrategy().printDocument(consignment);

		return result;
	}

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final Object data = consignmentModelActionContext.getData();
		return (data instanceof ConsignmentModel) && (consignmentModelActionContext.getData().getFulfillmentSystemConfig() == null);
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

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintPickSlipStrategy()
	{
		return consignmentPrintPickSlipStrategy;
	}

	protected WarehousingConsignmentWorkflowService getWarehousingConsignmentWorkflowService()
	{
		return warehousingConsignmentWorkflowService;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}
}
