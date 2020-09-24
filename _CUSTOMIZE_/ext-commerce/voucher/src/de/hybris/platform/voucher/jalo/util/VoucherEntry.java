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
package de.hybris.platform.voucher.jalo.util;

import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.product.Unit;


/**
 * This class represents a voucher entry
 */
public class VoucherEntry
{
	// --------------------------------------------------------------- Constants
	// ------------------------------------------------------ Instance Variables
	private final AbstractOrderEntry theOrderEntry;
	private long theQuantity;
	private final Unit theUnit;

	// ------------------------------------------------------------ Constructors
	/**
	 * Creates a new instance of <code>VoucherEntry</code>.
	 */
	public VoucherEntry(final AbstractOrderEntry anOrderEntry, final long quantity, final Unit aUnit)
	{
		this.theOrderEntry = anOrderEntry;
		this.theQuantity = quantity;
		this.theUnit = aUnit;
	}

	// -------------------------------------------------------------- Properties
	@Override
	public boolean equals(final Object o) {
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final VoucherEntry entry = (VoucherEntry) o;

		return this.getOrderEntry().equals(entry.getOrderEntry())
				&& (this.getQuantity() == entry.getQuantity() && this.getUnit().equals(entry.getUnit()) || this.getQuantity() == entry
						.getUnit().convert(this.getUnit(), entry.getQuantity()));
	}

	public AbstractOrderEntry getOrderEntry()
	{
		return this.theOrderEntry;
	}

	public long getQuantity()
	{
		return this.theQuantity;
	}

	public Unit getUnit()
	{
		return this.theUnit;
	}

	@Override
	public int hashCode()
	{
		return getOrderEntry().hashCode();
	}

	public void setQuantity(final long quantity)
	{
		this.theQuantity = quantity;
	}
}
