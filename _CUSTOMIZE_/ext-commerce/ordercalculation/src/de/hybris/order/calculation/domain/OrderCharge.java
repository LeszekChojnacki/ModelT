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

import de.hybris.order.calculation.exception.MissingCalculationDataException;
import de.hybris.order.calculation.money.AbstractAmount;
import de.hybris.order.calculation.money.Money;


/**
 * Specifies order level charges.
 */
public class OrderCharge extends AbstractCharge implements Taxable
{
	/**
	 * Creates a new order level charge with a specific amount and optional charge type.
	 */
	public OrderCharge(final AbstractAmount amount, final ChargeType chargeType)
	{
		super(amount);
		super.setChargeType(chargeType);
	}

	/**
	 * Creates a new order level charge with a specific amount.
	 */
	public OrderCharge(final AbstractAmount amount)
	{
		super(amount);
	}

	/**
	 * Calculates the total for this charge within the given order context.
	 * 
	 * Actually this is just delegating to {@link Order#getTotalCharges()}.
	 */
	@Override
	public Money getTotal(final Order context)
	{
		if (context == null)
		{
			throw new MissingCalculationDataException("Missing order context");
		}
		if (!context.getCharges().contains(this))
		{
			throw new IllegalArgumentException("Charge " + this + " doesnt belong to order " + context
					+ " - cannot calculate total for it.");
		}
		return context.getTotalCharges().get(this);
	}

	@Override
	public String toString()
	{
		return getAmount().toString() + " dontCharge:" + isDisabled() + (getChargeType() == null ? "" : " type:" + getChargeType());
	}
}
