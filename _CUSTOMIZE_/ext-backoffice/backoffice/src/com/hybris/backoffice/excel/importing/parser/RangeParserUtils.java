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

import de.hybris.platform.catalog.enums.ClassificationAttributeTypeEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


public class RangeParserUtils
{

	public static final String RANGE_DELIMITER = ";";
	public static final String RANGE_PREFIX = "RANGE[";
	public static final String RANGE_SUFFIX = "]";

	public static final String RANGE_FROM_PREFIX = "from$";
	public static final String RANGE_TO_PREFIX = "to$";

	public static final String COMPLEX_TYPE_RANGE_FORMAT = RANGE_PREFIX + "%1$s" + RANGE_DELIMITER + "%1$s" + RANGE_SUFFIX;
	public static final String SIMPLE_TYPE_RANGE_FORMAT = RANGE_PREFIX + "value" + RANGE_DELIMITER + "value" + RANGE_SUFFIX;

	/**
	 * e.g. RANGE[from;to]
	 */
	public static final Pattern RANGE_PATTERN = Pattern.compile("RANGE\\[(?<from>.*);(?<to>.*)]");

	private RangeParserUtils()
	{
	}

	/**
	 * Parses input to pair of values - beginning of the range and end of the range. The input should be in format
	 * {@value RANGE_PREFIX}from{@value RANGE_DELIMITER}to{@value RANGE_SUFFIX}. <br/>
	 * .e.g input {@value RANGE_PREFIX}from{@value RANGE_DELIMITER}to{@value RANGE_SUFFIX} returns {@link Pair} which
	 * contains "from" and "to".
	 *
	 * @param input
	 *           range raw value
	 * @return pair which contains beginning and ending of the range. The {@link Pair#getLeft()} returns "from" of the
	 *         range and {@link Pair#getRight()} returns "to" of the range.
	 * @throws ExcelParserException
	 *            when input doesn't match to {@link #RANGE_PATTERN}
	 */
	public static Pair<String, String> parseRangePattern(@Nonnull final String input) throws ExcelParserException
	{
		final Matcher matcher = RANGE_PATTERN.matcher(input);
		if (!matcher.matches())
		{
			throw new ExcelParserException(input, RANGE_PATTERN.pattern());
		}

		final String from = matcher.group("from");
		final String to = matcher.group("to");
		return ImmutablePair.of(from, to);
	}

	/**
	 * Splits given input by {@value RANGE_DELIMITER}. <br/>
	 * e.g. from{@value RANGE_DELIMITER} returns {@link Pair} which contains "from" and "to".
	 *
	 * @param input
	 *           value to split
	 * @return pair which contains beginning and ending of the range. The {@link Pair#getLeft()} returns "from" of the
	 *         range and {@link Pair#getRight()} returns "to" of the range.
	 */
	public static Pair<String, String> splitByRangeSeparator(@Nonnull final String input) throws ExcelParserException
	{
		final String[] arr = input.split(RANGE_DELIMITER);
		return ImmutablePair.of(arr[0], arr.length == 2 ? arr[1] : StringUtils.EMPTY);
	}

	/**
	 * Prepends {@value RANGE_FROM_PREFIX} to given input. <br/>
	 * e.g. "input" -> "{@value RANGE_FROM_PREFIX}input"
	 *
	 * @param input
	 *           value which will be prepended to {@value RANGE_FROM_PREFIX}
	 * @return input with {@value RANGE_FROM_PREFIX}
	 */
	public static String prependFromPrefix(@Nonnull final String input)
	{
		return RANGE_FROM_PREFIX + input;
	}

	/**
	 * Prepends {@value RANGE_TO_PREFIX} to given input. <br/>
	 * e.g. "input" -> "{@value RANGE_TO_PREFIX}input"
	 *
	 * @param input
	 *           value which will be prepended to {@value RANGE_FROM_PREFIX}
	 * @return input with {@value RANGE_FROM_PREFIX}
	 */
	public static String prependToPrefix(@Nonnull final String input)
	{
		return RANGE_TO_PREFIX + input;
	}

	/**
	 * Deletes {@value #RANGE_FROM_PREFIX} from given input
	 *
	 * @param input
	 *           to modify
	 * @return value without {@value #RANGE_FROM_PREFIX}
	 */
	public static String deleteFromPrefix(@Nonnull final String input)
	{
		return deletePrefix(input, RANGE_FROM_PREFIX);
	}

	/**
	 * Deletes {@value #RANGE_TO_PREFIX} from given input
	 *
	 * @param input
	 *           to modify
	 * @return value without {@value #RANGE_TO_PREFIX}
	 */
	public static String deleteToPrefix(@Nonnull final String input)
	{
		return deletePrefix(input, RANGE_TO_PREFIX);
	}

