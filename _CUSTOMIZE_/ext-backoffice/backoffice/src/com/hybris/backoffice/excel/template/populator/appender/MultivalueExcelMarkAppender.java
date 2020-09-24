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
package com.hybris.backoffice.excel.template.populator.appender;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Adds a special mark when input is multivalue
 */
public class MultivalueExcelMarkAppender implements ExcelMarkAppender<ExcelAttribute>
{

	@Override
	public String apply(final String s, final ExcelAttribute excelAttribute)
	{
		return excelAttribute.isMultiValue() ? (s + ExcelTemplateConstants.SpecialMark.MULTIVALUE.getMark()) : s;
	}

	@Override
	public int getOrder()
	{
		return 30_000;
	}
}
