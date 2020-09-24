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
package de.hybris.platform.warehousing.shipping.service.impl;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.enums.ProcessState;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.constants.WarehousingConstants;
import de.hybris.platform.warehousing.process.BusinessProcessException;
import de.hybris.platform.warehousing.shipping.service.WarehousingShippingService;
import de.hybris.platform.warehousing.taskassignment.services.WarehousingConsignmentWorkflowService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link WarehousingShippingService}
 */
public class DefaultWarehousingShippingService implements WarehousingShippingService
{
	protected static final String CONFIRM_SHIP_CONSIGNMENT_CHOICE = "confirmShipConsignment";
	protected static final String CONFIRM_PICKUP_CONSIGNMENT_CHOICE = "confirmPickupConsignment";
	protected static final String SHIPPING_TEMPLATE_CODE = "NPR_Shipping";
	protected static final String PICKUP_TEMPLATE_CODE = "NPR_Pickup";

	private ModelService modelService;
	private List<OrderStatus> validConsConfirmOrderStatusList;
	private List<ConsignmentStatus> validConsConfirmConsignmentStatusList;
	private WarehousingConsignmentWorkflowService warehousingConsignmentWorkflowService;

	@Override
	public boolean isConsignmentConfirmable(final ConsignmentModel consignment)
	{
		validateParameterNotNullStandardMessage("consignment", consignment);

		ConsignmentModel upToDateConsignment;
		if (getModelService().isUpToDate(consignment))
		{
			upToDateConsignment = consignment;
		}
		else
		{
			// Instead of refreshing, we get an updated decoupled object to prevent backoffice UI from refreshing.
			upToDateConsignment = getModelService().get(consignment.getPk());
		}
		Assert.notNull(upToDateConsignment.getOrder(),
				String.format("Order cannot be null for the Consignment with code: [%s]", upToDateConsignment.getCode()));
		final AbstractOrderModel order = upToDateConsignment.getOrder();

		Assert.notNull(order.getStatus(),
				String.format("Order Status cannot be null for the Order with code: [%s].", order.getCode()));

		final String consignmentProcessCode = upToDateConsignment.getCode() + WarehousingConstants.CONSIGNMENT_PROCESS_CODE_SUFFIX;
		final Optional<ConsignmentProcessModel> consignmentProcess = upToDateConsignment.getConsignmentProcesses().stream()
				.filter(consProcess -> consProcess.getCode().equals(consignmentProcessCode)).findFirst();
		Assert.isTrue(consignmentProcess.isPresent(),
				String.format("No process found for the Consignment with the code: [%s].", upToDateConsignment.getCode()));

		return getValidConsConfirmConsignmentStatusList().contains(upToDateConsignment.getStatus())
				&& getValidConsConfirmOrderStatusList().contains(order.getStatus()) && !ProcessState.SUCCEEDED
				.equals(consignmentProcess.get().getState()) &&
				upToDateConsignment.getConsignmentEntries().stream().filter(entry -> Objects.nonNull(entry.getQuantityPending()))
						.mapToLong(ConsignmentEntryModel::getQuantityPending).sum() > 0;
	}

	@Override
	public void confirmShipConsignment(final ConsignmentModel consignment) throws BusinessProcessException
	{
		getWarehousingConsignmentWorkflowService()
				.decideWorkflowAction(consignment, SHIPPING_TEMPLATE_CODE, CONFIRM_SHIP_CONSIGNMENT_CHOICE);
	}

	@Override
	public void confirmPickupConsignment(final ConsignmentModel consignment) throws BusinessProcessException
	{
		getWarehousingConsignmentWorkflowService()
				.decideWorkflowAction(consignment, PICKUP_TEMPLATE_CODE, CONFIRM_PICKUP_CONSIGNMENT_CHOICE);
	}

	protected List<ConsignmentStatus> getValidConsConfirmConsignmentStatusList()
	{
		return validConsConfirmConsignmentStatusList;
	}

	@Required
	public void setValidConsConfirmConsignmentStatusList(final List<ConsignmentStatus> validConsConfirmConsignmentStatusList)
	{
		this.validConsConfirmConsignmentStatusList = validConsConfirmConsignmentStatusList;
	}

	protected List<OrderStatus> getValidConsConfirmOrderStatusList()
	{
		return validConsConfirmOrderStatusList;
	}

	@Required
	public void setValidConsConfirmOrderStatusList(final List<OrderStatus> validConsConfirmOrderStatusList)
	{
		this.validConsConfirmOrderStatusList = validConsConfirmOrderStatusList;
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
