/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
/**
 *
 */
package de.hybris.platform.solrfacetsearch.solr.impl;

import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * Provides methods for Solr value formatting.
 */
public final class SolrValueFormatUtils
{
	private static final Map<String, Function<String, String>> FORMATTERS = new HashMap<>();

	static
	{
		FORMATTERS.put(generateKey(String.class), SolrValueFormatUtils::formatString);
		FORMATTERS.put(generateKey(Boolean.class), SolrValueFormatUtils::formatBoolean);
		FORMATTERS.put(generateKey(Short.class), SolrValueFormatUtils::formatShort);
		FORMATTERS.put(generateKey(Integer.class), SolrValueFormatUtils::formatInteger);
		FORMATTERS.put(generateKey(Long.class), SolrValueFormatUtils::formatLong);
		FORMATTERS.put(generateKey(Float.class), SolrValueFormatUtils::formatFloat);
		FORMATTERS.put(generateKey(Double.class), SolrValueFormatUtils::formatDouble);
		FORMATTERS.put(generateKey(BigInteger.class), SolrValueFormatUtils::formatBigInteger);
		FORMATTERS.put(generateKey(BigDecimal.class), SolrValueFormatUtils::formatBigDecimal);
		FORMATTERS.put(generateKey(Date.class), SolrValueFormatUtils::formatDate);
	}

	private SolrValueFormatUtils()
	{
		// Empty implementation
	}

	protected static String generateKey(final Class<?> valueClass)
	{
		return valueClass.getCanonicalName();
	}

	/**
	 * Format a value according to the given value class.
	 *
	 * @param value
	 *           - the value
	 * @param valueClass
	 *           - the value class
	 *
	 * @return the formatted value
	 */
	public static String format(final String value, final Class<?> valueClass)
	{
		if (valueClass == null)
		{
			throw new IllegalArgumentException("targetClass cannot be null");
		}

		if (value == null)
		{
			return null;
		}

		final String formatterKey = generateKey(valueClass);
		final Function<String, String> formatter = FORMATTERS.get(formatterKey);

		if (formatter == null)
		{
			throw new SolrServiceRuntimeException("Cannot find formatter for " + valueClass.getCanonicalName());
		}

		return formatter.apply(value);
	}

	/**
	 * Formats the value using {@link String} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatString(final String value)
	{
		return value;
	}

	/**
	 * Formats the value using {@link Boolean} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatBoolean(final String value)
	{
		return Boolean.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link Short} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatShort(final String value)
	{
		return Short.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link Integer} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatInteger(final String value)
	{
		return Integer.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link Long} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatLong(final String value)
	{
		return Long.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link Float} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatFloat(final String value)
	{
		return Float.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link Double} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatDouble(final String value)
	{
		return Double.valueOf(value).toString();
	}

	/**
	 * Formats the value using {@link BigInteger} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatBigInteger(final String value)
	{
		return (new BigInteger(value)).toString();
	}

	/**
	 * Formats the value using {@link BigDecimal} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatBigDecimal(final String value)
	{
		return (new BigDecimal(value)).toString();
	}

	/**
	 * Formats the value using {@link Date} as value type.
	 *
	 * @param value
	 *           - the value to format
	 *
	 * @return the formatted value
	 */
	public static String formatDate(final String value)
	{
		final OffsetDateTime dateTime = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		final OffsetDateTime utcDateTime = dateTime.withOffsetSameInstant(ZoneOffset.UTC);
		return utcDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
