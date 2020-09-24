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
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.util.localization.Localization;

import java.util.Collection;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to new customers only
 * 
 */
public class NewCustomerRestriction extends GeneratedNewCustomerRestriction
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(NewCustomerRestriction.class.getName());

	@Override
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()) };
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns
	 * <tt>true</tt> if the user of the specified abstract order has (never) placed an order before, such that
	 * <tt>(anOrder.getUser().getOrders().isEmpty()==isPositive().booleanValue())</tt>.
	 * 
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		final Collection orders = anOrder.getUser().getOrders();
		return isPositiveAsPrimitive() == (orders.isEmpty() || (orders.size() == 1 && orders.contains(anOrder)));
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
