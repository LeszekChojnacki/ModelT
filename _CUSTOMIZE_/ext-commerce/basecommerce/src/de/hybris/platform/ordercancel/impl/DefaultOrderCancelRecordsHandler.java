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

import de.hybris.platform.basecommerce.enums.OrderCancelEntryStatus;
import de.hybris.platform.basecommerce.enums.OrderModificationEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelRecordsHandler;
import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelResponse;
import de.hybris.platform.ordercancel.dao.OrderCancelDao;
import de.hybris.platform.ordercancel.exceptions.OrderCancelDaoException;
import de.hybris.platform.ordercancel.exceptions.OrderCancelRecordsHandlerException;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.orderhistory.OrderHistoryService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.ordermodify.model.OrderEntryModificationRecordEntryModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordEntryModel;
import de.hybris.platform.ordermodify.model.OrderModificationRecordModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * The default implementation for the OrderCancelRecordsHandler
 */
public class DefaultOrderCancelRecordsHandler implements OrderCancelRecordsHandler
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DefaultOrderCancelRecordsHandler.class);

	private OrderCancelDao orderCancelDao;
	private OrderHistoryService orderHistoryService;
	private UserService userService;
	private ModelService modelService;

	@Override
	public OrderCancelRecordEntryModel createRecordEntry(final OrderCancelRequest request)
			throws OrderCancelRecordsHandlerException
	{

		return createRecordEntry(request, getUserService().getCurrentUser());
	}

	@Override
	public OrderCancelRecordEntryModel createRecordEntry(final OrderCancelRequest request, final PrincipalModel requestor)
			throws OrderCancelRecordsHandlerException
	{
		if (request == null)
		{
			throw new OrderCancelRecordsHandlerException(null, "Cancel request cannot be null");
		}
		if (request.getOrder() == null)
		{
			throw new OrderCancelRecordsHandlerException(null, "Cancel request contains no order reference");
		}


		final OrderModel order = request.getOrder();

		//required because of PLA-8722 workaround (order entries number normalization)
		final OrderModel version = orderHistoryService.createHistorySnapshot(order);
		final Map<Integer, AbstractOrderEntryModel> originalOrderEntriesMapping = storeOriginalOrderEntriesMapping(version);

		final String description = (!request.isPartialCancel() ? "Full c" : "Partial c") + "ancel request for order: "
				+ order.getCode();
		final OrderHistoryEntryModel snapshot = createSnaphot(order, version, description, requestor);

		try
		{
			final OrderCancelRecordModel cancelRecord = getOrCreateCancelRecord(order);
			if (cancelRecord.isInProgress())
			{
				throw new IllegalStateException(
						"Cannot create new Orde cancel request - the order cancel record indicates: Cancel already in progress");
			}
			cancelRecord.setInProgress(true);
			getModelService().save(cancelRecord);
			return createCancelRecordEntry(request, order, cancelRecord, snapshot, originalOrderEntriesMapping);
		}
		catch (final OrderCancelDaoException e)
		{
			throw new OrderCancelRecordsHandlerException(order.getCode(), e);
		}
	}

	/**
	 * @param request
	 * @param order
	 * @param cancelRecord
	 * @return {@link OrderCancelRecordEntryModel}
	 * @throws OrderCancelRecordsHandlerException
	 */
	@SuppressWarnings("squid:S1172")
	protected OrderCancelRecordEntryModel createCancelRecordEntry(final OrderCancelRequest request, final OrderModel order,
			final OrderCancelRecordModel cancelRecord, final OrderHistoryEntryModel snapshot,
			final Map<Integer, AbstractOrderEntryModel> originalOrderEntriesMapping) throws OrderCancelRecordsHandlerException
	{

		final OrderCancelRecordEntryModel cancelRecordEntry = getModelService().create(OrderCancelRecordEntryModel.class);
		cancelRecordEntry.setTimestamp(new Date());
		cancelRecordEntry.setCode(generateEntryCode(snapshot));
		cancelRecordEntry.setOriginalVersion(snapshot);
		cancelRecordEntry.setModificationRecord(cancelRecord);
		cancelRecordEntry.setPrincipal(getUserService().getCurrentUser());
		cancelRecordEntry.setOwner(cancelRecord);
		cancelRecordEntry.setStatus(OrderModificationEntryStatus.INPROGRESS);
		cancelRecordEntry.setCancelResult(request.isPartialCancel() ? OrderCancelEntryStatus.PARTIAL : OrderCancelEntryStatus.FULL);
		cancelRecordEntry.setCancelReason(request.getCancelReason());
		cancelRecordEntry.setNotes(request.getNotes());
		getModelService().save(cancelRecordEntry);

		final List<OrderEntryModificationRecordEntryModel> orderEntriesRecords = new ArrayList<OrderEntryModificationRecordEntryModel>();
		for (final OrderCancelEntry cancelRequestEntry : request.getEntriesToCancel())
		{
			final OrderEntryCancelRecordEntryModel orderEntryRecordEntry = getModelService()
					.create(OrderEntryCancelRecordEntryModel.class);
			orderEntryRecordEntry.setCode(cancelRequestEntry.getOrderEntry().getPk().toString());
			orderEntryRecordEntry.setCancelRequestQuantity(Integer.valueOf((int) cancelRequestEntry.getCancelQuantity()));
			orderEntryRecordEntry.setModificationRecordEntry(cancelRecordEntry);
			orderEntryRecordEntry.setOrderEntry((OrderEntryModel) cancelRequestEntry.getOrderEntry());
			orderEntryRecordEntry.setOriginalOrderEntry(getOriginalOrderEntry(originalOrderEntriesMapping, cancelRequestEntry));
			orderEntryRecordEntry.setNotes(cancelRequestEntry.getNotes());
			orderEntryRecordEntry.setCancelReason(cancelRequestEntry.getCancelReason());
			getModelService().save(orderEntryRecordEntry);
			orderEntriesRecords.add(orderEntryRecordEntry);
		}
		cancelRecordEntry.setOrderEntriesModificationEntries(orderEntriesRecords);
		getModelService().save(cancelRecordEntry);

		return cancelRecordEntry;
	}

	/**
	 * @param originalOrderEntriesMapping
	 * @param cancelRequestEntry
	 * @throws OrderCancelRecordsHandlerException
	 */
	protected OrderEntryModel getOriginalOrderEntry(final Map<Integer, AbstractOrderEntryModel> originalOrderEntriesMapping,
			final OrderCancelEntry cancelRequestEntry) throws OrderCancelRecordsHandlerException
	{
		try
		{
			final int entryPos = cancelRequestEntry.getOrderEntry().getEntryNumber().intValue();
			return (OrderEntryModel) originalOrderEntriesMapping.get(Integer.valueOf(entryPos));
		}
		catch (final IndexOutOfBoundsException e)
		{
			throw new IllegalStateException("Cloned and original order have different number of entries", e);
		}
		catch (final Exception e)
		{
			throw new OrderCancelRecordsHandlerException(cancelRequestEntry.getOrderEntry().getOrder().getCode(),
					"Error during getting historical orderEntry", e);
		}
	}

	@Override
	public OrderCancelRecordEntryModel updateRecordEntry(final OrderCancelResponse response)
			throws OrderCancelRecordsHandlerException
	{
		if (response == null)
		{
			throw new IllegalArgumentException("Cancel response cannot be null");
		}
		if (response.getOrder() == null)
		{
			throw new IllegalArgumentException("Cancel response contains no order reference");
		}
		final OrderModel order = response.getOrder();

		final OrderCancelRecordEntryModel currentEntry = getPendingCancelRecordEntry(order);
		switch (response.getResponseStatus())
		{
			case full:
				currentEntry.setCancelResult(OrderCancelEntryStatus.FULL);
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case partial:
				currentEntry.setCancelResult(OrderCancelEntryStatus.PARTIAL);
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case denied:
				currentEntry.setCancelResult(OrderCancelEntryStatus.DENIED);
				currentEntry.setRefusedMessage(response.getNotes());
				currentEntry.setStatus(OrderModificationEntryStatus.SUCCESSFULL);
				break;

			case error:
				currentEntry.setStatus(OrderModificationEntryStatus.FAILED);
				currentEntry.setFailedMessage(response.getNotes());
				break;
		}

		for (final OrderCancelEntry responseEntry : response.getEntriesToCancel())
		{
			final OrderEntryCancelRecordEntryModel orderEntryRecord = getOrderCancelDao()
					.getOrderEntryCancelRecord((OrderEntryModel) responseEntry.getOrderEntry(), currentEntry);
			orderEntryRecord.setCancelledQuantity(Integer.valueOf((int) responseEntry.getCancelQuantity()));
			getModelService().save(orderEntryRecord);
		}
		getModelService().save(currentEntry);

		final OrderModificationRecordModel record = currentEntry.getModificationRecord();
		record.setInProgress(false);
		getModelService().save(record);
		return currentEntry;
	}

	@Override
	public OrderCancelRecordEntryModel getPendingCancelRecordEntry(final OrderModel order)
			throws OrderCancelRecordsHandlerException
	{
		final OrderCancelRecordModel orderCancelRecord = getOrderCancelDao().getOrderCancelRecord(order);
		if (orderCancelRecord == null || !orderCancelRecord.isInProgress())
		{
			throw new IllegalStateException("Order[" + order.getCode() + "]: cancel is not currently in progress");
		}
		final Collection<OrderModificationRecordEntryModel> entries = orderCancelRecord.getModificationRecordEntries();
		if (entries == null || entries.isEmpty())
		{
			throw new IllegalStateException("Order[" + order.getCode() + "]: has no cancel records");
		}
		OrderCancelRecordEntryModel currentCancelEntry = null;
		for (final Iterator<OrderModificationRecordEntryModel> iter = entries.iterator(); iter.hasNext();)
		{
			final OrderCancelRecordEntryModel entry = (OrderCancelRecordEntryModel) iter.next();
			if (entry.getStatus().equals(OrderModificationEntryStatus.INPROGRESS))
			{
				if (currentCancelEntry == null)
				{
					currentCancelEntry = entry;
				}
				else
				{
					throw new IllegalStateException(
							"Order[" + order.getCode() + "]: cancel record has more than one entries with status 'IN PROGRESS'");
				}
			}
		}
		return currentCancelEntry;
	}

	@Override
	public OrderCancelRecordModel getCancelRecord(final OrderModel order)
	{
		return getOrderCancelDao().getOrderCancelRecord(order);
	}

	/**
	 * @param order
	 * @throws OrderCancelDaoException
	 */
	protected OrderCancelRecordModel getOrCreateCancelRecord(final OrderModel order)
	{
		OrderCancelRecordModel cancelRecord = getOrderCancelDao().getOrderCancelRecord(order);
		if (cancelRecord == null)
		{
			cancelRecord = createCancelRecord(order);
		}
		return cancelRecord;
	}

	protected Map<Integer, AbstractOrderEntryModel> storeOriginalOrderEntriesMapping(final OrderModel order)
	{
		final Map<Integer, AbstractOrderEntryModel> mapping = new HashMap<Integer, AbstractOrderEntryModel>(
				order.getEntries().size());
		for (final AbstractOrderEntryModel currentEntry : order.getEntries())
		{
			mapping.put(currentEntry.getEntryNumber(), currentEntry);
		}
		return mapping;
	}

	/**
	 * @param order
	 */
	protected OrderCancelRecordModel createCancelRecord(final OrderModel order)
	{
		OrderCancelRecordModel cancelRecord;
		cancelRecord = getModelService().create(OrderCancelRecordModel.class);
		cancelRecord.setOrder(order);
		cancelRecord.setOwner(order);
		cancelRecord.setInProgress(false);
		getModelService().save(cancelRecord);
		return cancelRecord;
	}

	protected OrderHistoryEntryModel createSnaphot(final OrderModel order, final OrderModel version, final String description)
	{
		return createSnaphot(order, version, description, null);
	}

	protected OrderHistoryEntryModel createSnaphot(final OrderModel order, final OrderModel version, final String description,
			final PrincipalModel requestor)
	{
		getOrderHistoryService().saveHistorySnapshot(version);

		final OrderHistoryEntryModel historyEntry = getModelService().create(OrderHistoryEntryModel.class);
		historyEntry.setOrder(order);
		historyEntry.setPreviousOrderVersion(version);
		historyEntry.setDescription(description);
		historyEntry.setTimestamp(new Date());
		if (requestor instanceof EmployeeModel)
		{
			historyEntry.setEmployee((EmployeeModel) requestor);
		}
		getModelService().save(historyEntry);
		return historyEntry;
	}


	protected String generateEntryCode(final OrderHistoryEntryModel snapshot)
	{
		return snapshot.getOrder().getCode() + "_v" + snapshot.getPreviousOrderVersion().getVersionID() + "_c";
	}

	protected OrderCancelDao getOrderCancelDao()
	{
		return orderCancelDao;
	}

	@Required
	public void setOrderCancelDao(final OrderCancelDao orderCancelDao)
	{
		this.orderCancelDao = orderCancelDao;
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

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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

}
