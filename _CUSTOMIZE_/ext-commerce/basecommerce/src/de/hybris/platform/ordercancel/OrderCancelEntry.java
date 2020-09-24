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


/**
 * Represents a cancel entry in an Order Cancel Request or Order Cancel Response. A single cancel entry refers to an
 * OrderEntry and has a cancelQuantity value. cancelQuantity value must be greater than zero and less than or equal to
 * OrderEntry.getQuantity(). if cancelQuantity value is equal to the OrderEntry quantity, the OrderEntry is subject to
 * complete cancel operation, , otherwise it is subject to partial cancel.
 * 
 * In case of Order Cancel Request the cancelQuantity value means how many items should be canceled from the actual
 * OrderEntry. In case of Order Cancel Response the cancelQuantity value means how many items were successfully canceled
 * from the actual OrderEntry.
 */
public class OrderCancelEntry
{
	private final AbstractOrderEntryModel orderEntry;
	private final long cancelQuantity;
	private String notes;
	private CancelReason cancelReason;

	/**
	 * Creates an entry that represents cancellation of the whole Order Entry
	 * 
	 * @param orderEntry
	 */
	public OrderCancelEntry(final AbstractOrderEntryModel orderEntry)
	{
		this.orderEntry = orderEntry;
		this.cancelQuantity = orderEntry.getQuantity().longValue();
	}

	/**
	 * Creates an entry that represents cancellation of a part of the Order Entry (i.e. reducing Order Entry quantity).
	 * Reducing Order Entry quantity to zero is the same as canceling it completely.
	 * 
	 * @param orderEntry
	 */
	public OrderCancelEntry(final AbstractOrderEntryModel orderEntry, final long cancelQuantity)
	{
		this(orderEntry, cancelQuantity, null);
	}

	/**
	 * Creates an entry that represents cancellation of a part of the Order Entry (i.e. reducing Order Entry quantity).
	 * Reducing Order Entry quantity to zero is the same as canceling it completely.
	 * 
	 * @param orderEntry
	 *           - order entry
	 * @param cancelQuantity
	 *           - how much of the entry's quantity should be cancelled
	 * @param notes
	 *           - additional notes (I.E from CSAdmin)
	 */
	public OrderCancelEntry(final AbstractOrderEntryModel orderEntry, final long cancelQuantity, final String notes)
	{
		this(orderEntry, cancelQuantity, notes, CancelReason.NA);
	}

	/**
	 * Creates an entry that represents cancellation of a part of the Order Entry completely.
	 * 
	 * @param orderEntry
	 *           - order entry
	 * @param cancelReason
	 *           - reason of this order entry cancellation
	 * @param notes
	 *           - additional notes (I.E from CSAdmin)
	 */
	public OrderCancelEntry(final AbstractOrderEntryModel orderEntry, final String notes, final CancelReason cancelReason)
	{
		this(orderEntry, orderEntry.getQuantity().longValue(), notes, cancelReason);
	}

	public OrderCancelEntry(final AbstractOrderEntryModel orderEntry, final long cancelQuantity, final String notes,
			final CancelReason cancelReason)
	{
		this.orderEntry = orderEntry;
		if (cancelQuantity < 0)
		{
			throw new IllegalArgumentException("OrderCancelEntry's cancelQuantity value must be greater than zero");
		}
		if (cancelQuantity > orderEntry.getQuantity().longValue())
		{
			throw new IllegalArgumentException(
					"OrderCancelEntry's cancelQuantity value cannot be greater than actual OrderEntry quantity");
		}
		this.cancelQuantity = cancelQuantity;
		this.notes = notes;
		this.cancelReason = cancelReason;
	}



	/**
	 * @return the orderEntry
	 */
	public AbstractOrderEntryModel getOrderEntry()
	{
		return orderEntry;
	}

	/**
	 * @return the cancelQuantity
	 */
	public long getCancelQuantity()
	{
		return cancelQuantity;
	}

	/**
	 * @return the notes
	 */
	public String getNotes()
	{
		return notes;
	}

	/**
	 * @return the cancelReason
	 */
	public CancelReason getCancelReason()
	{
		return cancelReason;
	}

	/**
	 * @param cancelReason
	 *           the cancelReason to set
	 */
	public void setCancelReason(final CancelReason cancelReason)
	{
		this.cancelReason = cancelReason;
	}
}
