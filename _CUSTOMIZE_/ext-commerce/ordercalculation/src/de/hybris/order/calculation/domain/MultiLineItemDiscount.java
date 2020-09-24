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
package de.hybris.order.calculation.domain;

import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class MultiLineItemDiscount extends AbstractDiscount
{
	private static final MultiLineDiscountSplitStrategy DEFAULT = new EvenMultiLineDiscountSplitStrategy();


	private List<LineItem> lineitems;
	private List<Percentage> percentages = new ArrayList<>();
	private final MultiLineDiscountSplitStrategy mldss;


	public MultiLineItemDiscount(final Money amount)
	{
		this(amount, DEFAULT);
	}

	public MultiLineItemDiscount(final Money amount, final MultiLineDiscountSplitStrategy mldss)
	{
		super(amount);
		this.mldss = mldss == null ? DEFAULT : mldss;
	}


	public interface MultiLineDiscountSplitStrategy
	{
		List<Percentage> computeSplitRatio(List<LineItem> lineItems, List<Percentage> percentages);
	}

	public static class EvenMultiLineDiscountSplitStrategy implements MultiLineDiscountSplitStrategy
	{
		@Override
		public List<Percentage> computeSplitRatio(final List<LineItem> lineItems, final List<Percentage> percentages)
		{
			final List<Percentage> result = new ArrayList<>();
			if (percentages != null && !percentages.isEmpty())
			{
				if (percentages.size() > lineItems.size())
				{
					throw new IllegalArgumentException("Got more percentages as line items");
				}
				result.addAll(percentages);
			}

			if (result.size() < lineItems.size())
			{
				final Percentage remainder = Percentage.HUNDRED.subtract(Percentage.sum(result));
				result.addAll(remainder.split(lineItems.size() - result.size()));
			}
			return result;
		}
	}

	public Map<LineItem, Money> getDiscountValues()
	{
		final List<LineItem> lineItems2 = getLineItems();
		final List<Money> split = ((Money) getAmount()).split(this.mldss.computeSplitRatio(lineItems2, percentages));
		final Map<LineItem, Money> retmap = new LinkedHashMap<>(lineItems2.size());

		for (int index = 0; index < lineItems2.size(); index++)
		{
			retmap.put(lineItems2.get(index), split.get(index));
		}
		return retmap;
	}

	public void setLineitems(final List<LineItem> lineitems)
	{
		this.lineitems = lineitems;
	}

	public List<LineItem> getLineItems()
	{
		return Collections.unmodifiableList(lineitems);
	}

}
