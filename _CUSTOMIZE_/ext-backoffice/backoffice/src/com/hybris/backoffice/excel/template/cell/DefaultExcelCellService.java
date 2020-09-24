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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;


/**
 * Default implementation of {@link ExcelCellService}
 */
public class DefaultExcelCellService implements ExcelCellService
{
	private static final List<Character> formulaChars = Arrays.asList('+', '-', '=', '@', '|', '%');
	private static final char ESCAPE_CHAR = '\'';

	private List<CellValue> cellValues;

	@Override
	public @Nonnull String getCellValue(@Nullable final Cell cell)
	{
		final CellValue emptyCell = anyCell -> Optional.empty();

		return cell == null ? //
				StringUtils.EMPTY
				: getCellValues()//
						.stream()//
						.filter(cellValue -> cellValue.canHandle(cell.getCellTypeEnum()))//
						.findFirst()//
						.orElse(emptyCell)//
						.getValue(cell)//
						.map(String::trim)//
						.map(this::escapeImportFormula)//
						.orElse(StringUtils.EMPTY);
	}

	@Override
	public void insertAttributeValue(@Nonnull final Cell cell, @Nullable final Object value)
	{
		final String valueToInsert = value != null ? escapeExportFormula(value.toString().trim()) : null;
		cell.setCellValue(valueToInsert);
	}

	protected String escapeExportFormula(final String value)
	{
		final Predicate<String> isNotEmpty = StringUtils::isNotEmpty;
		final Predicate<String> startsWithFormulaChar = val -> formulaChars.contains(val.charAt(0));

		return isNotEmpty.and(startsWithFormulaChar).test(value) ? (ESCAPE_CHAR + value) : value;
	}

	protected String escapeImportFormula(final String value)
	{
		final Predicate<String> hasAtLeastTwoChars = val -> val.length() > 1;
		final Predicate<String> beginsWithEscapeChar = val -> val.charAt(0) == ESCAPE_CHAR;
		final Predicate<String> secondCharIsFormulaChar = val -> formulaChars.contains(val.charAt(1));

		return hasAtLeastTwoChars.and(beginsWithEscapeChar).and(secondCharIsFormulaChar).test(value) ? value.substring(1) : value;
	}

	public Collection<CellValue> getCellValues()
	{
		return cellValues;
	}

	@Required
	public void setCellValues(final List<CellValue> cellValues)
	{
		if (cellValues != null)
		{
			OrderComparator.sort(cellValues);
		}
		this.cellValues = cellValues;
	}
}
