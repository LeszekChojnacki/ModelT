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
package com.hybris.backoffice.excel.exporting;

import de.hybris.platform.core.model.ItemModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.exporting.data.ExcelCellValue;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslator;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslatorRegistry;


/**
 * Abstract class for workbook export decorator. This class is responsible for finding appropriate cell, selected
 * attribute and item which should be exported. Then it uses translator registry which finds appropriate translator and
 * invokes {@link com.hybris.backoffice.excel.translators.ExcelAttributeTranslator#exportData(ExcelAttribute, Object)}.
 * The result of translator is set directly into appropriate cell.
 */
public abstract class AbstractExcelExportWorkbookDecorator implements ExcelExportWorkbookDecorator
{

	private ExcelCellService excelCellService;
	private AttributeNameFormatter<ExcelClassificationAttribute> attributeNameFormatter;
	private ExcelAttributeTranslatorRegistry excelAttributeTranslatorRegistry;

	/**
	 * Decorate method responsible for finding appropriate cell for given attribute and item. Then it finds appropriate
	 * translator for the attribute and fills the cell with data. It also fills the header and reference format rows for
	 * classification attributes.
	 *
	 * @param workbook
	 *           workbook to work on
	 * @param attributes
	 *           collection of attributes to iterate on
	 * @param items
	 *           collection of items to iterate on
	 */
	protected void decorate(final Workbook workbook, final Collection<ExcelClassificationAttribute> attributes,
			final Collection<ItemModel> items)
	{
		final Map<ItemModel, Optional<Row>> rowsCache = new HashMap<>();

		for (final ExcelClassificationAttribute attribute : attributes)
		{
			final String headerValue = getAttributeNameFormatter().format(DefaultExcelAttributeContext.ofExcelAttribute(attribute));
			final Optional<ExcelAttributeTranslator<ExcelAttribute>> translator = getExcelAttributeTranslatorRegistry()
					.findTranslator(attribute);
			final String referenceFormat = translator.isPresent() ? translator.get().referenceFormat(attribute) : StringUtils.EMPTY;

			for (final ItemModel item : items)
			{
				rowsCache.computeIfAbsent(item, key -> findRow(workbook, key)).ifPresent(row -> {
					final Cell headerCell = insertHeaderIfNecessary(row.getSheet(), headerValue);
					final Cell valueCell = createCellIfNecessary(row, headerCell.getColumnIndex());
					insertReferenceFormatIfNecessary(valueCell, referenceFormat);

					final ExcelCellValue excelCellValue = new ExcelCellValue(valueCell, attribute, item);
					translator.ifPresent(t -> exportDataIntoCell(t, excelCellValue));
				});
			}
		}
	}

	protected void exportDataIntoCell(final ExcelAttributeTranslator<ExcelAttribute> translator,
			final ExcelCellValue excelCellValue)
	{
		translator.exportData(excelCellValue.getExcelAttribute(), excelCellValue.getItemModel())
				.ifPresent(value -> getExcelCellService().insertAttributeValue(excelCellValue.getCell(), value));
	}

	protected void insertReferenceFormatIfNecessary(final Cell excelCellValue, final String referenceFormat)
	{
		if (StringUtils.isNotBlank(referenceFormat))
		{
			final Sheet sheet = excelCellValue.getSheet();
			final Row referencePatternRow = sheet.getRow(ExcelTemplateConstants.REFERENCE_PATTERN_ROW_INDEX);
			final int columnIndex = excelCellValue.getColumnIndex();
			Cell referencePatternCell = referencePatternRow.getCell(columnIndex);
			if (referencePatternCell == null)
			{
				referencePatternCell = referencePatternRow.createCell(columnIndex);
			}
			getExcelCellService().insertAttributeValue(referencePatternCell, referenceFormat);
		}
	}

	/**
	 * Inserts new header cell if row does not contain any cell with given content
	 * 
	 * @param sheet
	 *           container of header values
	 * @param headerValue
	 *           value to insert
	 * @return cell with given headerValue
	 */
	protected Cell insertHeaderIfNecessary(final Sheet sheet, final String headerValue)
	{
		final Row headerRow = sheet.getRow(ExcelTemplateConstants.HEADER_ROW_INDEX);
		final int columnIndex = findColumnIndexByContentOrFirstEmptyCell(headerRow, headerValue);
		return createNewHeaderCell(headerRow, columnIndex != -1 ? columnIndex : (headerRow.getLastCellNum() + 1), headerValue);
	}

