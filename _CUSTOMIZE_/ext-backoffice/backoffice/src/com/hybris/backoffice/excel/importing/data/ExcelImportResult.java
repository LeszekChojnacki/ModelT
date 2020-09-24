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
package com.hybris.backoffice.excel.importing.data;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.Impex;


/**
 * Represents excel import result. The result consists of excel workbook and current generated impex script
 */
public class ExcelImportResult
{
	private final Workbook workbook;
	private final Impex impex;

	public ExcelImportResult(final Workbook workbook, final Impex impex)
	{
		this.workbook = workbook;
		this.impex = impex;
	}

	public Workbook getWorkbook()
	{
		return workbook;
	}

	public Impex getImpex()
	{
		return impex;
	}
}
