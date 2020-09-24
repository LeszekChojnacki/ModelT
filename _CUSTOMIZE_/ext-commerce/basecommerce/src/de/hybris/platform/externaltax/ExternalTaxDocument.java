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
package de.hybris.platform.externaltax;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.util.TaxValue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents external taxes which can be applied to a {@link AbstractOrderModel}.
 */
public class ExternalTaxDocument implements Serializable
{
	private Map<Integer, List<TaxValue>> lineItemTaxes;

	private List<TaxValue> shippingCostTaxes;

	public Map<Integer, List<TaxValue>> getAllTaxes()
	{
		return getTaxesMap(false);
	}

	protected Map<Integer, List<TaxValue>> getTaxesMap(final boolean createIfAbsent)
	{
		if (lineItemTaxes == null && createIfAbsent)
		{
			lineItemTaxes = new HashMap<Integer, List<TaxValue>>();
		}
		return lineItemTaxes == null ? Collections.emptyMap() : lineItemTaxes;
	}

	public List<TaxValue> getTaxesForOrderEntry(final int entryNumber)
	{
		final List<TaxValue> ret = getTaxesMap(false).get(Integer.valueOf(entryNumber));
		return ret == null ? Collections.emptyList() : ret;
	}

	public void setTaxesForOrderEntry(final int entryNumber, final List<TaxValue> taxes)
	{
		if (taxes == null)
		{
			getTaxesMap(true).remove(Integer.valueOf(entryNumber));
		}
		else
		{
			getTaxesMap(true).put(Integer.valueOf(entryNumber), taxes);
		}
	}

	public void setTaxesForOrderEntry(final int entryNumber, final TaxValue... taxes)
	{
		setTaxesForOrderEntry(entryNumber, taxes == null ? null : Arrays.asList(taxes));
	}

	public List<TaxValue> getShippingCostTaxes()
	{
		return shippingCostTaxes == null ? Collections.emptyList() : shippingCostTaxes;
	}

	public void setShippingCostTaxes(final List<TaxValue> shippingCostTaxes)
	{
		this.shippingCostTaxes = shippingCostTaxes;
	}

	public void setShippingCostTaxes(final TaxValue... shippingCostTaxes)
	{
		setShippingCostTaxes(shippingCostTaxes == null ? null : Arrays.asList(shippingCostTaxes));
	}
}
