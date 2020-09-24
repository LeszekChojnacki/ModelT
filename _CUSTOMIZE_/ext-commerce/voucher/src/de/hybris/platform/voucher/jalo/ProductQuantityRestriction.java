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
package de.hybris.platform.voucher.jalo;

import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.voucher.jalo.util.VoucherEntry;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to individual product quantities.
 * <p />
 * The voucher will only applied for a product quantity of max. X items.
 *
 */
public class ProductQuantityRestriction extends GeneratedProductQuantityRestriction //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProductQuantityRestriction.class.getName());

	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + (isPositiveAsPrimitive() ? "upto" : "from")),
				getQuantity().toString() + " " + getUnit().getName(), getProductNames() };
	}

	@Override
	public VoucherEntrySet getApplicableEntries(final AbstractOrder anOrder) //NOSONAR
	{
		final VoucherEntrySet entries = new VoucherEntrySet();
		final Collection restrictedProducts = getProducts();
		final boolean isPositive = isPositiveAsPrimitive();

		if (restrictedProducts.isEmpty() && !isPositive)
		{
			entries.addAll(anOrder.getAllEntries()); //NOSONAR
		}
		else
		{
			for (final Iterator iterator = anOrder.getAllEntries().iterator(); iterator.hasNext();) //NOSONAR
			{
				final AbstractOrderEntry entry = (AbstractOrderEntry) iterator.next();
				if (restrictedProducts.contains(entry.getProduct()))
				{
					if (isPositive) //NOSONAR
					{
						entries.add(new VoucherEntry(entry, getQuantityAsPrimitive(), getUnit()));
					}
					else
					{
						final long quantity = entry.getQuantity().longValue() - getQuantityAsPrimitive();
						if (quantity > 0)
						{
							entries.add(new VoucherEntry(entry, quantity, getUnit()));
						}
					}
				}
			}
		}
		return entries;
	}
}
