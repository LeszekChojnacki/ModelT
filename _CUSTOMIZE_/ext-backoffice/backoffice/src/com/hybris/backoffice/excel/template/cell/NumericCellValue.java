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
package com.hybris.backoffice.excel.template.cell;

import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * Allows to retrieve cell value of numeric or date types
 */
public class NumericCellValue implements CellValue
{

	private static final Logger LOG = LoggerFactory.getLogger(NumericCellValue.class);

	private ExcelDateUtils excelDateUtils;

	@Override
	public Optional<String> getValue(@Nullable final Cell cell)
	{
		return Optional.ofNullable(getNumericCellValue(cell));
	}

	@Override
	public boolean canHandle(final CellType cellType)
	{
		return cellType == CellType.NUMERIC;
	}

	@Override
	public int getOrder()
	{
		return 20;
	}

	protected String getNumericCellValue(final Cell cell)
	{
		if (isCellDateFormatted(cell))
		{
			final Date formattedDate = getJavaDate(cell);
			return excelDateUtils.exportDate(formattedDate);
		}
		return cell instanceof XSSFCell ? ((XSSFCell) cell).getRawValue() : Double.toString(cell.getNumericCellValue());
	}

	protected boolean isCellDateFormatted(final Cell cell)
	{
		return DateUtil.isCellDateFormatted(cell);
	}

	protected Date getJavaDate(final Cell cell)
	{
		return DateUtil.getJavaDate(cell.getNumericCellValue(), getTimeZone());
	}

	protected TimeZone getTimeZone()
	{
		try
		{
			return TimeZone.getTimeZone(excelDateUtils.getExportTimeZone());
		}
		catch (final Exception ex)
		{
			LOG.warn(String.format("Cannot find timezone for %s code", excelDateUtils.getExportTimeZone()), ex);
			return TimeZone.getDefault();
		}
	}

	public ExcelDateUtils getExcelDateUtils()
	{
		return excelDateUtils;
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}
}
