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
package de.hybris.platform.returns.impl;

import de.hybris.platform.basecommerce.enums.OrderModificationEntryStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordercancel.exceptions.OrderCancelDaoException;
import de.hybris.platform.orderhistory.OrderHistoryService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.ordermodify.model.OrderEntryModificationRecordEntryModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordEntryModel;
import de.hybris.platform.refund.model.OrderRefundRecordEntryModel;
import de.hybris.platform.returns.OrderReturnRecordHandler;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.dao.OrderReturnDao;
import de.hybris.platform.returns.model.OrderEntryReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordEntryModel;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;



public class DefaultOrderReturnRecordsHandler implements OrderReturnRecordHandler
{
	private OrderReturnDao orderReturnDao;
	private OrderHistoryService orderHistoryService;
	private ModelService modelService;

	@Override
	public OrderReturnRecordEntryModel createRefundEntry(final OrderModel order, final List<RefundEntryModel> refunds,
			final String description) throws OrderReturnRecordsHandlerException
	{
		final OrderHistoryEntryModel snapshot = createSnaphot(order, description);
		try
		{
			final OrderReturnRecordModel returnRecord = getOrCreateReturnRecord(order);
			if (returnRecord.isInProgress())
			{
				throw new IllegalStateException(
						"Cannot create new Order return request - the order return record indicates: Return already in progress");
			}
			returnRecord.setInProgress(true);
			getModelService().save(returnRecord);
			return createRefundRecordEntry(order, returnRecord, snapshot, refunds, null);

		}
		catch (final OrderCancelDaoException e)
		{
			throw new OrderReturnRecordsHandlerException(order.getCode(), e);
		}
	}

	@Override
	public OrderReturnRecordModel finalizeOrderReturnRecordForReturnRequest(final ReturnRequestModel returnRequest)
	{
		final OrderReturnRecordEntryModel orderReturnRecordEntry = getPendingReturnRecordEntryForReturnRequest(returnRequest);
		if (orderReturnRecordEntry == null)
		{
			throw new IllegalStateException(
					"Order[" + returnRequest.getOrder().getCode() + "]: return record has no entry with status 'IN PROGRESS'");
		}
		finalizeOrderReturnRecordEntry(orderReturnRecordEntry);

		final OrderReturnRecordModel orderReturnRecord = (OrderReturnRecordModel) orderReturnRecordEntry.getModificationRecord();
		finalizeOrderReturnRecord(orderReturnRecord);

		return orderReturnRecord;
	}

	@Override
	public OrderReturnRecordEntryModel getPendingReturnRecordEntryForReturnRequest(final ReturnRequestModel returnRequest)
	{
		final OrderReturnRecordModel orderReturnRecord = getPendingReturnRecordForReturnRequest(returnRequest);
		final Collection<OrderModificationRecordEntryModel> entries = orderReturnRecord.getModificationRecordEntries();
		if (CollectionUtils.isEmpty(entries))
		{
			throw new IllegalStateException("Order[" + returnRequest.getOrder().getCode() + "]: has no return records");
		}

		OrderReturnRecordEntryModel currentReturnEntry = null;
		for (final OrderModificationRecordEntryModel orderModificationRecordEntry : entries)
		{
			final OrderReturnRecordEntryModel orderReturnRecordEntry = (OrderReturnRecordEntryModel) orderModificationRecordEntry;
			if (isReturnRecordEntryForReturnRequest(returnRequest, orderReturnRecordEntry))
			{
				if (currentReturnEntry == null)
				{
					currentReturnEntry = orderReturnRecordEntry;
				}
				else
				{
					throw new IllegalStateException("Return request[" + returnRequest.getCode()
							+ "]: has more than one return record entries with status 'IN PROGRESS'");
				}
			}
		}
		return currentReturnEntry;
	}

