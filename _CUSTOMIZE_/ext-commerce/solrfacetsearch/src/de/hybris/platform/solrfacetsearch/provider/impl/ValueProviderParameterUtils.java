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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;

import java.util.Map;

import org.apache.commons.lang.StringUtils;



public final class ValueProviderParameterUtils
{
	private ValueProviderParameterUtils()
	{
	}

	public static int getInt(final IndexedProperty indexedProperty, final String key, final int defaultValue)
	{
		int value = defaultValue;

		final String stringValue = getStringValue(indexedProperty, key);
		if (stringValue != null)
		{
			value = Integer.parseInt(stringValue);
		}

		return value;
	}

	public static long getLong(final IndexedProperty indexedProperty, final String key, final long defaultValue)
	{
		long value = defaultValue;

		final String stringValue = getStringValue(indexedProperty, key);
		if (stringValue != null)
		{
			value = Long.parseLong(stringValue);
		}

		return value;
	}

	public static double getDouble(final IndexedProperty indexedProperty, final String key, final double defaultValue)
	{
		double value = defaultValue;

		final String stringValue = getStringValue(indexedProperty, key);
		if (stringValue != null)
		{
			value = Double.parseDouble(stringValue);
		}

		return value;
	}

	public static boolean getBoolean(final IndexedProperty indexedProperty, final String key, final boolean defaultValue)
	{
		boolean value = defaultValue;

		final String stringValue = getStringValue(indexedProperty, key);
		if (stringValue != null)
		{
			value = Boolean.parseBoolean(stringValue);
		}

		return value;
	}

	public static String getString(final IndexedProperty indexedProperty, final String key, final String defaultValue)
	{
		String value = defaultValue;

		final String stringValue = getStringValue(indexedProperty, key);
		if (stringValue != null)
		{
			value = stringValue;
		}

		return value;
	}

	protected static String getStringValue(final IndexedProperty indexedProperty, final String key)
	{
		String stringValue = null;

		final Map<String, String> parameters = indexedProperty.getValueProviderParameters();
		if (parameters != null)
		{
			stringValue = StringUtils.trimToNull(parameters.get(key));
		}

		return stringValue;
	}
}
