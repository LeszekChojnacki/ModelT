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
package com.hybris.backoffice.excel.importing.parser.splitter;

import javax.annotation.Nonnull;


/**
 * Given splitter doesn't split input, because Date is a simple type. It returns array with one element which equals the
 * input.
 */
public class DateExcelParserSplitter implements ExcelParserSplitter
{

	@Override
	public String[] apply(final @Nonnull String input)
	{
		return new String[]
		{ input };
	}
}