	/**
	 * Extracts the {@link OrderReturnRecordModel} for given {@link ReturnRequestModel}
	 *
	 * @param returnRequest
	 *           the given {@link ReturnRequestModel}
	 * @return the {@link OrderReturnRecordModel}
	 */
	protected OrderReturnRecordModel getPendingReturnRecordForReturnRequest(final ReturnRequestModel returnRequest)
	{
		ServicesUtil.validateParameterNotNull(returnRequest, "Return Request cannot be null");
		ServicesUtil.validateParameterNotNull(returnRequest.getOrder(), "Order cannot be null ");
		final OrderModel orderModel = returnRequest.getOrder();

		final OrderReturnRecordModel orderReturnRecord = getReturnRecord(orderModel);
		if (orderReturnRecord == null || !orderReturnRecord.isInProgress())
		{
			throw new IllegalStateException("Order[" + orderModel.getCode() + "]: return is not currently in progress");
		}
		return orderReturnRecord;
	}

	/**
	 * Validates if the given {@link OrderReturnRecordEntryModel} belongs to the given {@link ReturnRequestModel}
	 *
	 * @param returnRequest
	 *           the {@link ReturnRequestModel}
	 * @param orderReturnRecordEntry
	 *           the {@link OrderReturnRecordEntryModel} to be validated
	 * @return the boolean indicating if the {@link OrderReturnRecordEntryModel} belongs to given
	 *         {@link ReturnRequestModel}
	 */
	@SuppressWarnings("squid:S1168")
	protected boolean isReturnRecordEntryForReturnRequest(final ReturnRequestModel returnRequest,
			final OrderReturnRecordEntryModel orderReturnRecordEntry)
	{
		ServicesUtil.validateParameterNotNull(orderReturnRecordEntry, "Order return record entry cannot be null");
		return OrderModificationEntryStatus.INPROGRESS.equals(orderReturnRecordEntry.getStatus());
	}

	/**
	 * Updates {@link OrderReturnRecordEntryModel} properties, after return request is finalized
	 *
	 * @param orderReturnRecordEntry
	 *           the {@link OrderReturnRecordEntryModel} to be updated
	 */
	protected void finalizeOrderReturnRecordEntry(final OrderReturnRecordEntryModel orderReturnRecordEntry)
	{
		ServicesUtil.validateParameterNotNull(orderReturnRecordEntry, "Order return record entry cannot be null");
		for (final OrderEntryModificationRecordEntryModel orderEntryModificationRecordEntry : orderReturnRecordEntry
				.getOrderEntriesModificationEntries())
		{
			final OrderEntryReturnRecordEntryModel orderEntryReturnEntry = (OrderEntryReturnRecordEntryModel) orderEntryModificationRecordEntry;
			orderEntryReturnEntry.setReturnedQuantity(orderEntryReturnEntry.getExpectedQuantity());
			getModelService().save(orderEntryReturnEntry);
		}
		orderReturnRecordEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
		getModelService().save(orderReturnRecordEntry);
	}

	/**
	 * Updates {@link OrderReturnRecordModel} properties, after return request is finalized
	 *
	 * @param orderReturnRecord
	 *           the {@link OrderReturnRecordModel} to be updated
	 */
	protected void finalizeOrderReturnRecord(final OrderReturnRecordModel orderReturnRecord)
	{
		ServicesUtil.validateParameterNotNull(orderReturnRecord, "Order return record cannot be null");
		orderReturnRecord.setInProgress(false);
		getModelService().save(orderReturnRecord);
	}

	/**
	 * @param order
	 * @return {@link OrderRefundRecordEntryModel}
	 * @throws OrderReturnRecordsHandlerException
	 */
	@SuppressWarnings("squid:S1168")
	protected OrderReturnRecordEntryModel createRefundRecordEntry(final OrderModel order,
			final OrderReturnRecordModel returnRecord, final OrderHistoryEntryModel snapshot, final List<RefundEntryModel> refunds,
			final UserModel principal) throws OrderReturnRecordsHandlerException
	{

		final OrderRefundRecordEntryModel refundRecordEntry = getModelService().create(OrderRefundRecordEntryModel.class);
		refundRecordEntry.setTimestamp(new Date());
		refundRecordEntry.setCode(generateEntryCode(snapshot));
		refundRecordEntry.setOriginalVersion(snapshot);
		refundRecordEntry.setModificationRecord(returnRecord);
		refundRecordEntry.setPrincipal(principal);
		refundRecordEntry.setOwner(returnRecord);
		refundRecordEntry.setStatus(OrderModificationEntryStatus.INPROGRESS);

		getModelService().save(refundRecordEntry);

		final List<OrderEntryModificationRecordEntryModel> orderEntriesRecords = new ArrayList<OrderEntryModificationRecordEntryModel>();

		for (final RefundEntryModel refundEntry : refunds)
		{
			final OrderEntryReturnRecordEntryModel orderEntryRefundEntry = getModelService()
					.create(OrderEntryReturnRecordEntryModel.class);
			orderEntryRefundEntry.setCode(refundEntry.getOrderEntry().getPk().toString());
			orderEntryRefundEntry.setExpectedQuantity(refundEntry.getExpectedQuantity());
			orderEntryRefundEntry.setModificationRecordEntry(refundRecordEntry);
			orderEntryRefundEntry.setOriginalOrderEntry(getOriginalOrderEntry(snapshot, refundEntry));
			getModelService().save(orderEntryRefundEntry);
			orderEntriesRecords.add(orderEntryRefundEntry);
		}

		refundRecordEntry.setOrderEntriesModificationEntries(orderEntriesRecords);

		getModelService().saveAll();

		return refundRecordEntry;
	}

