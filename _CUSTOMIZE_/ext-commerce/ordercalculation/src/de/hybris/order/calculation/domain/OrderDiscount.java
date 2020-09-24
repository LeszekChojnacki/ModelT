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


/**
 * Holds discount that apply to a order.
 */
public class OrderDiscount extends AbstractDiscount
{
	/**
	 * Creates a new order level discount with a specific amount.
	 */
	public OrderDiscount(final AbstractAmount amount)
	{
		super(amount);
	}

	@Override
	public String toString()
	{
		return getAmount().toString();
	}
}
