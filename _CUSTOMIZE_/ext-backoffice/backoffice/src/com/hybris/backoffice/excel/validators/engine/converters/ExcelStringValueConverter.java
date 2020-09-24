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

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default excel value converter which converts cell value into string
 */
public class ExcelStringValueConverter implements ExcelValueConverter<String>
{

	private int order;

	@Override
	public boolean canConvert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		return Objects.equals(excelAttribute.getType(), String.class.getName());
	}

	@Override
	public String convert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		return importParameters.getCellValue().toString();
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
