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
package de.hybris.platform.warehousing.onhold.service.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.onhold.service.OrderOnHoldService;
import de.hybris.platform.warehousing.process.WarehousingBusinessProcessService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link OrderOnHoldService}
 */
public class DefaultOrderOnHoldService implements OrderOnHoldService
{
	protected static final String CONSIGNMENT_ACTION_EVENT_NAME = "ConsignmentActionEvent";
	protected static final String CANCEL_CONSIGNMENT_CHOICE = "cancelConsignment";

	private ModelService modelService;
	private List<ConsignmentStatus> nonCancellableConsignmentStatus;
	private WarehousingBusinessProcessService<ConsignmentModel> consignmentBusinessProcessService;
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;
	private InventoryEventService inventoryEventService;

	@Override
	public void processOrderOnHold(final OrderModel order)
	{
		validateParameterNotNullStandardMessage("order", order);

		order.setStatus(OrderStatus.ON_HOLD);
		getModelService().save(order);
		order.getConsignments().stream()
				.filter(consignment -> !getNonCancellableConsignmentStatus().contains(consignment.getStatus()))
				.forEach(consignment ->
				{
					consignment.setStatus(ConsignmentStatus.CANCELLED);
					getModelService().save(consignment);
					getConsignmentBusinessProcessService()
							.triggerChoiceEvent(consignment, CONSIGNMENT_ACTION_EVENT_NAME, CANCEL_CONSIGNMENT_CHOICE);
					getWarehousingConsignmentWorkflowService().terminateConsignmentWorkflow(consignment);
					getModelService().removeAll(getInventoryEventService().getAllocationEventsForConsignment(consignment));
				});
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

	protected List<ConsignmentStatus> getNonCancellableConsignmentStatus()
	{
		return nonCancellableConsignmentStatus;
	}

	@Required
	public void setNonCancellableConsignmentStatus(
			final List<ConsignmentStatus> nonCancellableConsignmentStatus)
	{
		this.nonCancellableConsignmentStatus = nonCancellableConsignmentStatus;
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

	protected InventoryEventService getInventoryEventService()
	{
		return inventoryEventService;
	}

	@Required
	public void setInventoryEventService(final InventoryEventService inventoryEventService)
	{
		this.inventoryEventService = inventoryEventService;
	}
}
