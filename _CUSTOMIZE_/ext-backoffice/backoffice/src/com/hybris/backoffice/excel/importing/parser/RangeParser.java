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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Implementation of {@link ImportParameterParser} which allows to parse default values for classification' ranges
 */
public class RangeParser implements ImportParameterParser
{
	private int order = 1000;

	private ParserRegistry parserRegistry;

	/**
	 * Checks whether given input matches to {@link RangeParserUtils#RANGE_PATTERN}.
	 *
	 * @param referenceFormat
	 *           value to check
	 * @return true if given input matches to pattern, false otherwise
	 */
	@Override
	public boolean matches(@Nonnull final String referenceFormat)
	{
		return RangeParserUtils.RANGE_PATTERN.matcher(referenceFormat).matches();
	}

	/**
	 * Parses given values and default values basing on referenceFormat
	 *
	 * @param referenceFormat
	 *           format of cell value should fit. It is placed in
	 *           {@value ExcelTemplateConstants#REFERENCE_PATTERN_ROW_INDEX} row of excel file.
	 * @param defaultValues
	 *           default value of cell. It is placed in {@value ExcelTemplateConstants#DEFAULT_VALUES_ROW_INDEX} row of
	 *           excel file.
	 * @param values
	 *           value of cell
	 * @return parsed values
	 */
	@Override
	public ParsedValues parseValue(@Nonnull final String referenceFormat, final String defaultValues, final String values)
	{
		if (StringUtils.isNotBlank(defaultValues) && StringUtils.isBlank(values))
		{
			final DefaultValues dv = parseDefaultValues(referenceFormat, StringUtils.EMPTY);
			return parseValue(defaultValues, dv);
		}

		final DefaultValues parsedDefaultValues = parseDefaultValues(referenceFormat, defaultValues);
		return parseValue(values, parsedDefaultValues);
	}

	/**
	 * Parses referencePattern cell (which is located in the second row) and default values cell (which is located in the
	 * third row) and creates map where key equals to reference key and value equals to value provided in the third row.
	 * <br/>
	 * For example, for the following reference cell's value:
	 * {@value RangeParserUtils#RANGE_PREFIX}value1{@value RangeParserUtils#RANGE_DELIMITER}value2{@value RangeParserUtils#RANGE_SUFFIX}
	 * and default value cell:
	 * {@value RangeParserUtils#RANGE_PREFIX}default1{@value RangeParserUtils#RANGE_DELIMITER}default2{@value RangeParserUtils#RANGE_SUFFIX}
	 * the following map will be returned: {{key: {@value RangeParserUtils#RANGE_FROM_PREFIX}value1, value: default1},
	 * {key: {@value RangeParserUtils#RANGE_TO_PREFIX}value2, value: default2}}
	 *
	 * @param referenceFormat
	 *           cell's value from second row of excel sheet. Should matches the {@link RangeParserUtils#RANGE_PATTERN}
	 *           pattern.
	 * @param defaultValues
	 *           cell's value from third row of excel sheet. Should matches the {@link RangeParserUtils#RANGE_PATTERN}
	 *           pattern.
	 * @return DefaultValues object.
	 */
	@Override
	public DefaultValues parseDefaultValues(final String referenceFormat, final String defaultValues)
	{
		final String trimmedReferenceFormat = StringUtils.trim(referenceFormat);
		final String trimmedDefaultValues = StringUtils.trim(defaultValues);

		final Pair<String, String> referenceFormatOfRange = RangeParserUtils.parseRangePattern(trimmedReferenceFormat);
		final String referenceFormatOfFrom = referenceFormatOfRange.getLeft();
		final String referenceFormatOfTo = referenceFormatOfRange.getRight();

		String defaultValueOfFrom = StringUtils.EMPTY;
		String defaultValueOfTo = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(trimmedDefaultValues))
		{
			final Pair<String, String> defaultValuesOfRange = RangeParserUtils.parseRangePattern(trimmedDefaultValues);
			defaultValueOfFrom = defaultValuesOfRange.getLeft();
			defaultValueOfTo = defaultValuesOfRange.getRight();
		}

		final ImportParameterParser parser = parserRegistry.getParser(referenceFormatOfFrom);
		final DefaultValues defaultValuesOfFrom = RangeParserUtils.appendPrefixToDefaultValues(
				parser.parseDefaultValues(referenceFormatOfFrom, defaultValueOfFrom), RangeParserUtils.RangeBounds.FROM);
		final DefaultValues defaultValuesOfTo = RangeParserUtils.appendPrefixToDefaultValues(
				parser.parseDefaultValues(referenceFormatOfTo, defaultValueOfTo), RangeParserUtils.RangeBounds.TO);

