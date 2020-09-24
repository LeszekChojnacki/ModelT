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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for max row. The validator checks whether total number of rows is less than max row indicated
 * in 'backoffice.excel.import.max.rows' property.
 */
public class WorkbookMaxRowValidator implements WorkbookValidator
{

	public static final String BACKOFFICE_EXCEL_IMPORT_MAX_ROWS_PROPERTY_KEY = "backoffice.excel.import.max.rows";
	private static final String VALIDATION_MESSAGE_HEADER = "excel.import.validation.max.row.exceeded.header";
	private static final String VALIDATION_MESSAGE_DESCRIPTION = "excel.import.validation.max.row.exceeded.description";
	public static final int DEFAULT_MAX_ROW = 2000;
	public static final int FIRST_DATA_ROW_INDEX = 3;
	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	private ExcelTemplateService excelTemplateService;
	private ExcelSheetService excelSheetService;
	private ExcelCellService excelCellService;

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		final Collection<Sheet> sheets = getExcelSheetService().getSheets(workbook);

		int numberOfRows = 0;
		for (final Sheet sheet : sheets)
		{
			numberOfRows += getNumberOfCorrectRows(sheet);
		}
		return prepareResult(numberOfRows);
	}

	protected Integer getMaxRow()
	{
		return Config.getInt(BACKOFFICE_EXCEL_IMPORT_MAX_ROWS_PROPERTY_KEY, DEFAULT_MAX_ROW);
	}

	protected List<ExcelValidationResult> prepareResult(final int totalNumberOfRows)
	{
		final Integer maxRow = getMaxRow();
		if (totalNumberOfRows <= maxRow)
		{
			return new ArrayList<>();
		}
		final ValidationMessage header = new ValidationMessage(VALIDATION_MESSAGE_HEADER);
		final ExcelValidationResult validationResult = new ExcelValidationResult(
				new ValidationMessage(VALIDATION_MESSAGE_DESCRIPTION, maxRow, totalNumberOfRows));
		validationResult.setHeader(header);
		return Collections.singletonList(validationResult);
	}

	protected int getNumberOfCorrectRows(final Sheet sheet)
	{
		int count = 0;
		for (int i = FIRST_DATA_ROW_INDEX; i <= sheet.getLastRowNum(); i++)
		{
			if (isRowCorrect(sheet.getRow(i)))
			{
				count++;
			}
		}
		return count;
	}

	protected boolean isRowCorrect(final Row row)
	{
		if (row != null)
		{
			for (int i = 0; i < row.getLastCellNum(); i++)
			{
				if (StringUtils.isNotBlank(getExcelCellService().getCellValue(row.getCell(i))))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelTemplateService getExcelTemplateService()
	{
		return excelTemplateService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setExcelTemplateService(final ExcelTemplateService excelTemplateService)
	{
		this.excelTemplateService = excelTemplateService;
	}

	public ExcelSheetService getExcelSheetService()
	{
		return excelSheetService;
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
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
}
