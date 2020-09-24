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
package com.hybris.backoffice.excel.exporting.data;

import de.hybris.platform.core.model.ItemModel;

import org.apache.poi.ss.usermodel.Cell;

import com.hybris.backoffice.excel.data.ExcelAttribute;


public class ExcelCellValue
{
	private final Cell cell;
	private final ExcelAttribute excelAttribute;
	private final ItemModel itemModel;

	public ExcelCellValue(final Cell cell, final ExcelAttribute excelAttribute, final ItemModel itemModel)
	{
		this.cell = cell;
		this.excelAttribute = excelAttribute;
		this.itemModel = itemModel;
	}

	public Cell getCell()
	{
		return cell;
	}

	public ExcelAttribute getExcelAttribute()
	{
		return excelAttribute;
	}

	public ItemModel getItemModel()
	{
		return itemModel;
	}
}
