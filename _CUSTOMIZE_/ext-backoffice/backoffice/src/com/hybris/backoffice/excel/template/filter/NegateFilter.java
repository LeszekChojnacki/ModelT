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
 * Filter which allows to negate result of given {@link ExcelFilter}
 */
public class NegateFilter<T> implements ExcelFilter<T>
{
	private ExcelFilter<T> excelFilter;

	@Override
	public boolean test(final T t)
	{
		return excelFilter.negate().test(t);
	}

	@Required
	public void setExcelFilter(final ExcelFilter<T> excelFilter)
	{
		this.excelFilter = excelFilter;
	}
}
