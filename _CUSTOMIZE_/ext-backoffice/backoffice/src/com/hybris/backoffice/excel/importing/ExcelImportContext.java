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
package com.hybris.backoffice.excel.importing;

import java.util.HashMap;
import java.util.Map;

import com.hybris.backoffice.excel.data.ImpexRow;


/**
 * Context class used by excel importing. The context contains impex row for currently processing row
 */
public class ExcelImportContext
{

	private ImpexRow impexRow;
	private final Map<String, Object> ctx = new HashMap<>();

	public ImpexRow getImpexRow()
	{
		return impexRow;
	}

	public void setImpexRow(final ImpexRow impexRow)
	{
		this.impexRow = impexRow;
	}

	public Map<String, Object> getCtx()
	{
		return ctx;
	}

	/**
	 * Gets value from context
	 * 
	 * @param key
	 * @return
	 */
	public Object getValue(final String key)
	{
		return ctx.get(key);
	}

	/**
	 * Sets value under given key
	 * 
	 * @param key
	 * @param value
	 */
	public void setValue(final String key, final Object value)
	{
		ctx.put(key, value);
	}
}
