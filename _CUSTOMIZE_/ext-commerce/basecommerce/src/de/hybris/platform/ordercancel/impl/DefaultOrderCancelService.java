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
package de.hybris.platform.ordercancel.impl;

import de.hybris.platform.basecommerce.enums.OrderCancelState;
import de.hybris.platform.basecommerce.enums.OrderModificationEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.jalo.order.OrderEntry;
import de.hybris.platform.ordercancel.CancelDecision;
import de.hybris.platform.ordercancel.OrderCancelCallbackService;
import de.hybris.platform.ordercancel.OrderCancelCancelableEntriesStrategy;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.OrderCancelDeniedException;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelRecordsHandler;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelRequestExecutor;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.OrderCancelResponseExecutor;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.ordercancel.OrderCancelStateMappingStrategy;
import de.hybris.platform.ordercancel.dao.OrderCancelDao;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Order Cancel Service implementation
 */
public class DefaultOrderCancelService implements OrderCancelService, OrderCancelCallbackService
{
	private ModelService modelService;
	private OrderCancelRecordsHandler orderCancelRecordsHandler;
	private OrderCancelDao orderCancelDao;
	private OrderCancelStateMappingStrategy stateMappingStrategy;
	private List<OrderCancelDenialStrategy> cancelDenialStrategies;
	private Map<OrderCancelState, OrderCancelRequestExecutor> requestExecutorsMap;
	private Map<OrderCancelState, OrderCancelResponseExecutor> responseExecutorsMap;
	private OrderCancelCancelableEntriesStrategy cancelableEntriesStrategy;

	@Override
	public OrderCancelRecordModel getCancelRecordForOrder(final OrderModel order)
	{
		return orderCancelRecordsHandler.getCancelRecord(order);
	}

	@Override
	public OrderCancelRecordEntryModel getPendingCancelRecordEntry(final OrderModel order) throws OrderCancelException
	{
		return orderCancelRecordsHandler.getPendingCancelRecordEntry(order);
	}

	@Override
	public CancelDecision isCancelPossible(final OrderModel order, final PrincipalModel requestor, final boolean partialCancel,
			final boolean partialEntryCancel)
	{
		final OrderCancelConfigModel configuration = this.getConfiguration();

		final List<OrderCancelDenialReason> reasons = new ArrayList<OrderCancelDenialReason>();
		for (final OrderCancelDenialStrategy ocas : cancelDenialStrategies)
		{
			final OrderCancelDenialReason result = ocas.getCancelDenialReason(configuration, order, requestor, partialCancel,
					partialEntryCancel);
			if (result != null)
			{
				reasons.add(result);
			}
		}

		final boolean cancelAllowed;

		if (reasons.isEmpty())
		{
			cancelAllowed = true;
		}
		else
		{
			cancelAllowed = false;
		}

		return new CancelDecision(cancelAllowed, reasons);
	}

	/**
	 * Returns all cancellable {@link OrderEntry}. Uses {@link OrderCancelCancelableEntriesStrategy} to perform logic
	 * "which entries are cancellable".
	 * 
	 * 
	 * @param order
	 *           Order that is subject to cancel
	 * @param requestor
	 *           Principal that originates the request ("issuer of the request"). It might be different from current
	 *           session user. (can be null)
	 * @return the cancellable {@link AbstractOrderEntryModel} and their cancellable quantity.
	 */
	@Override
	public Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(final OrderModel order, final PrincipalModel requestor)
	{
		return this.cancelableEntriesStrategy.getAllCancelableEntries(order, requestor);
	}

	@Override
	public OrderCancelConfigModel getConfiguration()
	{
		return orderCancelDao.getOrderCancelConfiguration();
	}

	@Override
	public OrderCancelRecordEntryModel requestOrderCancel(final OrderCancelRequest orderCancelRequest,
			final PrincipalModel requestor) throws OrderCancelException
	{
		final OrderCancelRecordEntryModel result;

		final CancelDecision cancelDecision = isCancelPossible(orderCancelRequest.getOrder(), requestor, orderCancelRequest
				.isPartialCancel(), orderCancelRequest.isPartialEntryCancel());
		if (cancelDecision.isAllowed())
		{
			result = orderCancelRecordsHandler.createRecordEntry(orderCancelRequest, requestor);

			final OrderCancelState currentCancelState = stateMappingStrategy.getOrderCancelState(orderCancelRequest.getOrder());

			final OrderCancelRequestExecutor ocre = this.requestExecutorsMap.get(currentCancelState);

			if (ocre == null)
			{
				throw new IllegalStateException("Cannot find request executor for cancel state: " + currentCancelState.name());
			}
			else
			{
				ocre.processCancelRequest(orderCancelRequest, result);
			}
		}
		else
		{
			throw new OrderCancelDeniedException(orderCancelRequest.getOrder().getCode(), cancelDecision);
		}

		return result;
	}

