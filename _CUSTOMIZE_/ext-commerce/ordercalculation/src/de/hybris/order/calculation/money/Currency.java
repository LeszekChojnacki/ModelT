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
package de.hybris.order.calculation.money;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;


/**
 * Currency object to be used from inside the calculation framework. It simply combines a code and the number of digits
 * available for that currency.
 * 
 * Actually this class only exists because {@link java.util.Currency} does not support creation of artificial
 * currencies.
 */
public class Currency
{
	private final String isocodelowercase;
	private final String isocode;
	private final int digits;

	private static final ConcurrentMap<String, Currency> commonCurrencyCache = new ConcurrentHashMap<String, Currency>();

	/**
	 * Creates a new currency with given iso code and digits.
	 */
	public Currency(final String isocode, final int digits)
	{
		if (StringUtils.isEmpty(isocode))
		{
			throw new IllegalArgumentException("Iso code cannot be empty");
		}
		if (digits < 0)
		{
			throw new IllegalArgumentException("Digits cannot be less than zero");
		}
		this.isocodelowercase = isocode.toLowerCase();
		this.isocode = isocode;
		this.digits = digits;
	}

	/**
	 * Shortcut for getting commonly used currency instances.
	 *
	 * Prefer this to {@link #Currency(String, int)} since this method caches instances.
	 */
	public static Currency valueOf(final String code, final int digits)
	{
		final String key = code.toLowerCase() + digits;
		Currency ret = commonCurrencyCache.get(key);
		if (ret == null)
		{
			ret = new Currency(code, digits);
			final Currency previous = commonCurrencyCache.putIfAbsent(key, ret);
			if (previous != null)
			{
				ret = previous;
			}
		}
		return ret;
	}

	/**
	 * A {@link Currency} is equal to an object if this object is also a {@link Currency} and this currency has the same
	 * digit count and the isocode is equal (ignore case) to the other isocode.
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (!(obj.getClass().equals(Currency.class)))
		{
			return false;
		}

		return digits == ((Currency) obj).getDigits() && this.isocodelowercase.equalsIgnoreCase(((Currency) obj).getIsoCode());
	}

	@Override
	public int hashCode()
	{
		return this.isocodelowercase.hashCode() * (this.digits + 1);
	}

	@Override
	public String toString()
	{
		return this.isocode;
	}

	public String getIsoCode()
	{
		return isocode;
	}

	public int getDigits()
	{
		return digits;
	}
}
