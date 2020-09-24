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
package com.hybris.backoffice.excel.importing.parser;

import java.util.List;
import java.util.Map;


/**
 * Represents parsed values. Original raw cell value is stored in {@link ParsedValues#cellValue}
 */
public class ParsedValues
{
	/**
	 * List of parameters.
	 */
	private final List<Map<String, String>> parameters;

	/**
	 * Raw cell value
	 */
	private final String cellValue;

	public ParsedValues(final String cellValue, final List<Map<String, String>> parameters)
	{
		this.cellValue = cellValue;
		this.parameters = parameters;
	}

	/**
	 * @return raw cell value
	 */
	public String getCellValue()
	{
		return cellValue;
	}

	/**
	 * @return parsed cell values.
	 */
	public List<Map<String, String>> getParameters()
	{
		return parameters;
	}
}
