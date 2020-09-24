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


public class DefaultExcelParserMatcher implements ExcelParserMatcher
{

	@Override
	public boolean test(final @Nonnull String ignored)
	{
		return true;
	}
}