	/**
	 * @param snapshot
	 * @param refundEntry
	 * @throws OrderReturnRecordsHandlerException
	 */
	protected OrderEntryModel getOriginalOrderEntry(final OrderHistoryEntryModel snapshot, final RefundEntryModel refundEntry)
			throws OrderReturnRecordsHandlerException
	{
		try
		{
			final int entryPos = refundEntry.getOrderEntry().getEntryNumber().intValue();
			return (OrderEntryModel) snapshot.getPreviousOrderVersion().getEntries().get(entryPos);
		}
		catch (final IndexOutOfBoundsException e)
		{
			throw new IllegalStateException("Cloned and original order have different number of entries", e);
		}
		catch (final Exception e)
		{
			throw new OrderReturnRecordsHandlerException(refundEntry.getOrderEntry().getOrder().getCode(),
					"Error during getting historical orderEntry", e);
		}
	}

	protected String generateEntryCode(final OrderHistoryEntryModel snapshot)
	{
		return snapshot.getOrder().getCode() + "_v" + snapshot.getPreviousOrderVersion().getVersionID() + "_c";
	}

	/**
	 * @param order
	 * @throws OrderCancelDaoException
	 */
	protected OrderReturnRecordModel getOrCreateReturnRecord(final OrderModel order)
	{
		OrderReturnRecordModel returnRecord = orderReturnDao.getOrderReturnRecord(order);
		if (returnRecord == null)
		{
			returnRecord = createReturnRecord(order);
		}
		return returnRecord;
	}

	/**
	 * @param order
	 */
	protected OrderReturnRecordModel createReturnRecord(final OrderModel order)
	{
		OrderReturnRecordModel returnRecord;
		returnRecord = getModelService().create(OrderReturnRecordModel.class);
		returnRecord.setOrder(order);
		returnRecord.setOwner(order);
		returnRecord.setInProgress(false);
		getModelService().save(returnRecord);
		return returnRecord;
	}

	protected OrderHistoryEntryModel createSnaphot(final OrderModel order, final String description)
	{
		final OrderModel version = getOrderHistoryService().createHistorySnapshot(order);
		getOrderHistoryService().saveHistorySnapshot(version);

		final OrderHistoryEntryModel historyEntry = getModelService().create(OrderHistoryEntryModel.class);
		historyEntry.setOrder(order);
		historyEntry.setPreviousOrderVersion(version);
		historyEntry.setDescription(description);
		historyEntry.setTimestamp(new Date());
		getModelService().save(historyEntry);
		return historyEntry;
	}

	@Override
	public OrderReturnRecordModel getReturnRecord(final OrderModel order)
	{
		return orderReturnDao.getOrderReturnRecord(order);
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

	protected OrderReturnDao getOrderReturnDao()
	{
		return orderReturnDao;
	}

	@Required
	public void setOrderReturnDao(final OrderReturnDao orderReturnDao)
	{
		this.orderReturnDao = orderReturnDao;
	}

	protected OrderHistoryService getOrderHistoryService()
	{
		return orderHistoryService;
	}

	@Required
	public void setOrderHistoryService(final OrderHistoryService orderHistoryService)
	{
		this.orderHistoryService = orderHistoryService;
	}

}
