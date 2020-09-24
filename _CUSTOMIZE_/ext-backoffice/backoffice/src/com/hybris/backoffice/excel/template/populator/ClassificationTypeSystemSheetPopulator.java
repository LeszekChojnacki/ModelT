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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants.ClassificationTypeSystemColumns;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Populates ClassificationTypeSheet with necessary information about classification attributes. To add more information
 * or customize cell values inject your own {@link ExcelClassificationCellPopulator}s via
 * {@link #setCellValuePopulators(Map)}. The populated data can be limited via {@link ExcelFilter}s, please use
 * {@link #setFilters(Collection)} to add your own implementation.
 */
public class ClassificationTypeSystemSheetPopulator implements ExcelSheetPopulator
{
	private ExcelCellService excelCellService;

	private Map<ClassificationTypeSystemColumns, ExcelClassificationCellPopulator> cellValuePopulators;
	private Collection<ExcelFilter<ExcelAttribute>> filters = new LinkedList<>();
	private ClassificationTypeSystemSheetCompressor compressor;

	/**
	 * Populates {@link Workbook}'s ClassificationTypeSystem sheet (will be created if missing) with information about the
	 * Classification attributes.
	 *
	 * @param excelExportResult
	 *           that contains the workbook to populate and export results to be used
	 */
	@Override
	public void populate(@Nonnull final ExcelExportResult excelExportResult)
	{
		final Sheet typeSystemSheet = getOrCreateClassificationTypeSystemSheet(excelExportResult.getWorkbook());
		final Collection<ExcelClassificationAttribute> classificationAttributes = extractClassificationAttributes(
				excelExportResult.getAvailableAdditionalAttributes());
		populate(typeSystemSheet, classificationAttributes);
	}

	protected void populate(final Sheet typeSystemSheet, final Collection<ExcelClassificationAttribute> classificationAttributes)
	{
		final Collection<Map<ClassificationTypeSystemColumns, String>> rows = classificationAttributes.stream() //
				.map(this::applyPopulatorsOnAttribute) //
				.collect(Collectors.toList());

		final Collection<Map<ClassificationTypeSystemColumns, String>> compressedRows = compressor.compress(rows);

		compressedRows.forEach(compressedRow -> {
			final Row lastRow = appendRow(typeSystemSheet);
			compressedRow.forEach((column, cellValue) -> {
				final int columnIndex = column.getIndex();
				excelCellService.insertAttributeValue(lastRow.createCell(columnIndex), cellValue);
			});
		});
	}

	private Map<ClassificationTypeSystemColumns, String> applyPopulatorsOnAttribute(final ExcelClassificationAttribute attribute)
	{
		return cellValuePopulators.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
				entry -> entry.getValue().apply(DefaultExcelAttributeContext.ofExcelAttribute(attribute))));
	}

	protected List<ExcelClassificationAttribute> extractClassificationAttributes(final Collection<ExcelAttribute> attributes)
	{
		return attributes.stream() //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.filter(this::applyAllFilters) //
				.collect(Collectors.toList());
	}

	private boolean applyAllFilters(final ExcelAttribute attribute)
	{
		return filters.stream().allMatch(filter -> filter.test(attribute));
	}

	/**
	 * Gets or creates ClassificationTypeSystem sheet in the passed {@link Workbook}
	 *
	 * @param workbook
	 *           that should contain ClassificationTypeSystem sheet
	 * @return ClassificationTypeSystem sheet
	 */
	protected Sheet getOrCreateClassificationTypeSystemSheet(final @Nonnull Workbook workbook)
	{
		return Optional.ofNullable(workbook.getSheet(ExcelTemplateConstants.UtilitySheet.CLASSIFICATION_TYPE_SYSTEM.getSheetName()))
				.orElseGet(() -> workbook.createSheet(ExcelTemplateConstants.UtilitySheet.CLASSIFICATION_TYPE_SYSTEM.getSheetName()));
	}

	private static Row appendRow(final Sheet typeSystemSheet)
	{
		return typeSystemSheet.createRow(typeSystemSheet.getLastRowNum() + 1);
	}

	@Required
	public void setCellValuePopulators(
			final Map<ClassificationTypeSystemColumns, ExcelClassificationCellPopulator> cellValuePopulators)
	{
		this.cellValuePopulators = cellValuePopulators;
	}

	@Required
	public void setCompressor(final ClassificationTypeSystemSheetCompressor compressor)
	{
		this.compressor = compressor;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}

	// optional
	public void setFilters(final Collection<ExcelFilter<ExcelAttribute>> filters)
	{
		this.filters = filters;
	}
}
