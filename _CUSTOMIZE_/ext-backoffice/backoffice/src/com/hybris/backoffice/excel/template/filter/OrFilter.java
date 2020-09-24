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
package com.hybris.backoffice.excel.template.filter;

import org.springframework.beans.factory.annotation.Required;


/**
 * Filter which allows for OR logical operator for given {@link ExcelFilter}s
 */
public class OrFilter<T> implements ExcelFilter<T>
{
	private ExcelFilter<T> excelFilter1;
	private ExcelFilter<T> excelFilter2;

	@Override
	public boolean test(final T t)
	{
		return excelFilter1.or(excelFilter2).test(t);
	}

	@Required
	public void setExcelFilter1(final ExcelFilter<T> excelFilter1)
	{
		this.excelFilter1 = excelFilter1;
	}

	@Required
	public void setExcelFilter2(final ExcelFilter<T> excelFilter2)
	{
		this.excelFilter2 = excelFilter2;
	}
}
