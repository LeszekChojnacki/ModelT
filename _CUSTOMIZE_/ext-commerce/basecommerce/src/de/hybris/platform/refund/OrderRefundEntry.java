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
package de.hybris.platform.refund;

import de.hybris.platform.basecommerce.enums.RefundReason;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;


/**
 * Represents a refund entry in an Order Return Request.
 */
public class OrderRefundEntry
{
	private final AbstractOrderEntryModel orderEntry;
	private final long expectedQuantity;
	private final String notes;
	private RefundReason reason;


	public OrderRefundEntry(final AbstractOrderEntryModel orderEntry, final long expectedQuantity)
	{
		this(orderEntry, expectedQuantity, null);
	}

	public OrderRefundEntry(final AbstractOrderEntryModel orderEntry, final long expectedQuantity, final String notes)
	{
		this(orderEntry, expectedQuantity, notes, null);
	}

	public OrderRefundEntry(final AbstractOrderEntryModel orderEntry, final long expectedQuantity, final String notes,
			final RefundReason reason)
	{
		this.orderEntry = orderEntry;
		if (expectedQuantity <= 0)
		{
			throw new IllegalArgumentException("OrderRefundEntry's expectedQuantity value must be greater than zero");
		}
		if (expectedQuantity > orderEntry.getQuantity().longValue())
		{
			throw new IllegalArgumentException(
					"OrderRefundEntry's expectedQuantity value cannot be greater than actual OrderEntry quantity");
		}
		this.expectedQuantity = expectedQuantity;
		this.notes = notes;
		this.reason = reason;
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
	public long getExpectedQuantity()
	{
		return expectedQuantity;
	}

	/**
	 * @return the notes
	 */
	public String getNotes()
	{
		return notes;
	}

	/**
	 * @return {@link RefundReason}
	 */
	public RefundReason getRefundReason()
	{
		return reason;
	}

	/**
	 * @param reason
	 *           the cancelReason to set
	 */
	public void setRefundReason(final RefundReason reason)
	{
		this.reason = reason;
	}
}
