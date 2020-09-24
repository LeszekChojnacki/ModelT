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
 * Defines a line item specific charge having a amount (can be {@link Money} or {@link Percentage}) that can only be set
 * during creation.
 * 
 * It either applies per unit or per line as specified within constructor. In case it applies per unit the maximum
 * number of units to apply to is being specified via {@link #setApplicableUnits(int)}.
 * 
 * Also the charge can be marked to be ignored (see {@link #setDisabled(boolean)}).
 */
public class LineItemCharge extends AbstractCharge
{
	private final boolean perUnit;
	private int applicableUnits;

	/**
	 * Creates a new line item charge with a given amount.
	 * 
	 * The charge is applied per line.
	 */
	public LineItemCharge(final AbstractAmount amount)
	{
		this(amount, false);
	}

	/**
	 * Creates a new line item charge with a specified amount. It may be either applied per unit or per line.
	 * 
	 * @param amount
	 *           the charge amount
	 * @param perUnit
	 *           if <code>true</code> the charge is being applied for each unit of the line item, otherwise just once per
	 *           line
	 */
	public LineItemCharge(final AbstractAmount amount, final boolean perUnit)
	{
		this(amount, perUnit, Integer.MAX_VALUE);
	}

	/**
	 * Creates a new line item charge with a specified amount. It may be either applied per unit or per line.
	 * 
	 * @param amount
	 *           the charge amount
	 * @param perUnit
	 *           if <code>true</code> the charge is being applied for each unit of the line item, otherwise just once per
	 *           line
	 * @param applicableForUnits
	 *           if applying per unit this limits the number of units that this charge must be applied to
	 */
	public LineItemCharge(final AbstractAmount amount, final boolean perUnit, final int applicableForUnits)
	{
		super(amount);
		this.perUnit = perUnit;
		if (perUnit && applicableForUnits < 0)
		{
			throw new IllegalArgumentException("This LineItemCharge is perUnit and therefore applicableForUnits cannot be negative");
		}
		this.applicableUnits = perUnit ? applicableForUnits : 0;
	}

	/**
	 * Tells whether this charge applies to each unit of the line item or just once per line.
	 * 
	 * In case it applies per unit the maximum number of units is specified by {@link #getApplicableUnits()}.
	 * 
	 * @see #getApplicableUnits()
	 */
	public boolean isPerUnit()
	{
		return perUnit;
	}

	/**
	 * In case this charge applies per unit this method returns the maximum number of units to apply to.
	 * 
	 * @see #isPerUnit()
	 * @see #setApplicableUnits(int)
	 */
	public int getApplicableUnits()
	{
		return applicableUnits;
	}

	/**
	 * For the case this charge applies per unit this method changes the maximum number of units to apply to.
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
		return getAmount() + " dontCharge:" + isDisabled() + (isPerUnit() ? " applicableUnit:" + getApplicableUnits() : "")
				+ (getChargeType() != null ? " type:" + getChargeType() : "");
	}
}