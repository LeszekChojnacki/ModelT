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
package de.hybris.platform.warehousingbackoffice.actions.printshippinglabel;

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousingbackoffice.labels.strategy.ConsignmentPrintDocumentStrategy;

import javax.annotation.Resource;

import java.util.Map;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;


/**
 * Action responsible for generating a {@link ConsignmentModel#SHIPPINGLABEL}
 */
public class PrintShippingLabelAction implements CockpitAction<ConsignmentModel, ConsignmentModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(PrintShippingLabelAction.class);
	protected static final String POPUP_UNSAVED_CONSIGNMENT_MESSAGE = "shippinglabelpopup.unsaved.consignment.message.question";
	protected static final String POPUP_UNSAVED_CONSIGNMENT_TITLE = "shippinglabelpopup.unsaved.consignment.title";

	@Resource
	private ConsignmentPrintDocumentStrategy consignmentPrintShippingLabelStrategy;
	@Resource
	private ModelService modelService;

	@Override
	public ActionResult<ConsignmentModel> perform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final ConsignmentModel consignment = consignmentModelActionContext.getData();
		final ActionResult<ConsignmentModel> actionResult = new ActionResult<ConsignmentModel>(ActionResult.SUCCESS);

		final Map<String, Object> parentWidgetMap = (Map<String, Object>) consignmentModelActionContext
				.getParameter("parentWidgetModel");

		if (consignment.getShippingLabel() == null && (boolean) parentWidgetMap.get("valueChanged"))
		{
			Messagebox.show(consignmentModelActionContext.getLabel(POPUP_UNSAVED_CONSIGNMENT_MESSAGE),
					consignmentModelActionContext.getLabel(POPUP_UNSAVED_CONSIGNMENT_TITLE),
					new Messagebox.Button[] { Messagebox.Button.NO, Messagebox.Button.YES }, Messagebox.QUESTION,
					clickEvent -> processMessageboxEvent(clickEvent, consignmentModelActionContext, parentWidgetMap));
		}
		else
		{
			printShippingLabel(consignment);
			actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
		}
		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		final Object data = consignmentModelActionContext.getData();
		return (data instanceof ConsignmentModel) && !(((ConsignmentModel) data)
				.getDeliveryMode() instanceof PickUpDeliveryModeModel) &&
				(consignmentModelActionContext.getData().getFulfillmentSystemConfig() == null);
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(ActionContext<ConsignmentModel> consignmentModelActionContext)
	{
		return null;
	}

	/***
	 * Handles the message box popup action when the object is modified. Saves the {@link ConsignmentModel} on YES. Refreshes the {@link ConsignmentModel} on NO.
	 * Reloads the {@link ConsignmentModel} in backoffice.
	 * @param clickEvent the event that contains the button clicked
	 * @param consignmentModelActionContext the action context that contains the consignment
	 */
	protected void processMessageboxEvent(final Event clickEvent,
			final ActionContext<ConsignmentModel> consignmentModelActionContext, final Map<String, Object> parentWidgetMap)
	{
		if (Messagebox.Button.YES.event.equals(clickEvent.getName()))
		{
			final ConsignmentModel consignment = consignmentModelActionContext.getData();

			getModelService().save(consignment);
			printShippingLabel(consignment);
			// Reload the consignment in backoffice UI
			getModelService().refresh(consignment);
			parentWidgetMap.put("currentObject", consignment);
		}
	}

	/***
	 * Generates a shipping label for {@link ConsignmentModel} based on the {@link ConsignmentPrintDocumentStrategy}
	 * @param consignment the consignment
	 */
	protected void printShippingLabel(final ConsignmentModel consignment)
	{
		LOG.info("Generate Print shipping label for consignment {}", consignment.getCode());
		getConsignmentPrintShippingLabelStrategy().printDocument(consignment);
	}

	protected ConsignmentPrintDocumentStrategy getConsignmentPrintShippingLabelStrategy()
	{
		return consignmentPrintShippingLabelStrategy;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}
}
