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
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.util.localization.Localization;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to minimum order amount
 *
 */
public class OrderRestriction extends GeneratedOrderRestriction
{
	private static final Logger LOG = Logger.getLogger(OrderRestriction.class);

	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()),
				getTotal().toString() + getCurrency().getIsoCode() };  //NOSONAR
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns
	 * <tt>true</tt> if the total of the specified abstract order (including tax and/or delivery costs in a way defined
	 * by this restriction) is greater than the total defined by this restriction in case of this restriction is positive
	 * or less than the total defined by this restriction, else.
	 *
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		final Currency minimumOrderValueCurrency = getCurrency();
		final Currency currentOrderCurrency = anOrder.getCurrency();
		if (minimumOrderValueCurrency == null || currentOrderCurrency == null)
		{
			return false;
		}
		final double minimumTotal = minimumOrderValueCurrency.convert(currentOrderCurrency, getTotalAsPrimitive()); //NOSONAR
		try
		{
			anOrder.calculateTotals(false);  //NOSONAR
		}
		catch (final JaloPriceFactoryException e)
		{
			LOG.warn("Order.calculateTotals(false) failed", e);
		}
		double currentTotal = anOrder.getSubtotal().doubleValue();
		if (isNetAsPrimitive() != anOrder.isNet().booleanValue())
		{
			if (anOrder.isNet().booleanValue())
			{
				currentTotal += anOrder.getTotalTax().doubleValue();
			}
			else
			{
				currentTotal -= anOrder.getTotalTax().doubleValue();
			}
		}
		// we have to add the shipping and payment costs ?
		if (!isValueofgoodsonlyAsPrimitive())
		{
			currentTotal += anOrder.getDeliveryCosts();
			currentTotal += anOrder.getPaymentCosts();
		}

		if (isPositiveAsPrimitive())
		{
			return currentTotal >= minimumTotal;
		}
		else
		{
			return currentTotal <= minimumTotal;
		}
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
	protected boolean isFulfilledInternal(final Product aProduct)  //NOSONAR
	{
		return true;
	}
}
