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
import org.apache.poi.ss.usermodel.DataFormatter;


/**
 * Allows to retrieve cell value using {@link DataFormatter}
 */
public class DataCellValue implements CellValue
{

	@Override
	public Optional<String> getValue(@Nullable final Cell cell)
	{
		return Optional.ofNullable(new DataFormatter().formatCellValue(cell));
	}

	@Override
	public boolean canHandle(final CellType cellType)
	{
		return true;
	}

	@Override
	public int getOrder()
	{
		return Integer.MAX_VALUE - 10;
	}

}
