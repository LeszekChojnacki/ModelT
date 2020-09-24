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
package com.hybris.backoffice.excel.template.populator;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Compresses ClassificationTypeSystem rows by classification attribute. Some column values (as in
 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.ClassificationTypeSystemColumns#FULL_NAME
 * FULL_NAME} and
 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.ClassificationTypeSystemColumns#ATTRIBUTE_LOC_LANG
 * ATTRIBUTE_LOC_LANG}) differ for each row, thus they will be merged to a single cell containing a collection of those
 * elements using {@link CollectionFormatter}. <br>
 * <br>
 * For example the values in column:
 * 
 * <pre>
 * +------------------+
 * | AttributeLocLang |
 * +------------------+
 * | en               |
 * | de               |
 * | fr               |
 * +------------------+
 * </pre>
 * 
 * will be compressed to:
 * 
 * <pre>
 * +------------------+
 * | AttributeLocLang |
 * +------------------+
 * | {en},{de},{fr}   |
 * +------------------+
 * </pre>
 * 
 * @see CollectionFormatter#formatToString(String...)
 */
public class ClassificationTypeSystemSheetCompressor
{
	protected static final String MERGING_SYMBOL = "#&#";
	protected static final BiFunction<String, String, String> MERGING_STRATEGY = (left, right) -> left + MERGING_SYMBOL + right;

	private CollectionFormatter collectionFormatter;

	public Collection<Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String>> //
			compress(final @Nonnull Collection<Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String>> rows)
	{
		return rows.stream() //
				.collect(Collectors.toMap( //
						ClassificationTypeSystemSheetCompressor::groupByClassificationAttribute, // key mapper
						Function.identity(), // value mapper
						ClassificationTypeSystemSheetCompressor::mergeRows)) // merging function
				.values() //
				.stream() //
				.map(this::mapRowToCollectionFormat) //
				.collect(Collectors.toList());
	}

	private static String groupByClassificationAttribute(
			final Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> row)
	{
		return row.get(ExcelTemplateConstants.ClassificationTypeSystemColumns.CLASSIFICATION_ATTRIBUTE);
	}

	private Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> //
			mapRowToCollectionFormat(final Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> row)
	{
		final Function<String, String> collectionFormattingMapper = cellValue -> {
			if (cellValue.contains(MERGING_SYMBOL))
			{
				return collectionFormatter.formatToString(cellValue.split(MERGING_SYMBOL));
			}
			return cellValue;
		};
		return row.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> collectionFormattingMapper.apply(entry.getValue())));
	}

	private static Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> mergeRows(
			final Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> left,
			final Map<ExcelTemplateConstants.ClassificationTypeSystemColumns, String> right)
	{
		left.merge(ExcelTemplateConstants.ClassificationTypeSystemColumns.FULL_NAME,
				right.get(ExcelTemplateConstants.ClassificationTypeSystemColumns.FULL_NAME), MERGING_STRATEGY);
		left.merge(ExcelTemplateConstants.ClassificationTypeSystemColumns.ATTRIBUTE_LOC_LANG,
				right.get(ExcelTemplateConstants.ClassificationTypeSystemColumns.ATTRIBUTE_LOC_LANG), MERGING_STRATEGY);
		return left;
	}

	@Required
	public void setCollectionFormatter(final CollectionFormatter collectionFormatter)
	{
		this.collectionFormatter = collectionFormatter;
	}
}
