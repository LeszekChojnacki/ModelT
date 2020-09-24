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
package com.hybris.backoffice.excel.data;

import java.util.Collection;
import java.util.Map;


/**
 * Represents single row of impex which consists of headers and values
 */
public class ImpexRow
{

	private final Map<ImpexHeaderValue, Object> row;

	public ImpexRow(final Map<ImpexHeaderValue, Object> row)
	{
		this.row = row;
	}

	/**
	 * @return collection of headers
	 */
	public Collection<ImpexHeaderValue> getHeaders()
	{
		return row.keySet();
	}

	/**
	 * @return collection of values of single row
	 */
	public Collection<Object> getValues()
	{
		return row.values();
	}

	public Map<ImpexHeaderValue, Object> getRow()
	{
		return row;
	}
}
