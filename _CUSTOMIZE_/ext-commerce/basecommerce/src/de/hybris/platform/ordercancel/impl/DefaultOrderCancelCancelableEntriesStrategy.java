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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelCancelableEntriesStrategy;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Default implementation for {@link OrderCancelCancelableEntriesStrategy}. Not cancelable quantities of Order entries
 * (i.e. single items that cannot be canceled from order entry) are:
 * <ul>
 * <li>Those items that are part of consignments with status: CANCELLED or SHIPPED
 * <li>Those items that are part of consignments entries with shippedQuantity > 0 - then the "shippedQuantity" number of
 * items is not cancelable.</li>
 * </ul>
 */
public class DefaultOrderCancelCancelableEntriesStrategy implements OrderCancelCancelableEntriesStrategy
{

	private static final Logger LOG = Logger.getLogger(DefaultOrderCancelCancelableEntriesStrategy.class.getName());

	@Override
	public Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(final OrderModel order, final PrincipalModel requestor)
	{
		final Map<AbstractOrderEntryModel, Long> uncancelableEntriesMap = collectUncancelableEntriesMap(order);
		return findCancellableEntries(order, uncancelableEntriesMap);
	}

	protected Map<AbstractOrderEntryModel, Long> findCancellableEntries(final OrderModel order,
			final Map<AbstractOrderEntryModel, Long> uncancelableEntriesMap)
	{
		final Map<AbstractOrderEntryModel, Long> cancelableEntries = new HashMap<AbstractOrderEntryModel, Long>();
		for (final AbstractOrderEntryModel entry : order.getEntries())
		{
			final long totalEntryQuantity = entry.getQuantity().longValue();
			final long uncancelableEntryQuantity;
			if (uncancelableEntriesMap.containsKey(entry))
			{
				uncancelableEntryQuantity = uncancelableEntriesMap.get(entry).longValue();
			}
			else
			{
				uncancelableEntryQuantity = 0;
			}

			final long cancelableQuantity = totalEntryQuantity - uncancelableEntryQuantity;
			if (cancelableQuantity > 0)
			{
				cancelableEntries.put(entry, Long.valueOf(cancelableQuantity));
			}
			else if (cancelableQuantity < 0)
			{
				LOG.error("Error while computing cancelableQuantity of order entry: result value < 0");
			}
		}

		return cancelableEntries;
	}

	/*
	 * Prepare map of "uncancelable" entries. It will contain an OrderEntry and a value that indicates a number of items
	 * that CANNOT be canceled from this OrderEntry. This value should always be in range 0..OrderEntry.getQuantity(). If
	 * it is 0 then all items from an OrderEntry can be canceled, if it is equal to OrderEntry.getQuantity() then none of
	 * items can be canceled.
	 */
	protected Map<AbstractOrderEntryModel, Long> collectUncancelableEntriesMap(final OrderModel order)
	{
		final Map<AbstractOrderEntryModel, Long> uncancelableEntriesMap = new HashMap<AbstractOrderEntryModel, Long>();
		for (final ConsignmentModel cm : order.getConsignments())
		{
			appendUncancelableEntriesMap(uncancelableEntriesMap, cm);
		}
		return uncancelableEntriesMap;
	}

	protected void appendUncancelableEntriesMap(final Map<AbstractOrderEntryModel, Long> uncancelableEntriesMap,
			final ConsignmentModel consignment)
	{
		final boolean consignmentUnavailableForCancel = ConsignmentStatus.SHIPPED.equals(consignment.getStatus())
				|| ConsignmentStatus.CANCELLED.equals(consignment.getStatus());

		for (final ConsignmentEntryModel cem : consignment.getConsignmentEntries())
		{
			appendUncancelableEntriesMap(uncancelableEntriesMap, consignmentUnavailableForCancel, cem);
		}
	}

	protected void appendUncancelableEntriesMap(final Map<AbstractOrderEntryModel, Long> uncancelableEntriesMap,
			final boolean consignmentUnavailableForCancel, final ConsignmentEntryModel consignmentEntry)
	{
		if (consignmentUnavailableForCancel)
		{
			//This consignment entry is unavailable for cancel because of the status of the consignment to which the entry belongs!
			mergeEntries(uncancelableEntriesMap, consignmentEntry.getOrderEntry(), consignmentEntry.getQuantity());
		}
		else
		{
			if (consignmentEntry.getShippedQuantity() != null && consignmentEntry.getShippedQuantity().longValue() > 0)
			{
				//This consignment entry indicates that some items has been shipped, we mark these items as unavailable for cancel.
				mergeEntries(uncancelableEntriesMap, consignmentEntry.getOrderEntry(), consignmentEntry.getShippedQuantity());
			}
		}
	}

	protected void mergeEntries(final Map<AbstractOrderEntryModel, Long> unavailableEntries, final AbstractOrderEntryModel entry,
			final Long unavailableQuantity)
	{
		if (unavailableQuantity == null)
		{
			return;
		}

		final long newUnavailableQuantity;
		if (unavailableEntries.containsKey(entry))
		{
			newUnavailableQuantity = unavailableEntries.get(entry).longValue() + unavailableQuantity.longValue();
		}
		else
		{
			newUnavailableQuantity = unavailableQuantity.longValue();
		}

		unavailableEntries.put(entry, Long.valueOf(newUnavailableQuantity));
	}
}
