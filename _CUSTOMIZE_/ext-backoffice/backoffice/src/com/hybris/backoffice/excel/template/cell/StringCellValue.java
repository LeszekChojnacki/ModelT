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


/**
 * Allows to retrieve cell value of String type
 */
public class StringCellValue implements CellValue
{

	@Override
	public Optional<String> getValue(@Nullable final Cell cell)
	{
		return Optional.ofNullable(cell).map(Cell::getStringCellValue);
	}

	@Override
	public boolean canHandle(final CellType cellType)
	{
		return cellType == CellType.STRING;
	}

	@Override
	public int getOrder()
	{
		return 0;
	}
}
