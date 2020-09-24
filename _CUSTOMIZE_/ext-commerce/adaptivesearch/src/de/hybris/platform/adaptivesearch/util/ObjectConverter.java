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
/**
 *
 */
package de.hybris.platform.adaptivesearch.util;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.AsRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides methods for generic object conversion.
 */
public final class ObjectConverter
{
	protected static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	private static final Map<String, Method> CONVERTERS = new HashMap<>();

	static
	{
		final Method[] methods = ObjectConverter.class.getDeclaredMethods();
		for (final Method method : methods)
		{
			if (method.getParameterTypes().length == 1)
			{
				final Class<?> sourceClass = method.getParameterTypes()[0];
				final Class<?> targetClass = method.getReturnType();
				final String converterKey = generateConverterKey(sourceClass, targetClass);
				CONVERTERS.put(converterKey, method);
			}
		}
	}

	private ObjectConverter()
	{
		// Empty implementation
	}

	protected static String generateConverterKey(final Class<?> sourceClass, final Class<?> targetClass)
	{
		return sourceClass.getCanonicalName() + "_" + targetClass.getCanonicalName();
	}

	/**
	 * Converts a value to the given target class.
	 *
	 * @param value
	 *           - the value
	 * @param targetClass
	 *           - the target class
	 *
	 * @return the converted value
	 *
	 * @throws AsException
	 *            - if an error occurs during the conversion
	 */
	public static <T> T convert(final Object value, final Class<T> targetClass) throws AsException
	{
		if (targetClass == null)
		{
			throw new IllegalArgumentException("targetClass cannot be null");
		}

		if (value == null)
		{
			return null;
		}

		final Class<?> sourceClass = value.getClass();
		if (targetClass.isAssignableFrom(sourceClass))
		{
			return (T) value;
		}

		final String converterKey = generateConverterKey(sourceClass, targetClass);
		final Method converter = CONVERTERS.get(converterKey);

		if (converter == null)
		{
			throw new AsException(
					"Cannot find converter from " + sourceClass.getCanonicalName() + " to " + targetClass.getCanonicalName());
		}

		try
		{
			return (T) converter.invoke(targetClass, value);
		}
		catch (final RuntimeException | IllegalAccessException | InvocationTargetException e)
		{
			throw new AsException("Cannot convert value " + value + " from " + sourceClass.getCanonicalName() + " to "
					+ targetClass.getCanonicalName(), e);
		}
	}

	/**
	 * Converts from {@link Boolean} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String booleanToString(final Boolean value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Boolean}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Boolean stringToBoolean(final String value)
	{
		return Boolean.valueOf(value);
	}

	/**
	 * Converts from {@link Short} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String shortToString(final Short value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Short}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Short stringToShort(final String value)
	{
		return Short.valueOf(value);
	}

	/**
	 * Converts from {@link Integer} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String integerToString(final Integer value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Integer}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Integer stringToInteger(final String value)
	{
		return Integer.valueOf(value);
	}

	/**
	 * Converts from {@link Long} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String longToString(final Long value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Long}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Long stringToLong(final String value)
	{
		return Long.valueOf(value);
	}

	/**
	 * Converts from {@link Float} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String floatToString(final Float value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Float}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Float stringToFloat(final String value)
	{
		return Float.valueOf(value);
	}

	/**
	 * Converts from {@link Double} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String doubleToString(final Double value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link Double}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Double stringToDouble(final String value)
	{
		return Double.valueOf(value);
	}

	/**
	 * Converts from {@link BigInteger} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String bigIntegerToString(final BigInteger value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link BigInteger}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static BigInteger stringToBigInteger(final String value)
	{
		return new BigInteger(value);
	}

	/**
	 * Converts from {@link BigDecimal} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String bigDecimalToString(final BigDecimal value)
	{
		return value.toString();
	}

	/**
	 * Converts from {@link String} to {@link BigDecimal}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static BigDecimal stringToBigDecimal(final String value)
	{
		return new BigDecimal(value);
	}

	/**
	 * Converts from {@link Date} to {@link String}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static String dateToString(final Date value)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
		return dateFormat.format(value);
	}

	/**
	 * Converts from {@link String} to {@link Date}.
	 *
	 * @param value
	 *           - the value to convert
	 *
	 * @return the converted value
	 */
	public static Date stringToDate(final String value)
	{
		try
		{
			final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
			return dateFormat.parse(value);
		}
		catch (final ParseException e)
		{
			throw new AsRuntimeException(e);
		}
	}
}
