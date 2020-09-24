/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ordercancel.impl.executors;

import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelRequestExecutor;
import de.hybris.platform.ordercancel.OrderCancelWarehouseAdapter;
import de.hybris.platform.ordercancel.OrderStatusChangeStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This executor uses {@link OrderCancelWarehouseAdapter} to forward cancel requests to the Warehouse for further
 * processing. From this point order cancel processing is suspended until a response from a Warehouse is received. This
 * response contains details if the order cancel request has been performed completely, partially, or not at all.
 */
public class WarehouseProcessingCancelRequestExecutor implements OrderCancelRequestExecutor, NotificationServiceAdapterDependent,
		WarehouseAdapterDependent
{
	private static final Logger LOG = Logger.getLogger(WarehouseProcessingCancelRequestExecutor.class.getName());

	private ModelService modelService;
	private OrderCancelWarehouseAdapter warehouseAdapter;
	private OrderCancelNotificationServiceAdapter notificationServiceAdapter;
	private OrderStatusChangeStrategy orderStatusChangeStrategy;

	@Override
	public void processCancelRequest(final OrderCancelRequest orderCancelRequest,
			final OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		orderStatusChangeStrategy.changeOrderStatusAfterCancelOperation(cancelRequestRecordEntry, true);
		warehouseAdapter.requestOrderCancel(orderCancelRequest);

		if (notificationServiceAdapter == null)
		{
			LOG.info("order: " + orderCancelRequest.getOrder().getCode() + " is being cancelled");
		}
		else
		{
			notificationServiceAdapter.sendCancelPendingNotifications(cancelRequestRecordEntry);
		}
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the warehouseAdapter
	 */
	public OrderCancelWarehouseAdapter getWarehouseAdapter()
	{
		return warehouseAdapter;
	}

	/**
	 * @param warehouseAdapter
	 *           the warehouseAdapter to set
	 */
	@Required
	@Override
	public void setWarehouseAdapter(final OrderCancelWarehouseAdapter warehouseAdapter)
	{
		this.warehouseAdapter = warehouseAdapter;
	}

	/**
	 * @return the notificationServiceAdapter
	 */
	public OrderCancelNotificationServiceAdapter getNotificationServiceAdapter()
	{
		return notificationServiceAdapter;
	}

	/**
	 * @param notificationServiceAdapter
	 *           the notificationServiceAdapter to set
	 */
	@Override
	public void setNotificationServiceAdapter(final OrderCancelNotificationServiceAdapter notificationServiceAdapter)
	{
		this.notificationServiceAdapter = notificationServiceAdapter;
	}

	/**
	 * @return the orderStatusChangeStrategy
	 */
	public OrderStatusChangeStrategy getOrderStatusChangeStrategy()
	{
		return orderStatusChangeStrategy;
	}

	/**
	 * @param orderStatusChangeStrategy
	 *           the orderStatusChangeStrategy to set
	 */
	@Required
	public void setOrderStatusChangeStrategy(final OrderStatusChangeStrategy orderStatusChangeStrategy)
	{
		this.orderStatusChangeStrategy = orderStatusChangeStrategy;
	}
}
