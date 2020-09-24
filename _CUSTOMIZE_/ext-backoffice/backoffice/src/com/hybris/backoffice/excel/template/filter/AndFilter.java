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

import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Filter which allows for AND logical operator for given {@link ExcelFilter}s
 */
public class AndFilter<T> implements ExcelFilter<T>
{
	private List<ExcelFilter<T>> filters;

	@Override
	public boolean test(final T t)
	{
		return filters.stream().allMatch(filter -> filter.test(t));
	}

	@Required
	public void setFilters(final List<ExcelFilter<T>> filters)
	{
		this.filters = filters;
	}

	public List<ExcelFilter<T>> getFilters()
	{
		return filters;
	}
}
