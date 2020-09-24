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
package com.hybris.backoffice.excel.validators.engine.converters;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default excel value converter which converts cell value into list of single import parameters
 */
public class ExcelMultiValueConverter implements ExcelValueConverter<Collection>
{

	private int order;

	@Override
	public boolean canConvert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		return excelAttribute.isMultiValue();
	}

	@Override
	public Collection convert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		return importParameters.getMultiValueParameters().stream() //
				.map(params -> params.get(ImportParameters.RAW_VALUE)) //
				.filter(StringUtils::isNotBlank) //
				.collect(Collectors.toList());
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}
}
