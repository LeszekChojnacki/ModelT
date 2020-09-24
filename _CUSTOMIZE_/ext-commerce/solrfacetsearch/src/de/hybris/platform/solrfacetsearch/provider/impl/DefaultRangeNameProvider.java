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
import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.ValueRangeSet;
import de.hybris.platform.solrfacetsearch.config.ValueRangeType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.config.exceptions.PropertyOutOfRangeException;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.RangeNameProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.util.CollectionUtils;


public class DefaultRangeNameProvider implements RangeNameProvider
{
	@Override
	public boolean isRanged(final IndexedProperty property)
	{
		return !CollectionUtils.isEmpty(property.getValueRangeSets());
	}

	@Override
	public List<ValueRange> getValueRanges(final IndexedProperty property, final String qualifier)
	{
		ValueRangeSet valueRangeSet;
		if (qualifier == null)
		{
			valueRangeSet = property.getValueRangeSets().get("default");
		}
		else
		{
			valueRangeSet = property.getValueRangeSets().get(qualifier);
			if (valueRangeSet == null)
			{
				valueRangeSet = property.getValueRangeSets().get("default");
			}
		}
		if (valueRangeSet != null)
		{
			return valueRangeSet.getValueRanges();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> getRangeNameList(final IndexedProperty property, final Object value) throws FieldValueProviderException
	{
		return getRangeNameList(property, value, null);
	}

	@Override
	public List<String> getRangeNameList(final IndexedProperty property, final Object value, final String qualifier)
			throws FieldValueProviderException
	{
		final int SINGLE_VALUE_SIZE = 1;
		List<String> rangeNameList = new ArrayList<>();

		if (!isRanged(property) || value == null)
		{
			return rangeNameList;
		}

		try
		{
			for (final ValueRange range : getValueRanges(property, qualifier))
			{
				addValueRange(rangeNameList, property, value, range);
			}

			if (rangeNameList.isEmpty())
			{
				throw new PropertyOutOfRangeException("No range found for property: [" + property.getName() + "] with value [" + value
						+ "] and qualifier [" + qualifier + "]");
			}
			else if (rangeNameList.size() > SINGLE_VALUE_SIZE && !property.isMultiValue()) // if property is not multivalued we return only first rangeName
			{
				final String message = "There was found multiple ranges " + rangeNameList + " for not multiple property ["
						+ property.getName() + "] with value [" + value + "]. Only first range was returned";
				rangeNameList = rangeNameList.subList(0, SINGLE_VALUE_SIZE);
				FieldValueProvider.LOG.warn(message);
			}

			return rangeNameList;
		}
		catch (final NumberFormatException | ClassCastException e)
		{
			throw new FieldValueProviderException(
					"Cannot get range for property [" + property.getName() + "] with value [" + value + "]", e);
		}
	}

	protected void addValueRange(final List<String> rangeNameList, final IndexedProperty property, final Object value,
			final ValueRange range)
	{
		final Comparable cFrom = range.getFrom();
		final Comparable cTo = range.getTo();
		Comparable cValue;
		Object from;
		Object to;

		if (isStringOrTextType(property))
		{
			cValue = ((String) value).toLowerCase(Locale.ROOT).substring(0, ((String) cFrom).length());
			from = cFrom.toString();
			to = valueOrDefault(cTo, String::toString, () -> null);
		}
		else if (isDoubleType(property))
		{
			cValue = BigDecimal.valueOf((Double)value);
			from = new BigDecimal(cFrom.toString());
			to = valueOrDefault(cTo, BigDecimal::new, () -> BigDecimal.valueOf(Double.MAX_VALUE));
		}
		else if (isFloatType(property))
		{
			cValue = BigDecimal.valueOf((Float)value);
			from = new BigDecimal(cFrom.toString());
			to = valueOrDefault(cTo, BigDecimal::new, () -> BigDecimal.valueOf(Float.MAX_VALUE));
		}
		else if (isIntegerType(property))
		{
			cValue = (Integer) value;
			from = Integer.valueOf(cFrom.toString());
			to = valueOrDefault(cTo, Integer::new, () -> Integer.valueOf(Integer.MAX_VALUE));
		}
		else
		{
			cValue = (Comparable) value;
			from = cFrom;
			to = cTo;
		}

		if (to != null && cValue.compareTo(from) >= 0 && cValue.compareTo(to) <= 0)
		{
			rangeNameList.add(range.getName());
		}
	}

	protected boolean isStringOrTextType(final IndexedProperty property)
	{
		return ValueRangeType.STRING.toString().equalsIgnoreCase(property.getType())
				|| ValueRangeType.TEXT.toString().equalsIgnoreCase(property.getType());
	}

	protected boolean isDoubleType(final IndexedProperty property)
	{
		return ValueRangeType.DOUBLE.toString().equalsIgnoreCase(property.getType());
	}

	protected boolean isFloatType(final IndexedProperty property)
	{
		return ValueRangeType.FLOAT.toString().equalsIgnoreCase(property.getType());
	}

	protected boolean isIntegerType(final IndexedProperty property)
	{
		return ValueRangeType.INT.toString().equalsIgnoreCase(property.getType());
	}

	protected Comparable valueOrDefault(final Object value, final Function<String, Comparable> valueFunction,
			final Supplier<Comparable> defaultSupplier)
	{
		if (value == null)
		{
			return defaultSupplier.get();
		}

		return valueFunction.apply(value.toString());
	}
}
