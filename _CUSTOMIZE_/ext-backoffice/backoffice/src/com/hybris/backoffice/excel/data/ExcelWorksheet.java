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
package com.hybris.backoffice.excel.data;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;


public class ExcelWorksheet
{

	private final String sheetName;
	private final Table<Integer, ExcelColumn, ImportParameters> table;

	public ExcelWorksheet(final String sheetName)
	{
		this.sheetName = sheetName;
		this.table = HashBasedTable.create();
	}

	public void forEachColumn(final Consumer<ExcelColumn> consumer)
	{
		getTable().columnKeySet().forEach(consumer::accept);
	}

	public void forEachRow(final ExcelColumn excelColumn, final BiConsumer<Integer, ImportParameters> biConsumer)
	{
		getTable().column(excelColumn).forEach(biConsumer::accept);
	}

	public String getSheetName()
	{
		return sheetName;
	}

	public Table<Integer, ExcelColumn, ImportParameters> getTable()
	{
		return ImmutableTable.copyOf(table);
	}

	public void add(final int rowIndex, final ExcelColumn column, final ImportParameters value)
	{
		table.put(rowIndex, column, value);
	}

}
