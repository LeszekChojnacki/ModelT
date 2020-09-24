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
package de.hybris.platform.omsbackoffice.renderers;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.processengine.BusinessProcessEvent;
import de.hybris.platform.processengine.BusinessProcessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Provides common methods for cell renderers to release objects from waiting steps.
 */
public abstract class ReleaseButtonCellRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseButtonCellRenderer.class);

	private BusinessProcessService businessProcessService;

	/**
	 * Checks if the {@link ConsignmentModel} has the given {@link ConsignmentStatus}
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} to check
	 * @return boolean indicating whether he {@link ConsignmentModel} has the given {@link ConsignmentStatus}
	 */
	protected boolean canPerformOperation(final ConsignmentModel consignmentModel, final ConsignmentStatus consignmentStatus)
	{
		validateParameterNotNullStandardMessage("consignmentModel", consignmentModel);
		validateParameterNotNullStandardMessage("consignmentStatus", consignmentStatus);

		return consignmentStatus.equals(consignmentModel.getStatus());
	}

	/**
	 * Triggers the {@link BusinessProcessService} with the given event and event choice
	 *
	 * @param consignmentModel
	 * 		the {@link ConsignmentModel} for which the event is to be triggered
	 * @param eventName
	 * 		the event to be triggered
	 * @param eventChoice
	 * 		the event choice
	 */
	protected void triggerBusinessProcessEvent(final ConsignmentModel consignmentModel, final String eventName,
			final String eventChoice)
	{
		validateParameterNotNullStandardMessage("consignmentModel", consignmentModel);
		validateParameterNotNullStandardMessage("eventName", eventName);
		validateParameterNotNullStandardMessage("eventChoice", eventChoice);

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(String.format("Manually releasing consignment with code: %s", consignmentModel.getCode()));
		}

		final String processCode = consignmentModel.getConsignmentProcesses().stream().findFirst().get().getCode();

		getBusinessProcessService().triggerEvent(BusinessProcessEvent.builder(processCode + "_" + eventName).withChoice(eventChoice)
				.withEventTriggeringInTheFutureDisabled().build());
	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

}
