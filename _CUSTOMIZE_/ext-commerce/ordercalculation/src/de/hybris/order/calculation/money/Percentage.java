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

import de.hybris.order.calculation.exception.AmountException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Represents a relative amount to be used inside the calculation framework.
 * <p/>
 * Provides common operations like {@link #add(Percentage)} and {@link #subtract(Percentage)}.
 * <p/>
 * Besides there are some utility methods to ease working with that class: {@link #sum(Collection)} and
 * {@link #split(int)}.
 */
public class Percentage extends AbstractAmount
{
	public static final Percentage ZERO = new Percentage(0);

	public static final Percentage TEN = new Percentage(10);

	public static final Percentage TWENTY = new Percentage(20);

	public static final Percentage TWENTYFIVE = new Percentage(25);

	public static final Percentage THIRTY = new Percentage(30);

	public static final Percentage FOURTY = new Percentage(40);

	public static final Percentage FIFTY = new Percentage(50);

	public static final Percentage SIXTY = new Percentage(60);

	public static final Percentage SEVENTY = new Percentage(70);

	public static final Percentage SEVENTYFIVE = new Percentage(75);

	public static final Percentage EIGHTY = new Percentage(80);

	public static final Percentage NINETY = new Percentage(90);

	public static final Percentage HUNDRED = new Percentage(100);

	private final BigDecimal rate;


	/**
	 * Creates a new percentage with the specified rate.
	 */
	public Percentage(final String rate)
	{
		if (rate == null)
		{
			throw new IllegalArgumentException("Parameter 'rate' is null!");
		}
		this.rate = new BigDecimal(rate);
	}

	/**
	 * Creates a new percentage with the specified rate.
	 */
	public Percentage(final int rate)
	{
		this(BigDecimal.valueOf(rate)); //shortcut
	}

	/**
	 * Creates a new percentage with the specified rate.
	 */
	public Percentage(final BigDecimal rate)
	{
		if (rate == null)
		{
			throw new IllegalArgumentException("Parameter 'rate' is null!");
		}
		this.rate = rate;
	}

	/**
	 * Creates a list percentages from the given String values.
	 */
	public static List<Percentage> valueOf(final String... rates)
	{
		final Percentage[] ret = new Percentage[rates.length];
		int index = 0;
		for (final String s : rates)
		{
			ret[index++] = new Percentage(s);
		}
		return Arrays.asList(ret);
	}

	/**
	 * Creates a list percentages from the given int values.
	 */
	public static List<Percentage> valueOf(final int... rates)
	{
		final Percentage[] ret = new Percentage[rates.length];
		int index = 0;
		for (final int r : rates)
		{
			ret[index++] = new Percentage(r);
		}
		return Arrays.asList(ret);
	}

	/**
	 * Creates a list percentages from the given BigDecimal values.
	 */
	public static List<Percentage> valueOf(final BigDecimal... rates)
	{
		final Percentage[] ret = new Percentage[rates.length];
		int index = 0;
		for (final BigDecimal r : rates)
		{
			ret[index++] = new Percentage(r);
		}
		return Arrays.asList(ret);
	}

	/**
	 * Returns the percentage rate.
	 */
	public BigDecimal getRate()
	{
		return rate;
	}

	/**
	 * Adds a percentage to current and returns the sum as new object.
	 */
	public Percentage add(final Percentage percentage)
	{
		return new Percentage(rate.add(percentage.getRate()));
	}

	// REVIEW: why not offering add( int ) : Percentage as well ?

	/**
	 * Subtracts a percentage to current and returns the result as new object.
	 */
	public Percentage subtract(final Percentage percentage)
	{
		return new Percentage(rate.subtract(percentage.getRate()));
	}

	// REVIEW: why not offering subtract( int ) : Percentage as well ?


	@Override
	public int hashCode()
	{
		return rate.hashCode();
	}

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

		if (!(obj.getClass().equals(Percentage.class)))
		{
			return false;
		}

		return ((Percentage) obj).getRate().compareTo(this.rate) == 0;
	}

	@Override
	public String toString()
	{
		return rate + "%";
	}

	/**
	 * Sums a list of percentages.
	 */
	public static final Percentage sum(final Percentage... percent)
	{
		if (percent == null || percent.length == 0)
		{
			throw new AmountException("Cannot sum nothing");
		}

		return sum(Arrays.asList(percent));
	}

	/**
	 * Sums a list of percentages.
	 */
	public static final Percentage sum(final Collection<Percentage> elements)
	{
		if (elements == null)
		{
			throw new IllegalArgumentException("Cannot sum null");
		}
		BigDecimal res = BigDecimal.ZERO;
		for (final Percentage x : elements)
		{
			res = res.add(x.getRate());
		}
		return new Percentage(res);
	}

	/**
	 * Splits this percentage into the specified number of parts.
	 */
	public List<Percentage> split(final int parts)
	{
		if (parts < 1)
		{
			throw new IllegalArgumentException("parts is less than 1");
		}
		final long total = rate.unscaledValue().longValue();
		final long[] results = new long[parts];

		Arrays.fill(results, total / parts);
		final long remainder = total % parts;

		for (int index = 0; index < remainder; index++)
		{
			results[index]++;
		}

		final List<Percentage> result = new ArrayList<Percentage>();
		for (int index = 0; index < parts; index++)
		{
			result.add(new Percentage(new BigDecimal(BigInteger.valueOf(results[index]), rate.scale())));
		}
		return result;
	}

}
