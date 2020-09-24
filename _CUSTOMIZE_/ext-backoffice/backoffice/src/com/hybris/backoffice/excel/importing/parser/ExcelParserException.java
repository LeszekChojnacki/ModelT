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
package com.hybris.backoffice.excel.importing.parser;

/**
 * Exception which can be thrown by {@link ImportParameterParser}
 */
public class ExcelParserException extends RuntimeException
{

	private final String cellValue;
	private final String expectedFormat;

	public ExcelParserException(final String cellValue, final String expectedFormat)
	{
		this.cellValue = cellValue;
		this.expectedFormat = expectedFormat;
	}

	public String getCellValue()
	{
		return cellValue;
	}

	public String getExpectedFormat()
	{
		return expectedFormat;
	}

}
