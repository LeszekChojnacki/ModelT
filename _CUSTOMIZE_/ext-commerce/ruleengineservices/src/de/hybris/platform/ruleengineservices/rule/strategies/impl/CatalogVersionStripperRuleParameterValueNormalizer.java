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

import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizer;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringBefore;


/**
 * Implementation of {@link RuleParameterValueNormalizer} that performs trims down the provided String value to its
 * catalog aware agnostic value. It uses configured <code>delimiter</code> as a flag character for stripping.
 */
public class CatalogVersionStripperRuleParameterValueNormalizer implements RuleParameterValueNormalizer
{
	private String delimiter;

	@Override
	public Object normalize(final Object value)
	{
		if (value instanceof Collection)
		{
			return ((Collection) value).stream().map(v -> normalizeSingleValue(v)).collect(toList());
		}
		else
		{
			return normalizeSingleValue(value);
		}
	}

	protected Object normalizeSingleValue(final Object value)
	{
		if (value == null)
		{
			return null;
		}

		return substringBefore(valueOf(value), getDelimiter());
	}

	protected String getDelimiter()
	{
		return delimiter;
	}

	@Required
	public void setDelimiter(final String delimiter)
	{
		this.delimiter = delimiter;
	}
}
