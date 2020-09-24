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
package com.hybris.backoffice.excel.validators.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Util class responsible for merging validation messages. When list of {@link ExcelValidationResult} contains results
 * which belong to the same rows, then such {@link ExcelValidationResult} are merged - all messages are put to one
 * {@link ExcelValidationResult} and common validation header is created. This class also adds appropriate headers and
 * metadata to excel validation result if needed.
 */
public class ExcelValidationResultUtil
{

	private ExcelValidationResultUtil()
	{

	}

	/**
	 * Creates validation header if given {@link ExcelValidationResult} does not have it. Moreover this methods populates
	 * metadata information about given validation result, for example: rowIndex, typeCode and attribute name.
	 * 
	 * @param singleResult
	 * @param rowIndex
	 * @param typeCode
	 * @param attributeName
	 * @return
	 */
	public static void insertHeaderIfNeeded(final ExcelValidationResult singleResult, final int rowIndex, final String typeCode,
			final String attributeName)
	{
		singleResult.getValidationErrors()
				.forEach(validationError -> populateHeaderMetadata(validationError, rowIndex, typeCode, attributeName));
		if (singleResult.getHeader() == null)
		{
			singleResult.setHeader(prepareValidationHeader(rowIndex, typeCode, attributeName));
		}
	}

	private static ValidationMessage prepareValidationHeader(final int rowIndex, final String typeCode, final String attributeName)
	{
		final ValidationMessage header = new ValidationMessage("excel.import.validation.header.title", typeCode, rowIndex);
		ExcelValidationResultUtil.populateHeaderMetadata(header, rowIndex, typeCode, attributeName);
		return header;
	}

	private static void populateHeaderMetadata(final ValidationMessage header, final int rowIndex, final String typeCode,
			final String attributeName)
	{
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY, rowIndex);
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY, typeCode);
		header.addMetadataIfAbsent(ExcelTemplateConstants.ValidationMessageMetadata.SELECTED_ATTRIBUTE_DISPLAYED_NAME_KEY,
				attributeName);
	}

	/**
	 * Finds {@link ExcelValidationResult} which belong to the same row and creates new {@link ExcelValidationResult} which
	 * consists of merged {@link ValidationMessage} and common header. This method returns new merged list of
	 * {@link ExcelValidationResult}.
	 * 
	 * @param resultsToMerge
	 * @return returns new merged list of {@link ExcelValidationResult}
	 */
	public static List<ExcelValidationResult> mergeValidationResults(final List<ExcelValidationResult> resultsToMerge)
	{
		final List<ExcelValidationResult> result = new ArrayList<>(findResultsWithoutRows(resultsToMerge));
		final Map<Pair<String, Integer>, List<ExcelValidationResult>> groupedResults = groupByTypeCodeAndRowIndex(resultsToMerge);

		for (final List<ExcelValidationResult> resultsForRow : groupedResults.values())
		{
			result.add(mergeResultsFromTheSameRow(resultsForRow));
		}

		return result;
	}

	private static Map<Pair<String, Integer>, List<ExcelValidationResult>> groupByTypeCodeAndRowIndex(
			final List<ExcelValidationResult> resultsToMerge)
	{
		final Map<Pair<String, Integer>, List<ExcelValidationResult>> groups = new LinkedHashMap<>();
		for (final ExcelValidationResult result : resultsToMerge)
		{
			final ValidationMessage header = result.getHeader();
			if (header != null)
			{
				groupByTypeCodeAndRowIndex(groups, result, header);
			}
		}
		return groups;
	}

	/**
	 * Allows to merge the list of errors from {@link ExcelValidationResult} to single {@link ExcelValidationResult}.
	 * 
	 * @param results
	 *           to merge
	 * @return merged result
	 */
	public static ExcelValidationResult mergeExcelValidationResults(final @Nonnull Collection<ExcelValidationResult> results)
	{
		ValidationMessage header = null;
		if (CollectionUtils.isNotEmpty(results))
		{
			header = new ArrayList<>(results).get(0).getHeader();
		}

		return new ExcelValidationResult(header, new ArrayList<>(mapExcelResultsToValidationMessages(results)));
	}

	/**
	 * Allows to retrieve all {@link ValidationMessage}s from given {@link ExcelValidationResult}s and returns the flat
	 * structure.
	 *
	 * @param results
	 *           to map
	 * @return collection of {@link ValidationMessage}
	 */
	public static Collection<ValidationMessage> mapExcelResultsToValidationMessages(
			final @Nonnull Collection<ExcelValidationResult> results)
	{
		return results.stream() //
				.map(ExcelValidationResult::getValidationErrors) //
				.flatMap(Collection::stream) //
				.distinct() //
				.collect(Collectors.toList());
	}

	private static void groupByTypeCodeAndRowIndex(final Map<Pair<String, Integer>, List<ExcelValidationResult>> groups,
			final ExcelValidationResult result, final ValidationMessage header)
	{
		final Object sheetNameValue = header.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY);
		final Object rowIndexValue = header.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY);
		if (sheetNameValue != null && rowIndexValue != null)
		{
			final String typeCode = sheetNameValue.toString();
			final int rowIndex = (int) rowIndexValue;
			final Pair<String, Integer> key = Pair.of(typeCode, rowIndex);
			groups.putIfAbsent(key, new ArrayList<>());
			final List<ExcelValidationResult> validationResults = groups.get(key);
			validationResults.add(result);
		}
	}

	private static List<ExcelValidationResult> findResultsWithoutRows(final List<ExcelValidationResult> allResults)
	{
		return allResults.stream().filter(result -> result.getHeader() != null)
				.filter(
						result -> !result.getHeader().containsMetadata(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY))
				.collect(Collectors.toList());
	}

	private static ExcelValidationResult mergeResultsFromTheSameRow(final List<ExcelValidationResult> results)
	{
		final List<ValidationMessage> mergedValidationMessages = new ArrayList<>();
		final ExcelValidationResult mergedResult = new ExcelValidationResult(mergedValidationMessages);
		if (CollectionUtils.isNotEmpty(results))
		{
			final ExcelValidationResult firstResult = results.get(0);
			mergedResult.setHeader(firstResult.getHeader());
			for (final ExcelValidationResult result : results)
			{
				mergedValidationMessages.addAll(result.getValidationErrors());
			}
		}
		return mergedResult;
	}
}
