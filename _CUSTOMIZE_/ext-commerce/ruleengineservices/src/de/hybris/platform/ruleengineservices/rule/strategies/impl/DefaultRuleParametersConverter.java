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

import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParametersConverter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;


/**
 * Implementation of {@link RuleParametersConverter} that uses a JSON format for strings.
 */
public class DefaultRuleParametersConverter extends AbstractRuleConverter implements RuleParametersConverter
{
	@Override
	public String toString(final List<RuleParameterData> parameters)
	{
		try
		{
			return getObjectWriter().writeValueAsString(parameters);
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	@Override
	public List<RuleParameterData> fromString(final String parameters)
	{
		if (StringUtils.isBlank(parameters))
		{
			return Collections.emptyList();
		}

		try
		{
			final ObjectReader objectReader = getObjectReader();
			final JavaType javaType = objectReader.getTypeFactory().constructCollectionType(List.class, RuleParameterData.class);
			final List<RuleParameterData> parsedParameters = objectReader.forType(javaType).readValue(parameters);

			convertParameterValues(parsedParameters);

			return parsedParameters;
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	protected void convertParameterValues(final List<RuleParameterData> parameters)
	{
		if (CollectionUtils.isEmpty(parameters))
		{
			return;
		}

		for (final RuleParameterData parameter : parameters)
		{
			final Object value = getRuleParameterValueConverter().fromString((String) parameter.getValue(), parameter.getType());
			parameter.setValue(value);
		}
	}
}
