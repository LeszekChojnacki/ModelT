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
package com.hybris.backoffice.excel.template;

import org.apache.poi.ss.usermodel.Workbook;


public class DefaultExcelSheetNamingStrategy implements ExcelSheetNamingStrategy
{
	protected static final int MAX_LENGTH_SHEET_NAME = 31;
	protected static final String SHEET_NUMBER_SEPARATOR = "_";

	@Override
	public String generateName(final Workbook workbook, final String typeCode)
	{
		if (typeCode.length() <= MAX_LENGTH_SHEET_NAME && !hasSheetForName(workbook, typeCode))
		{
			return typeCode;
		}
		return generateSheetName(workbook, typeCode);
	}

	protected String generateSheetName(final Workbook workbook, final String typeCode)
	{
		int counter = 1;
		while (true)
		{
			final String suffix = SHEET_NUMBER_SEPARATOR.concat(String.valueOf(counter));
			final String truncatedName = typeCode.substring(0, getEndOfOriginalName(typeCode, suffix)).concat(suffix);
			if (!hasSheetForName(workbook, truncatedName))
			{
				return truncatedName;
			}
			counter++;
		}
	}

	protected int getEndOfOriginalName(final String typeCode, final String suffix)
	{
		final int nameWithSuffix = typeCode.length() + suffix.length();
		if (nameWithSuffix <= MAX_LENGTH_SHEET_NAME)
		{
			return typeCode.length();
		}
		else
		{
			final int overflowSize = nameWithSuffix - MAX_LENGTH_SHEET_NAME;
			return nameWithSuffix - overflowSize - suffix.length();
		}
	}

	protected boolean hasSheetForName(final Workbook workbook, final String typeCode)
	{
		return workbook.getSheet(typeCode) != null;
	}

}