	@Override
	public void onOrderCancelResponse(final OrderCancelResponse cancelResponse) throws OrderCancelException
	{
		//Find
		final OrderCancelRecordModel ocrm = this.getCancelRecordForOrder(cancelResponse.getOrder());
		if (ocrm.isInProgress())
		{
			final OrderModificationRecordEntryModel pendingRecord = findPendingCancelRequest(ocrm);

			final OrderCancelState currentCancelState = stateMappingStrategy.getOrderCancelState(pendingRecord
					.getModificationRecord().getOrder());

			final OrderCancelResponseExecutor ocre = this.responseExecutorsMap.get(currentCancelState);

			if (ocre == null)
			{
				throw new IllegalStateException("Cannot find response executor for cancel state: " + currentCancelState.name());
			}
			else
			{
				ocre.processCancelResponse(cancelResponse, (OrderCancelRecordEntryModel) pendingRecord);
			}
		}
		else
		{
			throw new IllegalArgumentException("No pending cancel requests for given order found");
		}
	}

	protected OrderModificationRecordEntryModel findPendingCancelRequest(final OrderCancelRecordModel ocrm)
	{
		OrderModificationRecordEntryModel pendingRecord = null;
		for (final OrderModificationRecordEntryModel omrem : ocrm.getModificationRecordEntries())
		{
			if (OrderModificationEntryStatus.INPROGRESS == omrem.getStatus())
			{
				if (pendingRecord != null)
				{
					throw new IllegalStateException("more than one pending cancel requests for given order found");
				}
				pendingRecord = omrem;
			}
		}
		if (pendingRecord == null)
		{
			throw new IllegalArgumentException("No pending cancel requests for given order found");
		}
		return pendingRecord;
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
	 * @return the stateMappingStrategy
	 */
	public OrderCancelStateMappingStrategy getStateMappingStrategy()
	{
		return stateMappingStrategy;
	}

	/**
	 * @param stateMappingStrategy
	 *           the stateMappingStrategy to set
	 */
	public void setStateMappingStrategy(final OrderCancelStateMappingStrategy stateMappingStrategy)
	{
		this.stateMappingStrategy = stateMappingStrategy;
	}

	/**
	 * @return the cancelDenialStrategies
	 */
	public List<OrderCancelDenialStrategy> getCancelDenialStrategies()
	{
		return cancelDenialStrategies;
	}

	/**
	 * @param cancelDenialStrategies
	 *           the cancelDenialStrategies to set
	 */
	public void setCancelDenialStrategies(final List<OrderCancelDenialStrategy> cancelDenialStrategies)
	{
		this.cancelDenialStrategies = cancelDenialStrategies;
	}

	/**
	 * @return the requestExecutorsMap
	 */
	public Map<OrderCancelState, OrderCancelRequestExecutor> getRequestExecutorsMap()
	{
		return requestExecutorsMap;
	}

	/**
	 * @param requestExecutorsMap
	 *           the requestExecutorsMap to set
	 */
	public void setRequestExecutorsMap(final Map<OrderCancelState, OrderCancelRequestExecutor> requestExecutorsMap)
	{
		this.requestExecutorsMap = requestExecutorsMap;
	}

	/**
	 * @return the responseExecutorsMap
	 */
	public Map<OrderCancelState, OrderCancelResponseExecutor> getResponseExecutorsMap()
	{
		return responseExecutorsMap;
	}

	/**
	 * @param responseExecutorsMap
	 *           the responseExecutorsMap to set
	 */
	public void setResponseExecutorsMap(final Map<OrderCancelState, OrderCancelResponseExecutor> responseExecutorsMap)
	{
		this.responseExecutorsMap = responseExecutorsMap;
	}

	/**
	 * @return the orderCancelDao
	 */
	public OrderCancelDao getOrderCancelDao()
	{
		return orderCancelDao;
	}

	/**
	 * @param orderCancelDao
	 *           the orderCancelDao to set
	 */
	public void setOrderCancelDao(final OrderCancelDao orderCancelDao)
	{
		this.orderCancelDao = orderCancelDao;
	}

	/**
	 * @return the cancelableEntriesStrategy
	 */
	public OrderCancelCancelableEntriesStrategy getCancelableEntriesStrategy()
	{
		return cancelableEntriesStrategy;
	}

	/**
	 * @param cancelableEntriesStrategy
	 *           the cancellableEntriesStrategy to set
	 */
	public void setCancelableEntriesStrategy(final OrderCancelCancelableEntriesStrategy cancelableEntriesStrategy)
	{
		this.cancelableEntriesStrategy = cancelableEntriesStrategy;
	}
}
