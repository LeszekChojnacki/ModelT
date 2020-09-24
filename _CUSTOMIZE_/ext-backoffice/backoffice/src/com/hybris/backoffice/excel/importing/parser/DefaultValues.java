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
package com.hybris.backoffice.excel.importing.parser;

import java.util.Collection;
import java.util.Map;


/**
 * Represents parsed default values.
 */
public class DefaultValues
{

	private final String defaultValuesCellValues;
	private final String referenceFormat;
	private final Map<String, String> parsedDefaultValues;

	public DefaultValues(final String defaultValuesCellValues, final String referenceFormat,
			final Map<String, String> parsedDefaultValues)
	{
		this.defaultValuesCellValues = defaultValuesCellValues;
		this.referenceFormat = referenceFormat;
		this.parsedDefaultValues = parsedDefaultValues;
	}

	public String getDefaultValues()
	{
		return defaultValuesCellValues;
	}

	public String getReferenceFormat()
	{
		return referenceFormat;
	}

	public Map<String, String> toMap()
	{
		return parsedDefaultValues;
	}

	public String getDefaultValue(final String key)
	{
		return parsedDefaultValues.get(key);
	}

	public Collection<String> getKeys()
	{
		return parsedDefaultValues.keySet();
	}

	public Collection<String> getValues()
	{
		return parsedDefaultValues.values();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final DefaultValues that = (DefaultValues) o;

		if (defaultValuesCellValues != null ? !defaultValuesCellValues.equals(that.defaultValuesCellValues)
				: (that.defaultValuesCellValues != null))
		{
			return false;
		}
		if (referenceFormat != null ? !referenceFormat.equals(that.referenceFormat) : (that.referenceFormat != null))
		{
			return false;
		}
		return parsedDefaultValues != null ? parsedDefaultValues.equals(that.parsedDefaultValues)
				: (that.parsedDefaultValues == null);
	}

	@Override
	public int hashCode()
	{
		int result = defaultValuesCellValues != null ? defaultValuesCellValues.hashCode() : 0;
		result = 31 * result + (referenceFormat != null ? referenceFormat.hashCode() : 0);
		result = 31 * result + (parsedDefaultValues != null ? parsedDefaultValues.hashCode() : 0);
		return result;
	}
}
