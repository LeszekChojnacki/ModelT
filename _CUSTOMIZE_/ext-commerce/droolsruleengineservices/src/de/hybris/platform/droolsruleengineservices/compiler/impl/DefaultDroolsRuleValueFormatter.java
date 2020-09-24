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
package de.hybris.platform.droolsruleengineservices.compiler.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatter;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatterException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;


public class DefaultDroolsRuleValueFormatter implements DroolsRuleValueFormatter
{
	public static final String NULL_VALUE = "null";

	private final Map<String, DroolsRuleValueFormatterHelper> formatters = new HashMap<>();

	/**
	 * The method initializes formatters.
	 */
	public void initFormatters()
	{
		formatters.put(Boolean.class.getName(), (context, value) -> {
			if (((Boolean) value).booleanValue())
			{
				return "Boolean.TRUE";
			}
			else
			{
				return "Boolean.FALSE";
			}
		});

		formatters.put(Character.class.getName(), (context, value) -> "'" + value + "'");

		formatters.put(String.class.getName(), (context, value) -> "\"" + value + "\"");

		formatters.put(Byte.class.getName(), (context, value) -> "new Byte(" + value + ")");

		formatters.put(Short.class.getName(), (context, value) -> "new Short(" + value + ")");

		formatters.put(Integer.class.getName(), (context, value) -> "new Integer(" + value + ")");

		formatters.put(Long.class.getName(), (context, value) -> "new Long(" + value + ")");

		formatters.put(Float.class.getName(), (context, value) -> "new Float(" + value + ")");

		formatters.put(Double.class.getName(), (context, value) -> "new Double(" + value + ")");

		formatters.put(BigInteger.class.getName(),
				(context, value) -> "new " + context.generateClassName(BigInteger.class) + "(" + value + ")");

		formatters.put(BigDecimal.class.getName(),
				(context, value) -> "new " + context.generateClassName(BigDecimal.class) + "(\"" + value + "\")");

		formatters.put(Enum.class.getName(), (context, value) -> value.getClass().getName() + "." + ((Enum) value).name());

		formatters.put(Date.class.getName(),
				(context, value) -> "new " + context.generateClassName(Date.class) + "(" + ((Date) value).getTime() + ")");

		formatters.put(AbstractList.class.getName(), (context, value) -> {
			final StringJoiner joiner = new StringJoiner(", ", "(", ")");

			((List<Object>) value).stream().forEach(v -> joiner.add(formatValue(context, v)));

			return joiner.toString();
		});

		formatters.put(AbstractMap.class.getName(), (context, value) -> {
			final StringJoiner joiner = new StringJoiner(", ", "[", "]");

			((Map<Object, Object>) value).entrySet().stream()
					.forEach(e -> joiner.add(formatValue(context, e.getKey()) + ":" + formatValue(context, e.getValue())));

			return joiner.toString();
		});
	}

	@Override
	public String formatValue(final DroolsRuleGeneratorContext context, final Object value)
	{
		return formatValue(context, value, () -> Collections.emptyMap());
	}

	protected String formatValue(final DroolsRuleGeneratorContext context, final Object value,
			final Supplier<Map<String, DroolsRuleValueFormatterHelper>> formattersSupplier)
	{
		if (isNullValue(value))
		{
			return NULL_VALUE;
		}

		Class valueClass = value.getClass();

		do
		{
			final DroolsRuleValueFormatterHelper formatter = formattersSupplier.get().getOrDefault(valueClass.getName(),
					getFormatters().get(valueClass.getName()));

			if (formatter != null)
			{
				return formatter.format(context, value);
			}

			valueClass = valueClass.getSuperclass();
		}
		while (nonNull(valueClass));

		throw new DroolsRuleValueFormatterException(
				"Cannot find the value formatter for an object of type: " + value.getClass().getName());
	}

	protected boolean isNullValue(final Object value)
	{
		if (value instanceof Collection)
		{
			return CollectionUtils.isEmpty((Collection) value);
		}

		if (value instanceof Map)
		{
			return MapUtils.isEmpty((Map) value);
		}

		return isNull(value);
	}

	protected Map<String, DroolsRuleValueFormatterHelper> getFormatters()
	{
		return formatters;
	}

	@FunctionalInterface
	protected interface DroolsRuleValueFormatterHelper<V>
	{
		/**
		 *
		 * @param context
		 * @param value
		 * @throws {@link DefaultDroolsRuleValueFormatter}
		 * @return
		 */
		String format(DroolsRuleGeneratorContext context, V value);
	}
}
