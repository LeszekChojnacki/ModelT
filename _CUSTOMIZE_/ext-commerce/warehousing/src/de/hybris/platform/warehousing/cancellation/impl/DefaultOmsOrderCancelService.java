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
package de.hybris.platform.warehousing.cancellation.impl;

import de.hybris.platform.commerceservices.util.GuidKeyGenerator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.OrderCancelService;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.ordercancel.model.OrderEntryCancelRecordEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.warehousing.cancellation.CancellationException;
import de.hybris.platform.warehousing.cancellation.OmsOrderCancelService;
import de.hybris.platform.warehousing.comment.WarehousingCommentService;
import de.hybris.platform.warehousing.data.cancellation.OmsUnallocatedCancellationRemainder;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentContext;
import de.hybris.platform.warehousing.data.comment.WarehousingCommentEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Cancellation service implementation that will create cancellation events associated with the order entries.
 */
public class DefaultOmsOrderCancelService implements OmsOrderCancelService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOmsOrderCancelService.class);
	protected static final String COMMENT_SUBJECT = "Cancel order entry";
	private WarehousingCommentService orderEntryCommentService;
	private GuidKeyGenerator guidKeyGenerator;
	private ModelService modelService;
	private OrderCancelService orderCancelService;
	private UserService userService;

	@Override
	public List<OrderCancelEntry> processOrderCancel(final OrderCancelRecordEntryModel orderCancelRecordEntryModel)
			throws OrderCancelException
	{
		final OrderModel order = orderCancelRecordEntryModel.getModificationRecord().getOrder();
		LOGGER.info("Processing cancel order with code : {}", order.getCode());

		final List<OrderEntryCancelRecordEntryModel> orderEntryCancellationRecords = orderCancelRecordEntryModel
				.getOrderEntriesModificationEntries().stream().filter(entry -> entry instanceof OrderEntryCancelRecordEntryModel)
				.map(cancelEntry -> (OrderEntryCancelRecordEntryModel) cancelEntry).collect(Collectors.toList());

		checkIncomingCancellationRecordsMatchOrderEntries(order, orderEntryCancellationRecords);
		checkCancellationQuantitiesOnConsignments(order, orderEntryCancellationRecords);

		final List<OmsUnallocatedCancellationRemainder> unallocatedCancellationRemainders = cancelUnallocatedQuantities(
				orderEntryCancellationRecords);

		return extractCancellationEntriesForAllocatedQuantities(unallocatedCancellationRemainders);
	}

	/**
	 * Compares cancellation request with the order's consignment status(es) to ensure that the
	 * cancellation quantities requested are possible
	 *
	 * @param order
	 * 		the {@link OrderModel} containing the {@link OrderEntryModel}s to cancel
	 * @param orderCancelRecordEntries
	 * 		list of {@link OrderEntryCancelRecordEntryModel} containing information on which {@link OrderEntryModel}s to cancel from
	 */
	protected void checkCancellationQuantitiesOnConsignments(final OrderModel order,
			final List<OrderEntryCancelRecordEntryModel> orderCancelRecordEntries)
	{
		final Map<AbstractOrderEntryModel, Long> allCancelableEntries = getOrderCancelService()
				.getAllCancelableEntries(order, getUserService().getCurrentUser());

		if (allCancelableEntries.isEmpty() || orderCancelRecordEntries.stream().anyMatch(orderCancelRecordEntry -> (
				allCancelableEntries.get(orderCancelRecordEntry.getOrderEntry()) == null ?
						0L :
						allCancelableEntries.get(orderCancelRecordEntry.getOrderEntry())) < orderCancelRecordEntry
				.getCancelRequestQuantity()))
		{
			throw new CancellationException(
					"Requested order cancellation can not be processed because you are trying to cancel for more than the available quantity.");
		}
	}

	/**
	 * Checks that the order entry cancellation records are linked to the proper order entries.
	 *
	 * @param order
	 * 		the order containing the entries to cancel
	 * @param orderEntryCancellationRecords
	 * 		list of {@link OrderEntryCancelRecordEntryModel} containing information on which entries to cancel from
	 * 		and requested quantity to cancel
	 */
	protected void checkIncomingCancellationRecordsMatchOrderEntries(final OrderModel order,
			final List<OrderEntryCancelRecordEntryModel> orderEntryCancellationRecords)
	{
		orderEntryCancellationRecords.stream().forEach(orderEntryCancellationRecord -> order.getEntries().stream()
				.filter(entry -> entry.getEntryNumber().equals(orderEntryCancellationRecord.getOrderEntry().getEntryNumber()))
				.findFirst()
				.orElseThrow(() -> new CancellationException("Requested cancellation does not match any order entries.")));
	}

	/**
	 * Cancels unallocated quantities based on each entry's requested cancel quantity.
	 *
	 * @param orderEntryCancellationRecords
	 * 		list of {@link OrderEntryCancelRecordEntryModel} containing information on which entries to cancel from
	 * 		and requested quantity to cancel
	 */
	protected List<OmsUnallocatedCancellationRemainder> cancelUnallocatedQuantities(
			final List<OrderEntryCancelRecordEntryModel> orderEntryCancellationRecords)

	{
		final List<OmsUnallocatedCancellationRemainder> remainders = new ArrayList<>();
		orderEntryCancellationRecords.stream()
				.forEach(orderEntryCancellationRecord -> cancelUnallocatedAndAddRemainder(remainders, orderEntryCancellationRecord));

		getModelService().saveAll(orderEntryCancellationRecords);
		return remainders;
	}

	/**
	 * Cancels in a first time the unallocated quantities and if necessary creates remainders for the left quantities
	 *
	 * @param remainders
	 * 		the list of remainders
	 * @param orderEntryCancellationRecord
	 * 		the {@link OrderEntryCancelRecordEntryModel}
	 */
	protected void cancelUnallocatedAndAddRemainder(final List<OmsUnallocatedCancellationRemainder> remainders,
			final OrderEntryCancelRecordEntryModel orderEntryCancellationRecord)
	{
		if (orderEntryCancellationRecord.getCancelRequestQuantity() > orderEntryCancellationRecord.getOrderEntry().getQuantity())
		{
			throw new CancellationException("Cannot cancel more than the original order entry quantity.");
		}

		final Integer requestedCancelQuantity = Optional.ofNullable(orderEntryCancellationRecord.getCancelRequestQuantity())
				.orElse(0);
		final Integer unallocatedQuantity = orderEntryCancellationRecord.getOrderEntry().getQuantityUnallocated().intValue();
		final Integer quantityToCancel =
				requestedCancelQuantity > unallocatedQuantity ? unallocatedQuantity : requestedCancelQuantity;
		final Integer cancellationRemainder = requestedCancelQuantity - unallocatedQuantity;

		if (quantityToCancel > 0)
		{
			final Long previousOrderEntryQuantity = orderEntryCancellationRecord.getOrderEntry().getQuantity();
			final Integer previousCancelledQty = orderEntryCancellationRecord.getCancelledQuantity() != null ?
					orderEntryCancellationRecord.getCancelledQuantity() :
					Integer.valueOf(0);
			final Integer newCancelledQty = Integer.valueOf(previousCancelledQty.intValue() + quantityToCancel.intValue());
			orderEntryCancellationRecord.setCancelledQuantity(newCancelledQty);
			orderEntryCancellationRecord.getOrderEntry().setQuantity(previousOrderEntryQuantity - quantityToCancel);

			if (!Objects.isNull(orderEntryCancellationRecord.getNotes()))
			{
				final WarehousingCommentContext commentContext = new WarehousingCommentContext();
				commentContext.setCommentType(WarehousingCommentEventType.CANCEL_ORDER_COMMENT);
				commentContext.setItem(orderEntryCancellationRecord.getOrderEntry());
				commentContext.setSubject(COMMENT_SUBJECT);
				commentContext.setText(orderEntryCancellationRecord.getNotes());

				final String code = "cancellation_" + getGuidKeyGenerator().generate().toString();
				getOrderEntryCommentService().createAndSaveComment(commentContext, code);
			}
		}

		if (cancellationRemainder > 0)
		{
			final OmsUnallocatedCancellationRemainder unallocatedCancellationRemainder = new OmsUnallocatedCancellationRemainder();
			unallocatedCancellationRemainder.setOrderEntryCancellationRecord(orderEntryCancellationRecord);
			unallocatedCancellationRemainder.setRemainingQuantity(cancellationRemainder);
			remainders.add(unallocatedCancellationRemainder);
		}
	}

	/**
	 * Extracts a list of {@link OrderCancelEntry} corresponding to the requested quantity to cancel.
	 *
	 * @param unallocatedCancellationRemainders
	 * 		list of {@link OrderEntryCancelRecordEntryModel} containing information on which entries to cancel from
	 * 		and requested quantity to cancel
	 * @return list of {@link OrderCancelEntry}
	 */
	protected List<OrderCancelEntry> extractCancellationEntriesForAllocatedQuantities(
			final List<OmsUnallocatedCancellationRemainder> unallocatedCancellationRemainders)
	{
		final List<OrderCancelEntry> orderCancelEntries = new ArrayList<>();
		for (final OmsUnallocatedCancellationRemainder unallocatedCancellationRemainder : unallocatedCancellationRemainders)
		{
			final OrderCancelEntry newCancellationEntry = new OrderCancelEntry(
					unallocatedCancellationRemainder.getOrderEntryCancellationRecord().getOrderEntry(),
					unallocatedCancellationRemainder.getRemainingQuantity().longValue(),
					unallocatedCancellationRemainder.getOrderEntryCancellationRecord().getNotes(),
					unallocatedCancellationRemainder.getOrderEntryCancellationRecord().getCancelReason());
			orderCancelEntries.add(newCancellationEntry);
		}
		return orderCancelEntries;
	}

	protected WarehousingCommentService getOrderEntryCommentService()
	{
		return orderEntryCommentService;
	}

	@Required
	public void setOrderEntryCommentService(final WarehousingCommentService orderEntryCommentService)
	{
		this.orderEntryCommentService = orderEntryCommentService;
	}

	protected GuidKeyGenerator getGuidKeyGenerator()
	{
		return guidKeyGenerator;
	}

	@Required
	public void setGuidKeyGenerator(final GuidKeyGenerator guidKeyGenerator)
	{
		this.guidKeyGenerator = guidKeyGenerator;
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

	protected OrderCancelService getOrderCancelService()
	{
		return orderCancelService;
	}

	@Required
	public void setOrderCancelService(final OrderCancelService orderCancelService)
	{
		this.orderCancelService = orderCancelService;
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
}