	/**
	 * Deletes given prefix from given input
	 *
	 * @param input
	 *           to modify
	 * @param prefix
	 *           to remove
	 * @return value without given prefix
	 */
	public static String deletePrefix(final String input, final String prefix)
	{
		return input.substring(prefix.length());
	}

	/**
	 * Deletes {@value #RANGE_FROM_PREFIX} or {@value #RANGE_TO_PREFIX} from {@link ImportParameters}.
	 *
	 * @param importParameters
	 *           to modify
	 * @param rangeBounds
	 *           allows to decide which prefix should be handled
	 * @return value without given prefix
	 */
	public static ImportParameters deletePrefixFromImportParameters(final ImportParameters importParameters,
			final RangeBounds rangeBounds)
	{
		final List<Map<String, String>> newParams = importParameters.getMultiValueParameters().stream()
				.map(params -> deletePrefixFromMapKey(params, rangeBounds)).filter(MapUtils::isNotEmpty).collect(Collectors.toList());
		return new ImportParameters(importParameters.getTypeCode(), importParameters.getIsoCode(), importParameters.getCellValue(),
				importParameters.getEntryRef(), newParams);
	}

	/**
	 * Every range contains of FROM and TO values. This method allows to retrieve {@link ImportParameters} from one of
	 * given bounds.
	 *
	 * @param excelClassificationAttribute
	 *           it is necessary, because this method must check whether the type is complex or simple. If it's simple
	 *           then {@link ImportParameters#cellValue} will be completed with {@value ImportParameters#RAW_VALUE}.
	 * @param importParameters
	 *           to work on
	 * @param params
	 *           to be put in {@link ImportParameters#parameters}
	 * @param rangeBounds
	 *           allows to decide whether retrieve FROM or TO bound
	 * @return importParameters of given bound
	 */
	public static ImportParameters getSingleImportParameters(final ExcelClassificationAttribute excelClassificationAttribute,
			final ImportParameters importParameters, final Map<String, String> params,
			final RangeParserUtils.RangeBounds rangeBounds)
	{
		final boolean isSimpleType = excelClassificationAttribute.getAttributeAssignment()
				.getAttributeType() != ClassificationAttributeTypeEnum.REFERENCE;

		final Supplier<String> complexCellValue = () -> {
			final Pair<String, String> pair = RangeParserUtils.parseRangePattern((String) importParameters.getCellValue());
			return rangeBounds == RangeParserUtils.RangeBounds.FROM ? pair.getLeft() : pair.getRight();
		};

		final Supplier<String> rawValueKey = () -> rangeBounds == RangeParserUtils.RangeBounds.FROM
				? RangeParserUtils.prependFromPrefix(ImportParameters.RAW_VALUE)
				: RangeParserUtils.prependToPrefix(ImportParameters.RAW_VALUE);

		final String cellValue = isSimpleType ? params.get(rawValueKey.get()) : complexCellValue.get();
		return RangeParserUtils.deletePrefixFromImportParameters(new ImportParameters(importParameters.getTypeCode(),
				importParameters.getIsoCode(), cellValue, importParameters.getEntryRef(), Lists.newArrayList(params)), rangeBounds);
	}

	/**
	 * Appends {@value #RANGE_FROM_PREFIX} or {@value #RANGE_TO_PREFIX} to {@link ParsedValues}.
	 *
	 * @param parsedValues
	 *           to modify
	 * @param range
	 *           allows to decide which prefix should be handled
	 * @return value with given prefix
	 */
	public static ParsedValues appendPrefixToParsedValues(final ParsedValues parsedValues, final RangeBounds range)
	{
		final UnaryOperator<String> converter = val -> appendPrefixToInput(val, range);
		return handlePrefixOfParsedValues(parsedValues, converter);
	}

	/**
	 * Deletes {@value #RANGE_FROM_PREFIX} or {@value #RANGE_TO_PREFIX} from {@link ParsedValues}.
	 *
	 * @param parsedValues
	 *           to modify
	 * @param range
	 *           allows to decide which prefix should be handled
	 * @return value without given prefix
	 */
	public static ParsedValues deletePrefixFromParsedValues(final ParsedValues parsedValues, final RangeBounds range)
	{
		final UnaryOperator<String> converter = val -> deletePrefixFromInput(val, range);
		return handlePrefixOfParsedValues(parsedValues, converter);
	}

