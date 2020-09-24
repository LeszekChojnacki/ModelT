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
package com.hybris.backoffice.excel.importing.parser.matcher;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * Given matcher returns true if the input is a date's format.
 */
public class DateExcelParserMatcher implements ExcelParserMatcher
{

	private ExcelDateUtils excelDateUtils;

	@Override
	public boolean test(final @Nonnull String input)
	{
		return StringUtils.equals(input, excelDateUtils.getDateTimeFormat());
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}
}
