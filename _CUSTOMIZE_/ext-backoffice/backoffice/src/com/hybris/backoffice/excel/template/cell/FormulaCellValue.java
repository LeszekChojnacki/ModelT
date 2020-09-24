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

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Allows to retrieve cell value which contains excel formula
 */
public class FormulaCellValue implements CellValue
{
	@Override
	public Optional<String> getValue(@Nullable final Cell cell)
	{
		return Optional.ofNullable(cell) //
				.map(Cell::getRow) //
				.map(Row::getSheet) //
				.map(Sheet::getWorkbook) //
				.map(Workbook::getCreationHelper) //
				.map(CreationHelper::createFormulaEvaluator) //
				.map(c -> c.evaluate(cell)) //
				.map(org.apache.poi.ss.usermodel.CellValue::getStringValue);
	}

	@Override
	public boolean canHandle(final CellType cellType)
	{
		return cellType == CellType.FORMULA;
	}

	@Override
	public int getOrder()
	{
		return 10;
	}
}
