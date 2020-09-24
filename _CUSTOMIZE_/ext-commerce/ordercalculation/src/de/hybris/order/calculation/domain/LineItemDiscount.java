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
 * Holds a discounts specific for a {@link LineItem}. The discount holds a amount (can be {@link Money} or
 * {@link Percentage}) which can only be set during creation (see {@link #getAmount()}).
 * 
 * Further it either applies per unit or per line (see {@link #isPerUnit()}). In case it applies per unit it may be
 * limited to just a certain amount of units ( see {@link #getApplicableUnits()} ).
 */
public class LineItemDiscount extends AbstractDiscount
{
	private final boolean perUnit;
	private int applicableUnits;

	/**
	 * Creates a new line item discount with the given amount. This discount applies per line.
	 * 
	 * @see #LineItemDiscount(AbstractAmount, boolean, int)
	 */
	public LineItemDiscount(final AbstractAmount amount)
	{
		this(amount, false, 0);
	}

	/**
	 * Creates a new line item discount with given amount. Depending on the given parameters it either applies per unit
	 * or per line.
	 * 
	 * @param amount
	 *           the discount amount
	 * @param perUnit
	 *           if <code>true</code> it applies per unit, otherwise per line
	 */
	public LineItemDiscount(final AbstractAmount amount, final boolean perUnit)
	{
		this(amount, perUnit, perUnit ? Integer.MAX_VALUE : 0);
	}

	/**
	 * Creates a new line item discount with given amount. Depending on the given parameters it either applies per unit
	 * or per line.
	 * 
	 * @param amount
	 *           the discount amount
	 * @param perUnit
	 *           if <code>true</code> it applies per unit, otherwise per line
	 * @param applicableUnits
	 *           when applying per unit this contains the maximum applicable number of units
	 */
	public LineItemDiscount(final AbstractAmount amount, final boolean perUnit, final int applicableUnits)
	{
		super(amount);
		this.perUnit = perUnit;
		if (perUnit && applicableUnits < 0)
		{
			throw new IllegalArgumentException("This LineItemDiscount is perUnit and therefore applicableUnits cannot be negative");
		}
		this.applicableUnits = applicableUnits;
	}

	/**
	 * Tell whether this discount should be applied for each unit or just once for the whole line.
	 * 
	 * In case of per unit the number of units are limited by {@link #getApplicableUnits()}.
	 */
	public boolean isPerUnit()
	{
		return perUnit;
	}

	/**
	 * In case this discount applied per unit this returns the maximum number of units that it should apply to.
	 * 
	 * Otherwise this has no effect.
	 * 
	 * @see #isPerUnit()
	 */
	public int getApplicableUnits()
	{
		return applicableUnits;
	}

	/**
	 * For the case this discount applies per unit this method changes the maximum number of units to apply to.
	 * 
	 * @see #isPerUnit()
	 * @see #getApplicableUnits()
	 */
	public void setApplicableUnits(final int numberOfUnits)
	{
		if (numberOfUnits < 0)
		{
			throw new IllegalArgumentException("number of applicable units must be greater or equal than zero");
		}
		this.applicableUnits = numberOfUnits;
	}


	@Override
	public String toString()
	{
		return getAmount() + (isPerUnit() ? " applicableUnits:" + getApplicableUnits() : "");
	}
}
