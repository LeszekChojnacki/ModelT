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
package de.hybris.platform.ruleengineservices.rule.strategies.impl;

import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterTypeFormatter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Default implementation of {@Link RuleParameterTypeFormatter}
 */
public class DefaultRuleParameterTypeFormatter implements RuleParameterTypeFormatter
{
	private static final String DEFAULT_PARAM_TYPE = "java.lang.String";

	private Map<String, String> formats;

	@Override
	public String formatParameterType(final String paramType)
	{
		if (StringUtils.isEmpty(paramType))
		{
			return DEFAULT_PARAM_TYPE;
		}

		String convertedType = StringUtils.EMPTY;
		if (MapUtils.isNotEmpty(formats))
		{
			convertedType = formatConfigurableTypes(paramType);
		}

		if (StringUtils.isEmpty(convertedType))
		{
			convertedType = paramType;
		}

		return convertedType;
	}

	protected String formatConfigurableTypes(final String paramType)
	{
		for (final Entry<String, String> entry : formats.entrySet())
		{
			final Matcher typeMatcher = Pattern.compile(entry.getKey()).matcher(paramType);

			if (typeMatcher.matches())
			{
				final int matchesNumber = typeMatcher.groupCount();
				final Object[] params = new String[matchesNumber];
				for (int i = 0; i < matchesNumber; i++)
				{
					final String formattedValue = formatParameterType(typeMatcher.group(i + 1));

					params[i] = formattedValue;
				}

				return String.format(entry.getValue(), params);
			}
		}

		return StringUtils.EMPTY;
	}

	public Map<String, String> getFormats()
	{
		return formats;
	}

	public void setFormats(final Map<String, String> formats)
	{
		this.formats = formats;
	}
}