	/**
	 * Finds column index by comparing content of cells.
	 * 
	 * @param row
	 *           source of the searching
	 * @param content
	 *           content of sought column
	 * @return index of sought column - if column was not found then -1 is returned
	 */
	protected int findColumnIndexByContentOrFirstEmptyCell(final Row row, final String content)
	{
		int emptyCell = -1;
		for (int columnIndex = row.getFirstCellNum(); columnIndex <= row.getLastCellNum(); columnIndex++)
		{
			final String cellValue = getExcelCellService().getCellValue(row.getCell(columnIndex));
			if (cellValue.equals(content))
			{
				return columnIndex;
			}
			if (emptyCell == -1 && StringUtils.isBlank(cellValue))
			{
				emptyCell = columnIndex;
			}
		}
		return emptyCell;
	}

	/**
	 * Creates new header cell and inserts appropriate header value.
	 * 
	 * @param headerRow
	 *           row will be increased by a new column
	 * @param columnIndex
	 *           index of newly created cell
	 * @param headerValue
	 *           value of newly created cell
	 * @return newly created cell
	 */
	protected Cell createNewHeaderCell(final Row headerRow, final int columnIndex, final String headerValue)
	{
		final Cell cell = createCellIfNecessary(headerRow, columnIndex);
		getExcelCellService().insertAttributeValue(cell, headerValue);
		return cell;
	}

	/**
	 * Finds appropriate cell by column index and if cell does not exist then a new cell is created.
	 * 
	 * @param row
	 *           source of the searching
	 * @param columnIndex
	 *           index of sought column
	 * @return cell which is located in row passed as param and in given columnIndex
	 */
	protected Cell createCellIfNecessary(final Row row, final int columnIndex)
	{
		final Cell cell = row.getCell(columnIndex);
		return cell != null ? cell : row.createCell(columnIndex);
	}

	/**
	 * Finds appropriate row based on provided item.
	 * 
	 * @param workbook
	 *           exported workbook
	 * @param item
	 *           provided item
	 * @return found row
	 */
	protected Optional<Row> findRow(final Workbook workbook, final ItemModel item)
	{
		final Sheet pkSheet = workbook.getSheet(ExcelTemplateConstants.UtilitySheet.PK.getSheetName());
		for (int rowIndex = pkSheet.getFirstRowNum(); rowIndex <= pkSheet.getLastRowNum(); rowIndex++)
		{
			final Row row = pkSheet.getRow(rowIndex);
			if (row != null)
			{
				final Cell foundPkCell = row.getCell(ExcelTemplateConstants.PkColumns.PK);
				final String pkAsString = getExcelCellService().getCellValue(foundPkCell);
				if (item.getPk().getLongValueAsString().equals(pkAsString))
				{
					final String foundSheetName = getExcelCellService()
							.getCellValue(row.getCell(ExcelTemplateConstants.PkColumns.SHEET_NAME));
					final String foundRowIndex = getExcelCellService()
							.getCellValue(row.getCell(ExcelTemplateConstants.PkColumns.ROW_INDEX));

					return findRowBySheetNameAndRowIndex(workbook, foundSheetName, foundRowIndex);
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<Row> findRowBySheetNameAndRowIndex(final Workbook workbook, final String foundSheetName,
			final String foundRowIndex)
	{
		final Sheet sheet = workbook.getSheet(foundSheetName);
		return Optional.ofNullable(sheet.getRow(Integer.valueOf(foundRowIndex)));
	}

	public ExcelCellService getExcelCellService()
	{
		return excelCellService;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}

	public AttributeNameFormatter<ExcelClassificationAttribute> getAttributeNameFormatter()
	{
		return attributeNameFormatter;
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelClassificationAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}

	public ExcelAttributeTranslatorRegistry getExcelAttributeTranslatorRegistry()
	{
		return excelAttributeTranslatorRegistry;
	}

	@Required
	public void setExcelAttributeTranslatorRegistry(final ExcelAttributeTranslatorRegistry excelAttributeTranslatorRegistry)
	{
		this.excelAttributeTranslatorRegistry = excelAttributeTranslatorRegistry;
	}
}
