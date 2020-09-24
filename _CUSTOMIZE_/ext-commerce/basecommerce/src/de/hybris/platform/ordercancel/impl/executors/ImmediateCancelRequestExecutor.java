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
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelRequestExecutor;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.OrderCancelResponse.ResponseStatus;
import de.hybris.platform.ordercancel.OrderStatusChangeStrategy;
import de.hybris.platform.ordercancel.OrderUtils;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.log4j.Logger;


/**
 */
public class ImmediateCancelRequestExecutor
		implements OrderCancelRequestExecutor, NotificationServiceAdapterDependent, PaymentServiceAdapterDependent
{
	private static final Logger LOG = Logger.getLogger(ImmediateCancelRequestExecutor.class.getName());

	private ModelService modelService;
	private OrderCancelPaymentServiceAdapter paymentServiceAdapter;
	private OrderCancelNotificationServiceAdapter notificationServiceAdapter;
	private OrderCancelRecordsHandler orderCancelRecordsHandler;
	private OrderStatusChangeStrategy completeCancelStatusChangeStrategy;
	private OrderStatusChangeStrategy partialCancelStatusChangeStrategy;

	@Override
	public void processCancelRequest(final OrderCancelRequest orderCancelRequest,
			final OrderCancelRecordEntryModel cancelRequestRecordEntry) throws OrderCancelException
	{

		modifyOrderAccordingToRequest(orderCancelRequest);

		final OrderModel order = orderCancelRequest.getOrder();
		modelService.refresh(order);

		if (!OrderUtils.hasLivingEntries(order))
		{
			//Complete Cancel
			if (completeCancelStatusChangeStrategy != null)
			{
				completeCancelStatusChangeStrategy.changeOrderStatusAfterCancelOperation(cancelRequestRecordEntry, true);
			}
		}
		else
		{
			//Partial Cancel
			if (partialCancelStatusChangeStrategy != null)
			{
				partialCancelStatusChangeStrategy.changeOrderStatusAfterCancelOperation(cancelRequestRecordEntry, true);
			}
		}

		//call order recalculation and payment modifications
		if (paymentServiceAdapter != null)
		{
			paymentServiceAdapter.recalculateOrderAndModifyPayments(orderCancelRequest.getOrder());
		}
		else if (LOG.isDebugEnabled())
		{
			LOG.debug("Missing OrderCancelPaymentServiceAdapter!");
		}

		//call notification service
		if (notificationServiceAdapter == null)
		{
			LOG.info("order: " + orderCancelRequest.getOrder().getCode() + " has been "
					+ (!orderCancelRequest.isPartialCancel() ? "completely" : "partially") + " cancelled");
		}
		else
		{
			notificationServiceAdapter.sendCancelFinishedNotifications(cancelRequestRecordEntry);
		}

		//update record entries
		orderCancelRecordsHandler.updateRecordEntry(makeInternalResponse(orderCancelRequest, true, null));

		updateOrderProcess(orderCancelRequest);
	}

	/**
	 * Updates {@link de.hybris.platform.orderprocessing.model.OrderProcessModel} after performing cancellation
	 *
	 * @param orderCancelRequest
	 * 		the order being cancelled
	 */
	@SuppressWarnings("squid:S1172")
	protected void updateOrderProcess(final OrderCancelRequest orderCancelRequest)
	{
		LOG.info("Not updating the order process. Please provide your own implementation");
	}

	protected void modifyOrderAccordingToRequest(final OrderCancelRequest cancelRequest) throws OrderCancelException
	{
		for (final OrderCancelEntry oce : cancelRequest.getEntriesToCancel())
		{
			final AbstractOrderEntryModel orderEntry = oce.getOrderEntry();
			final long previousQuantity = orderEntry.getQuantity().longValue();

			if (oce.getCancelQuantity() <= oce.getOrderEntry().getQuantity().longValue())
			{
				orderEntry.setQuantity(Long.valueOf(previousQuantity - oce.getCancelQuantity()));

				if (previousQuantity == oce.getCancelQuantity())
				{
					orderEntry.setQuantityStatus(OrderEntryStatus.DEAD);
				}
				modelService.save(orderEntry);
			}
			else
			{
				throw new OrderCancelException(cancelRequest.getOrder().getCode(), "Trying to cancel " + oce.getCancelQuantity()
						+ ", whereas orderEntry (" + orderEntry.getPk() + ") has quantity of " + previousQuantity);
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

	protected OrderCancelResponse makeInternalResponse(final OrderCancelRequest request, final boolean success,
			final String message)
	{
		if (request.isPartialCancel())
		{
			return new OrderCancelResponse(request.getOrder(), request.getEntriesToCancel(),
					success ? ResponseStatus.partial : ResponseStatus.error, message);
		}
		else
		{
			return new OrderCancelResponse(request.getOrder(), success ? ResponseStatus.full : ResponseStatus.error, message);
		}
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
}
