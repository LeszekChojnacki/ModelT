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
package de.hybris.platform.ordercancel;

import de.hybris.platform.basecommerce.enums.CancelReason;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents Order Cancel requests. Instances of this class can represent:
 * <ul>
 * <li>Requests for canceling whole order (all order entries of an Order are discarded)</li>
 * <li>Requests for canceling only some of the order entries of an Order</li> An order entry may be canceled completely
 * (order entry is discarded) or partially (i.e. only order entry quantity is reduced).
 *
 * It is important to note that the requests represented by this class may be processed completely, declined or
 * processed only partially by the OrderCancelService.
 * </ul>
 */
public class OrderCancelRequest
{
	private final OrderModel order;
	private final List<OrderCancelEntry> entriesToCancel;
	private final boolean partialCancel;
	private final boolean partialEntryCancel;
	private String requestToken;
	private String notes;
	private CancelReason cancelReason = CancelReason.NA;

	/**
	 * Creates OrderCancelRequest for Order Cancel Complete
	 *
	 * @param order
	 *           an Order that should be completely canceled
	 */
	public OrderCancelRequest(final OrderModel order)
	{
		this(order, CancelReason.NA);
	}

	/**
	 * Creates {@link OrderCancelRequest} for Order Cancel Complete
	 *
	 * @param cancelReason
	 *           reason for complete cancel
	 * @param order
	 *           an Order that should be completely canceled
	 */
	public OrderCancelRequest(final OrderModel order, final CancelReason cancelReason)
	{
		this(order, cancelReason, null);
	}

	/**
	 * Creates {@link OrderCancelRequest} for Order Cancel Complete
	 *
	 * @param cancelReason
	 *           reason for complete cancel
	 * @param order
	 *           an Order that should be completely canceled
	 * @param notes
	 *           - additional notes (i.e. from CSAdmin)
	 */
	public OrderCancelRequest(final OrderModel order, final CancelReason cancelReason, final String notes)
	{
		this.order = order;
		final List<OrderCancelEntry> tmpList = new ArrayList<>();
		for (final AbstractOrderEntryModel aoem : order.getEntries())
		{
			tmpList.add(new OrderCancelEntry(aoem, aoem.getQuantity().longValue()));
		}
		Collections.sort(tmpList, (final OrderCancelEntry order1, final OrderCancelEntry order2) -> order1.getOrderEntry()
				.getEntryNumber().compareTo(order2.getOrderEntry().getEntryNumber()));
		this.entriesToCancel = Collections.unmodifiableList(tmpList);
		this.partialCancel = false;
		this.partialEntryCancel = false;
		this.cancelReason = cancelReason;
		this.notes = notes;
	}

	/**
	 * Creates OrderCancelRequest for Order Cancel Partial.
	 *
	 * @param order
	 *           an Order that should be partially canceled
	 * @param orderCancelEntries
	 *           specifies how should order entries be canceled. Each OrderCancelEntry's cancelQuantity value specifies
	 *           how many items should be canceled from the corresponding OrderEntry. If cancelQuantity value equals the
	 *           OrderEntry.getQuantity() value, the whole OrderEntry is be canceled.
	 */
	public OrderCancelRequest(final OrderModel order, final List<OrderCancelEntry> orderCancelEntries)
	{
		this(order, orderCancelEntries, null);
	}

