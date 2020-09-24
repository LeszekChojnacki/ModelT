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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default excel value converter which converts cell value into boolean
 */
public class ExcelBooleanValueConverter implements ExcelValueConverter<Boolean>
{

	private int order;

	@Override
	public boolean canConvert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		final String cellValue = Objects.toString(importParameters.getCellValue(), StringUtils.EMPTY);
		return StringUtils.isNotBlank(cellValue) && Objects.equals(excelAttribute.getType(), Boolean.class.getName());
	}

	@Override
	public Boolean convert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		return "true".equalsIgnoreCase(importParameters.getCellValue().toString());
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