		return mergeDefaultValues(defaultValuesOfFrom, defaultValuesOfTo);
	}


	protected DefaultValues mergeDefaultValues(final DefaultValues from, final DefaultValues to)
	{
		final Map<String, String> parsedValues = new LinkedHashMap<>(from.toMap());
		parsedValues.putAll(to.toMap());

		final String rangeFormat = "%s" + RangeParserUtils.RANGE_DELIMITER + "%s";
		final String referenceFormat = String.format(rangeFormat, from.getReferenceFormat(), to.getReferenceFormat());
		final String defaultValues = StringUtils.isBlank(from.getDefaultValues()) && StringUtils.isBlank(to.getDefaultValues()) //
				? //
				StringUtils.EMPTY : //
				String.format(rangeFormat, from.getDefaultValues(), to.getDefaultValues());
		return new DefaultValues(defaultValues, referenceFormat, parsedValues);
	}

	/**
	 * Parses referencePattern cell (which is located in the second row) and cell with data (which is located in the data
	 * row) and creates map where key equals to reference key and value equals to value provided in the data row. If
	 * value is null then default value is taken into account. For example, for the following reference cell's value:
	 * {@value RangeParserUtils#RANGE_PREFIX}value1{@value RangeParserUtils#RANGE_DELIMITER}value2{@value RangeParserUtils#RANGE_SUFFIX}
	 * and cell's value:
	 * {@value RangeParserUtils#RANGE_PREFIX}default1{@value RangeParserUtils#RANGE_DELIMITER}default2{@value RangeParserUtils#RANGE_SUFFIX}
	 * the following map will be returned: {{key: {@value RangeParserUtils#RANGE_FROM_PREFIX}value1, value: default1},
	 * {key: {@value RangeParserUtils#RANGE_TO_PREFIX}value2, value: default2}}. Parameters map always contains keys:
	 * {@value RangeParserUtils#RANGE_FROM_PREFIX}{@value ImportParameters#RAW_VALUE} which represents not parsed value,
	 * but merged with default values for the beginning of the range and
	 * {@value RangeParserUtils#RANGE_TO_PREFIX}{@value ImportParameters#RAW_VALUE} for the ending of the range.
	 *
	 * @param cellValue
	 *           value of cell's data
	 * @param defaultValues
	 *           parsed default values
	 * @return ParsedValues object. If default values are not provided then only keys will be returned.
	 */
	@Override
	public ParsedValues parseValue(final String cellValue, final DefaultValues defaultValues)
	{
		if (StringUtils.isBlank(cellValue) && StringUtils.isBlank(defaultValues.getDefaultValues()))
		{
			return new ParsedValues(StringUtils.EMPTY, new ArrayList<>());
		}
		final ParsedValues parsedValuesOfLeft = RangeParserUtils
				.appendPrefixToParsedValues(getLeftParsedValues(cellValue, defaultValues), RangeParserUtils.RangeBounds.FROM);

		final ParsedValues parsedValuesOfRight = RangeParserUtils
				.appendPrefixToParsedValues(getRightParsedValues(cellValue, defaultValues), RangeParserUtils.RangeBounds.TO);

		final List<Map<String, String>> params = ListUtils.union(parsedValuesOfLeft.getParameters(),
				parsedValuesOfRight.getParameters());

		return new ParsedValues(cellValue, params);
	}

	protected ParsedValues getLeftParsedValues(final String cellValue, final DefaultValues defaultValues)
	{
		return getParsedValues(cellValue, defaultValues, RangeParserUtils.RangeBounds.FROM);
	}

	protected ParsedValues getRightParsedValues(final String cellValue, final DefaultValues defaultValues)
	{
		return getParsedValues(cellValue, defaultValues, RangeParserUtils.RangeBounds.TO);
	}

	private ParsedValues getParsedValues(final String cellValue, final DefaultValues defaultValues,
			final RangeParserUtils.RangeBounds rangeType)
	{
		final String prefixToSearchFor = rangeType == RangeParserUtils.RangeBounds.FROM ? RangeParserUtils.RANGE_FROM_PREFIX
				: RangeParserUtils.RANGE_TO_PREFIX;
		final Function<Pair<String, String>, String> getFromOrTo = pair -> rangeType == RangeParserUtils.RangeBounds.FROM
				? pair.getLeft() : pair.getRight();

		final Map<String, String> parsedDefaultValues = defaultValues.toMap() //
				.entrySet() //
				.stream() //
				.filter(entry -> StringUtils.contains(entry.getKey(), prefixToSearchFor)) //
				.collect(LinkedHashMap::new, (newMap, convertedMap) -> newMap.put(convertedMap.getKey(), convertedMap.getValue()),
						LinkedHashMap::putAll);

		final DefaultValues newDefaultValues = new DefaultValues( //
				getFromOrTo.apply(RangeParserUtils.splitByRangeSeparator(defaultValues.getDefaultValues())), //
				getFromOrTo.apply(RangeParserUtils.splitByRangeSeparator(defaultValues.getReferenceFormat())), parsedDefaultValues //
		);

		final UnaryOperator<String> converter = val -> RangeParserUtils.deletePrefix(val, prefixToSearchFor);
		final ImportParameterParser parser = parserRegistry
				.getParser(RangeParserUtils.convert(newDefaultValues.getReferenceFormat(), converter));

		if (StringUtils.contains(cellValue, ExcelTemplateConstants.MULTI_VALUE_DELIMITER))
		{
			final String[] multiCellValue = cellValue.split(ExcelTemplateConstants.MULTI_VALUE_DELIMITER);
			final List<Map<String, String>> parameters = new ArrayList<>();
			for (final String singleCellValue : multiCellValue)
			{
				final ParsedValues singleParsedValues = parser.parseValue( //
						getFromOrTo.apply(RangeParserUtils.parseRangePattern(singleCellValue)), copyDefaultValues(newDefaultValues) //
				);
				parameters.addAll(singleParsedValues.getParameters());
			}
			return new ParsedValues(cellValue, parameters);
		}

		return parser.parseValue( //
				getFromOrTo.apply(RangeParserUtils.parseRangePattern(
						StringUtils.isBlank(cellValue) ? String.format("RANGE[%s]", defaultValues.getDefaultValues()) : cellValue)),
				newDefaultValues //
		);
	}

	private static DefaultValues copyDefaultValues(final DefaultValues defaultValues)
	{
		final Map<String, String> copiedParams = new LinkedHashMap<>(defaultValues.toMap());
		return new DefaultValues(defaultValues.getDefaultValues(), defaultValues.getReferenceFormat(), copiedParams);
	}

	@Required
	public void setParserRegistry(final ParserRegistry parserRegistry)
	{
		this.parserRegistry = parserRegistry;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	// optional
	public void setOrder(final int order)
	{
		this.order = order;
	}

}