	/**
	 * Creates OrderCancelRequest for Order Cancel Partial.
	 *
	 * @param order
	 *           an Order that should be partially canceled
	 * @param orderCancelEntries
	 *           specifies how should order entries be canceled. Each OrderCancelEntry's cancelQuantity value specifies
	 *           how many items should be canceled from the corresponding OrderEntry. If cancelQuantity value equals the
	 *           OrderEntry.getQuantity() value, the whole OrderEntry is be canceled.
	 * @param notes
	 *           - additional notes from the CSAdmin on the whole order cancellation
	 */
	public OrderCancelRequest(final OrderModel order, final List<OrderCancelEntry> orderCancelEntries, final String notes)
	{
		this.order = order;
		if (orderCancelEntries == null || orderCancelEntries.isEmpty())
		{
			throw new IllegalArgumentException("orderCancelEntries is null or empty");
		}

		//////////
		//Check entries for errors: invalid Order, adding the same entry twice. Also check for partial entry cancel condition here.
		//////////

		//This holds pairs: entryNumber(OrderEntry), OrderCancelEntry(OrderEntry)
		final Map<Integer, OrderCancelEntry> cancelEntriesMap = new HashMap<>();

		boolean partialEntryCancelDetected = false;
		for (final OrderCancelEntry oce : orderCancelEntries)
		{
			if (!order.equals(oce.getOrderEntry().getOrder()))
			{
				throw new IllegalArgumentException("Attempt to add Order Entry that belongs to another order");
			}

			if (cancelEntriesMap.containsKey(oce.getOrderEntry().getEntryNumber()))
			{
				throw new IllegalArgumentException("Attempt to add Order Entry twice");
			}
			else
			{
				cancelEntriesMap.put(oce.getOrderEntry().getEntryNumber(), oce);
			}

			if (oce.getCancelQuantity() < oce.getOrderEntry().getQuantity().longValue())
			{
				partialEntryCancelDetected = true;
			}
		}

		final List<OrderCancelEntry> tmpList = new ArrayList<>(cancelEntriesMap.values());
		Collections.sort(tmpList, (final OrderCancelEntry oce1, final OrderCancelEntry oce2) -> oce1.getOrderEntry()
				.getEntryNumber().compareTo(oce2.getOrderEntry().getEntryNumber()));

		this.entriesToCancel = Collections.unmodifiableList(tmpList);

		this.partialEntryCancel = partialEntryCancelDetected;

		//Set value for "partialCancel" flag
		if (partialEntryCancelDetected)
		{
			this.partialCancel = true;
		}
		else
		{
			//Detect if this is partial or complete cancel.
			//It is complete cancel, when all OrderEntries of an Order have corresponding OrderCancelEntry.
			boolean allOrderEntriesCancelled = true;
			for (final AbstractOrderEntryModel aoem : order.getEntries())
			{
				if (!cancelEntriesMap.containsKey(aoem.getEntryNumber()))
				{
					allOrderEntriesCancelled = false;
				}
			}

			this.partialCancel = !allOrderEntriesCancelled;
		}
		this.notes = notes;
	}

	public OrderModel getOrder()
	{
		return this.order;
	}

	public List<OrderCancelEntry> getEntriesToCancel()
	{
		return this.entriesToCancel;
	}

	/**
	 * Provides information about this request. If the return value is true, this request is a request for partial
	 * cancel. Otherwise it is a request for complete cancel.
	 *
	 * @return the isPartialCancelRepresentation
	 */
	public boolean isPartialCancel()
	{
		return partialCancel;
	}

	/**
	 * @return the partialEntryCancel
	 */
	public boolean isPartialEntryCancel()
	{
		return partialEntryCancel;
	}

	/**
	 * @return the requestToken
	 */
	public String getRequestToken()
	{
		return requestToken;
	}

	/**
	 * @param requestToken
	 *           the requestToken to set
	 */
	public void setRequestToken(final String requestToken)
	{
		this.requestToken = requestToken;
	}

	/**
	 * @return the notes
	 */
	public String getNotes()
	{
		return notes;
	}

	/**
	 * @param notes
	 *           the notes to set
	 */
	public void setNotes(final String notes)
	{
		this.notes = notes;
	}


	/**
	 * reason for complete order cancel
	 *
	 * @return the cancelReason
	 */
	public CancelReason getCancelReason()
	{
		return cancelReason;
	}

	/**
	 * reason for complete cancel
	 *
	 * @param cancelReason
	 *           the cancelReason to set
	 */
	public void setCancelReason(final CancelReason cancelReason)
	{
		this.cancelReason = cancelReason;
	}
}