	private static ParsedValues handlePrefixOfParsedValues(final ParsedValues parsedValues, final UnaryOperator<String> converter)
	{
		final List<Map<String, String>> newParameters = parsedValues.getParameters().stream()
				.filter(map -> map.containsKey(ImportParameters.RAW_VALUE)).peek(peek -> {
					final String rawValue = peek.get(ImportParameters.RAW_VALUE);
					peek.remove(ImportParameters.RAW_VALUE);
					final String rawKey = converter.apply(ImportParameters.RAW_VALUE);
					peek.put(rawKey, rawValue);
				}).collect(Collectors.toList());
		return new ParsedValues(parsedValues.getCellValue(), newParameters);
	}

	/**
	 * Appends {@value #RANGE_FROM_PREFIX} or {@value #RANGE_TO_PREFIX} to {@link DefaultValues}.
	 *
	 * @param defaultValues
	 *           to modify
	 * @param range
	 *           allows to decide which prefix should be handled
	 * @return value with given prefix
	 */
	public static DefaultValues appendPrefixToDefaultValues(final DefaultValues defaultValues, final RangeBounds range)
	{
		final UnaryOperator<String> converter = val -> appendPrefixToInput(val, range);
		return handlePrefixForDefaultValues(defaultValues, converter);
	}

	/**
	 * Deletes {@value #RANGE_FROM_PREFIX} or {@value #RANGE_TO_PREFIX} from {@link DefaultValues}.
	 *
	 * @param defaultValues
	 *           to modify
	 * @param range
	 *           allows to decide which prefix should be handled
	 * @return value without given prefix
	 */
	public static DefaultValues deletePrefixFromDefaultValues(final DefaultValues defaultValues, final RangeBounds range)
	{
		final UnaryOperator<String> converter = val -> deletePrefixFromInput(val, range);
		return handlePrefixForDefaultValues(defaultValues, converter);
	}

	private static DefaultValues handlePrefixForDefaultValues(final DefaultValues defaultValues,
			final UnaryOperator<String> converter)
	{
		final String newReferenceFormat = converter.apply(defaultValues.getReferenceFormat());
		final Map<String, String> newParsedParameters = defaultValues.toMap().entrySet().stream().collect(LinkedHashMap::new,
				(newMap, convertedMap) -> newMap.put(converter.apply(convertedMap.getKey()), convertedMap.getValue()),
				LinkedHashMap::putAll);
		return new DefaultValues(defaultValues.getDefaultValues(), newReferenceFormat, newParsedParameters);

	}

	private static String appendPrefixToInput(final String input, final RangeBounds range)
	{
		final UnaryOperator<String> converter = val -> range == RangeBounds.FROM ? RangeParserUtils.prependFromPrefix(val)
				: RangeParserUtils.prependToPrefix(val);

		return convert(input, converter);
	}

	private static String deletePrefixFromInput(final String input, final RangeBounds rangeBounds)
	{
		final UnaryOperator<String> converter = val -> rangeBounds == RangeBounds.FROM ? RangeParserUtils.deleteFromPrefix(val)
				: RangeParserUtils.deleteToPrefix(val);

		return convert(input, converter);
	}

	/**
	 * Allows to convert given input with given converter. If input contains
	 * {@link SelectedAttribute#REFERENCE_PATTERN_SEPARATOR} then it is split using this separator and all of the
	 * subelements are converted and then merged.<br/>
	 * E.g. input "a:b:c" and converter which appends {@value #RANGE_FROM_PREFIX} then
	 * "{@value #RANGE_TO_PREFIX}a:{@value #RANGE_TO_PREFIX}b:{@value #RANGE_TO_PREFIX}c" will be returned
	 *
	 * @param input
	 *           to modify
	 * @param converter
	 *           to use for conversion
	 * @return converted input
	 */
	public static String convert(final String input, final UnaryOperator<String> converter)
	{
		if (StringUtils.isBlank(input))
		{
			return input;
		}

		return Stream.of(input.split(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR)).map(converter)
				.collect(Collectors.joining(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR));
	}

	private static Map<String, String> deletePrefixFromMapKey(final Map<String, String> map, final RangeBounds rangeBounds)
	{
		return map.entrySet() //
				.stream() //
				.filter(entry -> containsPrefix(entry.getKey(), rangeBounds)) //
				.collect( //
						LinkedHashMap::new, //
						(newMap, currentMap) -> newMap.put(deletePrefixFromInput(currentMap.getKey(), rangeBounds), //
								currentMap.getValue()), //
						LinkedHashMap::putAll //
		);
	}

	private static boolean containsPrefix(final String input, final RangeBounds rangeBounds)
	{
		final String prefix = rangeBounds == RangeBounds.FROM ? RANGE_FROM_PREFIX : RANGE_TO_PREFIX;
		return StringUtils.contains(input, prefix);
	}

	public enum RangeBounds
	{
		FROM, TO
	}
}
