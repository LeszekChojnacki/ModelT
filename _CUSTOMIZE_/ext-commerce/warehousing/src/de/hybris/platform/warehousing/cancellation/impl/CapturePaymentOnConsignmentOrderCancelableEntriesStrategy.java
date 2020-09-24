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

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelCancelableEntriesStrategy;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * E2e implementation for {@link OrderCancelCancelableEntriesStrategy}. Not cancellable quantities of Order entries
 * (i.e. single items that cannot be cancelled from order entry) are evaluated by summarizing all unallocated items and
 * items which are in cancelable consignments
 */
public class CapturePaymentOnConsignmentOrderCancelableEntriesStrategy implements OrderCancelCancelableEntriesStrategy
{
	private Collection<ConsignmentStatus> notCancelableConsignmentStatusList;

	@Override
	public Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(final OrderModel order, final PrincipalModel requestor)
	{
		return order.getEntries().stream().filter(entry -> calculateCancelableQtyForOrderEntry((OrderEntryModel) entry) > 0)
				.collect(Collectors.toMap(entry -> entry, entry -> calculateCancelableQtyForOrderEntry((OrderEntryModel) entry)));
	}

	/**
	 * Calculates cancelable quantity for an {@link OrderEntryModel}. Takes sum of unallocated items and all items which are in
	 * cancelable consignments
	 *
	 * @param orderEntryModel
	 * 		{@link OrderEntryModel} for calculating cancelable qty
	 * @return cancelable quantity for the given {@link OrderEntryModel}
	 */
	protected long calculateCancelableQtyForOrderEntry(final OrderEntryModel orderEntryModel)
	{
		final long cancelableConsignmentQty = orderEntryModel.getOrder().getConsignments().stream()
				.filter(consignmentModel -> !getNotCancelableConsignmentStatusList().contains(consignmentModel.getStatus()))
				.flatMap(consignmentModel -> consignmentModel.getConsignmentEntries().stream()).filter(
						consignmentEntryModel -> consignmentEntryModel.getOrderEntry().getEntryNumber()
								.equals(orderEntryModel.getEntryNumber())).mapToLong(ConsignmentEntryModel::getQuantity).sum();
		return orderEntryModel.getQuantityUnallocated() + cancelableConsignmentQty;
	}

	protected Collection<ConsignmentStatus> getNotCancelableConsignmentStatusList()
	{
		return notCancelableConsignmentStatusList;
	}

	@Required
	public void setNotCancelableConsignmentStatusList(final Collection<ConsignmentStatus> notCancelableConsignmentStatusList)
	{
		this.notCancelableConsignmentStatusList = notCancelableConsignmentStatusList;
	}
}
