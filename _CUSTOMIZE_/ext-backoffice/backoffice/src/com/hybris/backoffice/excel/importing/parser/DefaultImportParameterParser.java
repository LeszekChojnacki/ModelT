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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.fest.util.Arrays;
import org.fest.util.Collections;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.parser.matcher.ExcelParserMatcher;
import com.hybris.backoffice.excel.importing.parser.splitter.ExcelParserSplitter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


public class DefaultImportParameterParser implements ImportParameterParser
{

	private ExcelParserMatcher matcher;
	private ExcelParserSplitter splitter;

	private int order;

	@Override
	public boolean matches(@Nonnull final String referenceFormat)
	{
		return matcher.test(referenceFormat);
	}

	/**
	 * Parses referencePattern cell (which is located in the second row) and default values cell (which is located in the
	 * third row) and creates map where key is equals to reference key and value is equals to value provided in the third
	 * row. For example, for the following reference cell's value: catalog:version and default value cell: Default:Online
	 * the following map will be returned: {{key: catalog, value: Default}, {key: version, value: Online}}
	 *
	 * @param referenceFormat
	 *           cell's value from second row of excel sheet
	 * @param defaultValues
	 *           cell's value from third row of excel sheet
	 * @return DefaultValues object. If default values are not provided then only keys will be returned.
	 */
	public DefaultValues parseDefaultValues(final String referenceFormat, final String defaultValues)
	{
		final String trimmedReferenceFormat = StringUtils.trim(referenceFormat);
		final String trimmedDefaultValues = StringUtils.trim(defaultValues);
		final Map<String, String> defaultValuesMap = new LinkedHashMap<>();

		if (StringUtils.isBlank(trimmedReferenceFormat))
		{
			return new DefaultValues(trimmedDefaultValues, trimmedReferenceFormat, defaultValuesMap);
		}
		final String[] referenceFormatTokens = splitter.apply(trimmedReferenceFormat);
		final String[] defaultValuesTokens = splitter.apply(trimmedDefaultValues);

		for (int i = 0; i < referenceFormatTokens.length; i++)
		{
			final String referenceFormatToken = referenceFormatTokens[i];
			final String defaultValueToken = defaultValuesTokens.length > i && StringUtils.isNotBlank(defaultValuesTokens[i])
					? defaultValuesTokens[i]
					: null;
			defaultValuesMap.put(referenceFormatToken, defaultValueToken);
		}
		return new DefaultValues(trimmedDefaultValues, trimmedReferenceFormat, defaultValuesMap);
	}

	/**
	 * Parses referencePattern cell (which is located in the second row) and cell with data (which is located in the data
	 * row) and creates map where key is equal to reference key and value is equals to value provided in the data row. If
	 * value is null then default value is taken into account. For example, for the following reference cell's value:
	 * catalog:version and cell's value: Default:Online the following map will be returned: {{key: catalog, value: Default},
	 * {key: version, value: Online}}. Parameters map always contains key : {@link ImportParameters#RAW_VALUE} which
	 * represents not parsed value, but merged with default values
	 *
	 * @param cellValue
	 *           value of cell's data
	 * @param defaultValues
	 *           parsed default values
	 * @return ParsedValues object. If default values are not provided then only keys will be returned.
	 */
	public ParsedValues parseValue(final String cellValue, final DefaultValues defaultValues)
	{
		final String trimmedCellValue = StringUtils.trim(cellValue);
		final Map<String, String> defaultValuesMap = defaultValues.toMap();
		final List<Map<String, String>> convertedParameters = new ArrayList<>();

		final String[] multiValues = StringUtils.splitPreserveAllTokens(trimmedCellValue, ImportParameters.MULTIVALUE_SEPARATOR);

		if (Arrays.isEmpty(multiValues))
		{
			if (!defaultValuesMap.isEmpty())
			{
				defaultValuesMap.put(ImportParameters.RAW_VALUE, joinValues(defaultValues.getValues()));
			}
			else
			{
				if (StringUtils.isBlank(trimmedCellValue) && StringUtils.isNotBlank(defaultValues.getDefaultValues()))
				{
					defaultValuesMap.put(ImportParameters.RAW_VALUE, defaultValues.getDefaultValues());
				}
				else
				{
					defaultValuesMap.put(ImportParameters.RAW_VALUE, trimmedCellValue);
				}
			}
			convertedParameters.add(defaultValuesMap);
		}
		for (final String multivalue : multiValues)
		{
			final Map<String, String> params = new LinkedHashMap<>();
			final String[] providedValues = splitter.apply(multivalue);
			int index = 0;
			final List<String> values = new ArrayList<>();
			if (Collections.isEmpty(defaultValues.toMap().entrySet()))
			{
				values.add(multivalue);
			}
			for (final Map.Entry<String, String> defaultValue : defaultValues.toMap().entrySet())
			{
				final String value = providedValues.length > index && StringUtils.isNotBlank(providedValues[index])
						? providedValues[index]
						: defaultValue.getValue();
				params.put(defaultValue.getKey(), value);
				values.add(value);
				++index;
			}
			params.put(ImportParameters.RAW_VALUE, joinValues(values));
			convertedParameters.add(params);
		}
		return new ParsedValues(
				String.join(ImportParameters.MULTIVALUE_SEPARATOR,
						convertedParameters.stream().map(map -> map.get(ImportParameters.RAW_VALUE)).collect(Collectors.toList())),
				convertedParameters);
	}

	private static String joinValues(final Collection<String> values)
	{
		final List<String> valuesWithoutNulls = values.stream().map(value -> value == null ? StringUtils.EMPTY : value)
				.collect(Collectors.toList());
		if (valuesWithoutNulls.stream().anyMatch(StringUtils::isNotBlank))
		{
			return String.join(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR, valuesWithoutNulls);
		}
		return StringUtils.EMPTY;
	}

	@Required
	public void setMatcher(final ExcelParserMatcher matcher)
	{
		this.matcher = matcher;
	}

	@Required
	public void setSplitter(final ExcelParserSplitter splitter)
	{
		this.splitter = splitter;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	@Required
	public void setOrder(final int order)
	{
		this.order = order;
	}
}
