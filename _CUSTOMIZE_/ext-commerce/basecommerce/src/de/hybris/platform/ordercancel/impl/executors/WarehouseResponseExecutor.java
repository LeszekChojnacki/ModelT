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

import de.hybris.platform.basecommerce.enums.OrderEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;
import de.hybris.platform.ordercancel.OrderCancelPaymentServiceAdapter;
import de.hybris.platform.ordercancel.OrderCancelRecordsHandler;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.OrderCancelResponseExecutor;
import de.hybris.platform.ordercancel.OrderStatusChangeStrategy;
import de.hybris.platform.ordercancel.OrderUtils;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.log4j.Logger;


/**
 *
 */
public class WarehouseResponseExecutor
		implements OrderCancelResponseExecutor, NotificationServiceAdapterDependent, PaymentServiceAdapterDependent
{
	private static final Logger LOG = Logger.getLogger(WarehouseResponseExecutor.class.getName());

	private ModelService modelService;
	private OrderCancelRecordsHandler orderCancelRecordsHandler;
	private OrderCancelPaymentServiceAdapter paymentServiceAdapter;
	private OrderCancelNotificationServiceAdapter notificationServiceAdapter;
	private OrderStatusChangeStrategy completeCancelStatusChangeStrategy;
	private OrderStatusChangeStrategy partialCancelStatusChangeStrategy;
	private OrderStatusChangeStrategy warehouseDenialStatusChangeStrategy;
	private OrderStatusChangeStrategy warehouseErrorStatusChangeStrategy;


	@Override
	public void processCancelResponse(final OrderCancelResponse orderCancelResponse,
			final OrderCancelRecordEntryModel cancelRequestRecordEntry) throws OrderCancelException
	{
		realizeCancelAfterWarehouseResponse(orderCancelResponse);
	}

	protected void realizeCancelAfterWarehouseResponse(final OrderCancelResponse cancelResponse) throws OrderCancelException
	{
		//this line handles records processing (+ current state validation)
		final OrderCancelRecordEntryModel pendingRecord = orderCancelRecordsHandler.updateRecordEntry(cancelResponse);
		switch (cancelResponse.getResponseStatus())
		{
			case denied:
				if (warehouseDenialStatusChangeStrategy != null)
				{
					warehouseDenialStatusChangeStrategy.changeOrderStatusAfterCancelOperation(pendingRecord, true);
				}
				break;
			case error: //NOSONAR
				if (warehouseErrorStatusChangeStrategy != null)
				{
					warehouseErrorStatusChangeStrategy.changeOrderStatusAfterCancelOperation(pendingRecord, true);
				}
				throw new OrderCancelException(cancelResponse.getOrder().getCode(),
						"Order could not be cancelled due to :" + cancelResponse.getNotes());
			default:
				finalizeCancelProcessing(cancelResponse, pendingRecord);
				break;
		}
	}

	protected void finalizeCancelProcessing(final OrderCancelResponse orderCancelResponse,
			final OrderCancelRecordEntryModel cancelRequestRecordEntry)
	{
		modifyOrderAccordingToRequest(orderCancelResponse);

		final OrderModel order = orderCancelResponse.getOrder();
		modelService.refresh(order);

		if (!OrderUtils.hasLivingEntries(order))
		{
			//Complete cancel
			if (completeCancelStatusChangeStrategy != null)
			{
				completeCancelStatusChangeStrategy.changeOrderStatusAfterCancelOperation(cancelRequestRecordEntry, true);
			}
		}
		else
		{
			//Partial cancel
			if (partialCancelStatusChangeStrategy != null)
			{
				partialCancelStatusChangeStrategy.changeOrderStatusAfterCancelOperation(cancelRequestRecordEntry, true);
			}
		}

		paymentServiceAdapter.recalculateOrderAndModifyPayments(orderCancelResponse.getOrder());

		if (notificationServiceAdapter == null)
		{
			LOG.info("order: " + orderCancelResponse.getOrder().getCode() + " has been "
					+ (orderCancelResponse.isPartialCancel() ? "partially" : "completely") + " cancelled");
		}
		else
		{
			notificationServiceAdapter.sendCancelFinishedNotifications(cancelRequestRecordEntry);
		}
	}

	protected void modifyOrderAccordingToRequest(final OrderCancelResponse cancelResponse)
	{
		for (final OrderCancelEntry oce : cancelResponse.getEntriesToCancel())
		{
			final AbstractOrderEntryModel orderEntry = oce.getOrderEntry();
			final long previousQuantity = orderEntry.getQuantity().longValue();
			if (oce.getCancelQuantity() <= oce.getOrderEntry().getQuantity().longValue())
			{
				orderEntry.setQuantity(Long.valueOf(previousQuantity - oce.getCancelQuantity()));
				if (orderEntry.getQuantity().equals(Long.valueOf(0)))
				{
					orderEntry.setQuantityStatus(OrderEntryStatus.DEAD);
				}

				modelService.save(orderEntry);
			}
			else
			{
				throw new IllegalStateException("Error while cancelling order [" + cancelResponse.getOrder().getCode()
						+ "] Trying to cancel " + oce.getCancelQuantity() + ", whereas orderEntry (" + orderEntry.getPk()
						+ ") has quantity of " + previousQuantity);
			}
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
	 * @return the paymentServiceAdapter
	 */
	public OrderCancelPaymentServiceAdapter getPaymentServiceAdapter()
	{
		return paymentServiceAdapter;
	}

	/**
	 * @param paymentServiceAdapter
	 *           the paymentServiceAdapter to set
	 */
	@Override
	public void setPaymentServiceAdapter(final OrderCancelPaymentServiceAdapter paymentServiceAdapter)
	{
		this.paymentServiceAdapter = paymentServiceAdapter;
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
	 * @return the orderCancelRecordsHandler
	 */
	public OrderCancelRecordsHandler getOrderCancelRecordsHandler()
	{
		return orderCancelRecordsHandler;
	}

	/**
	 * @param orderCancelRecordsHandler
	 *           the orderCancelRecordsHandler to set
	 */
	public void setOrderCancelRecordsHandler(final OrderCancelRecordsHandler orderCancelRecordsHandler)
	{
		this.orderCancelRecordsHandler = orderCancelRecordsHandler;
	}

	/**
	 * @return the completeCancelStatusChangeStrategy
	 */
	public OrderStatusChangeStrategy getCompleteCancelStatusChangeStrategy()
	{
		return completeCancelStatusChangeStrategy;
	}

	/**
	 * @param completeCancelStatusChangeStrategy
	 *           the completeCancelStatusChangeStrategy to set
	 */
	public void setCompleteCancelStatusChangeStrategy(final OrderStatusChangeStrategy completeCancelStatusChangeStrategy)
	{
		this.completeCancelStatusChangeStrategy = completeCancelStatusChangeStrategy;
	}

	/**
	 * @return the partialCancelStatusChangeStrategy
	 */
	public OrderStatusChangeStrategy getPartialCancelStatusChangeStrategy()
	{
		return partialCancelStatusChangeStrategy;
	}

	/**
	 * @param partialCancelStatusChangeStrategy
	 *           the partialCancelStatusChangeStrategy to set
	 */
	public void setPartialCancelStatusChangeStrategy(final OrderStatusChangeStrategy partialCancelStatusChangeStrategy)
	{
		this.partialCancelStatusChangeStrategy = partialCancelStatusChangeStrategy;
	}

	/**
	 * @return the warehouseDenialStatusChangeStrategy
	 */
	public OrderStatusChangeStrategy getWarehouseDenialStatusChangeStrategy()
	{
		return warehouseDenialStatusChangeStrategy;
	}

	/**
	 * @param warehouseDenialStatusChangeStrategy
	 *           the warehouseDenialStatusChangeStrategy to set
	 */
	public void setWarehouseDenialStatusChangeStrategy(final OrderStatusChangeStrategy warehouseDenialStatusChangeStrategy)
	{
		this.warehouseDenialStatusChangeStrategy = warehouseDenialStatusChangeStrategy;
	}

	/**
	 * @return the warehouseErrorStatusChangeStrategy
	 */
	public OrderStatusChangeStrategy getWarehouseErrorStatusChangeStrategy()
	{
		return warehouseErrorStatusChangeStrategy;
	}

	/**
	 * @param warehouseErrorStatusChangeStrategy
	 *           the warehouseErrorStatusChangeStrategy to set
	 */
	public void setWarehouseErrorStatusChangeStrategy(final OrderStatusChangeStrategy warehouseErrorStatusChangeStrategy)
	{
		this.warehouseErrorStatusChangeStrategy = warehouseErrorStatusChangeStrategy;
	}
}
