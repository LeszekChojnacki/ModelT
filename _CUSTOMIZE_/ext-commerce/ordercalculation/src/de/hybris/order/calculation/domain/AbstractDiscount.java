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

import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;


/**
 * The abstract class for all discounts which applies to {@link LineItem} or to an {@link Order}.
 */
public abstract class AbstractDiscount
{
	private final AbstractAmount amount;

	/**
	 * Constructor that requires to provide a amount which cannot be changed later on.
	 */
	public AbstractDiscount(final AbstractAmount amount)
	{
		if (amount == null)
		{
			throw new IllegalArgumentException("The amount was null");
		}
		this.amount = amount;
	}

	/**
	 * Returns the amount of discount. This may be either {@link Percentage} or {@link Money}.
	 */
	public AbstractAmount getAmount()
	{
		return amount;
	}
}
