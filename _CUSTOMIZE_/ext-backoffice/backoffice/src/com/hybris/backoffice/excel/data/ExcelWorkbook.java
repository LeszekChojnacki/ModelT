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

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


public class ExcelWorkbook
{

	private final Set<ExcelWorksheet> worksheets;

	public ExcelWorkbook(final ExcelWorksheet... excelWorksheets)
	{
		this.worksheets = Sets.newHashSet(excelWorksheets);
	}

	public void add(final ExcelWorksheet excelWorksheet)
	{
		this.worksheets.add(excelWorksheet);
	}

	public Set<ExcelWorksheet> getWorksheets()
	{
		return ImmutableSet.copyOf(worksheets);
	}

	public void forEachWorksheet(final Consumer<ExcelWorksheet> consumer)
	{
		worksheets.forEach(consumer::accept);
	}
	
}
