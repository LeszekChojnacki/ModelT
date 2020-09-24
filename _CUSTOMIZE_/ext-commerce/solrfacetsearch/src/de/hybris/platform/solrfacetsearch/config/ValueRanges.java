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
package de.hybris.platform.solrfacetsearch.config;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class ValueRanges
{
	public static final String INFINITY = "INF";
	public static final Set<String> ALLOWEDTYPES;
	public static final String DATEFORMAT = "yyyy-MM-dd [HH:mm]";

	static
	{
		ALLOWEDTYPES = Collections.unmodifiableSet(
				Stream.of(ValueRangeType.values()).map(type -> type.toString().toLowerCase(Locale.ROOT)).collect(Collectors.toSet()));
	}

	// Suppresses default constructor, ensuring non-instantiability.
	private ValueRanges()
	{
	}

	public static String getAllowedRangeTypes()
	{
		final String separator = ", ";
		String result = "";
		for (final String type : ALLOWEDTYPES)
		{
			if ("".equals(result))
			{
				result = result.concat(type);
			}
			else
			{
				result = result.concat(separator + type);
			}
		}
		return result;
	}

	public static Date parseDate(final String date) throws ParseException
	{
		final DateFormat df = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
		df.setLenient(false);
		try
		{
			return df.parse(date.replaceAll("(\\s)+", " "));
		}
		catch (final ParseException e)
		{
			if (!date.contains("["))
			{
				return df.parse(date.intern().concat(" [00:00]"));
			}
			else
			{
				throw e;
			}
		}
	}

	public static ValueRange createValueRange(final String name, final Object from, final Object to)
	{
		final ValueRange set = new ValueRange();
		set.setFrom((Comparable) from);
		set.setName(name);
		set.setTo((Comparable) to);
		return set;
	}
}
