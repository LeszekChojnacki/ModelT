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
 * Base class for charges which applies to {@link LineItem}s or to the {@link Order}.
 */
public abstract class AbstractCharge
{
	private ChargeType chargeType;
	private boolean disabled;
	private final AbstractAmount amount;

	/**
	 * The kind of charge.
	 */
	public enum ChargeType
	{
		PAYMENT, SHIPPING;
	}

	/**
	 * Default constructor requiring a spcific amount.
	 */
	public AbstractCharge(final AbstractAmount amount)
	{
		if (amount == null)
		{
			throw new IllegalArgumentException("The amount was null");
		}
		this.amount = amount;
	}

	/**
	 * @return true if this charge will be ignored for calculation.
	 */
	public boolean isDisabled()
	{
		return disabled;
	}

	/**
	 * Marks this charge as disabled, which means that it will be ignored during calculation.
	 */
	public void setDisabled(final boolean disabled)
	{
		this.disabled = disabled;
	}

	/**
	 * @return the amount (in {@link Percentage} or {@link Money} of this additional charge.
	 */
	public AbstractAmount getAmount()
	{
		return amount;
	}

	/**
	 * Sets the kind of this additional charge. Can be null.
	 * 
	 */
	public void setChargeType(final ChargeType chargeType)
	{
		this.chargeType = chargeType;
	}

	/**
	 * Returns the kind of this additional charge.
	 * 
	 * @return null if nothing is set.
	 */
	public ChargeType getChargeType()
	{
		return chargeType;
	}

}
