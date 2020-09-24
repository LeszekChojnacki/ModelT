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

import de.hybris.platform.jalo.c2l.Currency;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.util.localization.Localization;

import java.util.Iterator;


/**
 * This restriction restricts vouchers to customers who ordered a total of X EUR or greater in their lifetime.
 *
 */
public class RegularCustomerOrderTotalRestriction extends GeneratedRegularCustomerOrderTotalRestriction //NOSONAR
{
	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()),
				getAllOrdersTotal().toString() + getCurrency().getIsoCode() };  //NOSONAR
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns
	 * <tt>true</tt> if the user of the specified abstract order has (not) placed orders amounting to a total defined by
	 * this restriction before.
	 *
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		double currentTotal = 0.0;
		final Currency currency = getCurrency();
		for (final Iterator iterator = anOrder.getUser().getOrders().iterator(); iterator.hasNext();)
		{
			final Order order = (Order) iterator.next();
			if (!order.equals(anOrder))
			{
				currentTotal += order.getCurrency().convert(currency, order.getSubtotal().doubleValue());   //NOSONAR

				// we have to add the taxes ?
				if (isNetAsPrimitive() != order.isNet().booleanValue())
				{
					if (order.isNet().booleanValue()) //NOSONAR
					{
						currentTotal += order.getTotalTax().doubleValue();
					}
					else
					{
						currentTotal -= order.getTotalTax().doubleValue();
					}
				}

				//we have to add the shipping and payment costs ?
				if (!isValueofgoodsonlyAsPrimitive())
				{
					currentTotal += order.getDeliveryCosts();
					currentTotal += order.getPaymentCosts();
				}
			}
		}
		return currentTotal >= getAllOrdersTotalAsPrimitive();
	}

	/**
	 * Returns <tt>true</tt> if the specified product fulfills this restriction.
	 *
	 * @param aProduct
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(Product)
	 */
	@Override
	protected boolean isFulfilledInternal(final Product aProduct)   //NOSONAR
	{
		return true;
	}
}
