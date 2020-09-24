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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Default excel value converter which converts cell value into number
 */
public class ExcelNumberValueConverter implements ExcelValueConverter<Number>
{

	private int order;
	private final Map<String, Function<String, Number>> supportedTypes;

	public ExcelNumberValueConverter()
	{
		supportedTypes = new HashMap<>();
		supportedTypes.put(Byte.class.getName(), Byte::valueOf);
		supportedTypes.put(Short.class.getName(), Short::valueOf);
		supportedTypes.put(Integer.class.getName(), Integer::valueOf);
		supportedTypes.put(Long.class.getName(), Long::valueOf);
		supportedTypes.put(Float.class.getName(), Float::valueOf);
		supportedTypes.put(Double.class.getName(), Double::valueOf);
		supportedTypes.put(BigDecimal.class.getName(), BigDecimal::new);
		supportedTypes.put(BigInteger.class.getName(), BigInteger::new);
	}

	@Override
	public boolean canConvert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		final String cellValue = Objects.toString(importParameters.getCellValue(), StringUtils.EMPTY);
		return StringUtils.isNotBlank(cellValue) && supportedTypes.containsKey(excelAttribute.getType());
	}

	@Override
	public Number convert(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		final String cellValue = Objects.toString(importParameters.getCellValue(), StringUtils.EMPTY);
		return supportedTypes.get(excelAttribute.getType()).apply(cellValue);
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
