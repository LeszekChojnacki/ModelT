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
package de.hybris.platform.voucher.jalo.util;

import de.hybris.platform.jalo.c2l.Currency;


/**
 * This class represents the value of a voucher
 */
public class VoucherValue
{
	// --------------------------------------------------------------- Constants
	// ------------------------------------------------------ Instance Variables
	private final double theValue;
	private final Currency theCurrency;

	// ------------------------------------------------------------ Constructors
	/**
	 * Creates a new instance of <code>VoucherValue</code>.
	 */
	public VoucherValue(final double aValue, final Currency aCurrency)
	{
		this.theValue = aValue;
		this.theCurrency = aCurrency;
	}

	// -------------------------------------------------------------- Properties
	public Currency getCurrency()
	{
		return this.theCurrency;
	}

	public String getCurrencyIsoCode()
	{
		return getCurrency() != null ? getCurrency().getIsoCode() : null; //NOSONAR
	}

	public double getValue()
	{
		return this.theValue;
	}
}
