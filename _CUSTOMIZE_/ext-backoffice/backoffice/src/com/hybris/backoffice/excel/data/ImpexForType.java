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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


/**
 * Represents impex for given type code. The object consists of type code and table with impex headers and data rows.
 */
public class ImpexForType implements Serializable
{

	/**
	 * Type code for given impex.
	 */
	private String typeCode;

	/**
	 * Tables represents impex structure. A key for row is equals to row index. A key for column is equals to
	 * {@link ImpexHeaderValue}.
	 */
	private final transient Table<Integer, ImpexHeaderValue, Object> impexTable = HashBasedTable.create();

	public ImpexForType(final String typeCode)
	{
		this.typeCode = typeCode;
	}

	/**
	 * Puts value to impex table. A key for row is equals to row index. A key for column is equals to
	 * {@link ImpexHeaderValue}. If value is null then empty string is put to the table.
	 *
	 * @param rowNumber
	 *           of impex table where value should be put.
	 * @param key
	 *           {@link ImpexHeaderValue} column key where value should be put.
	 * @param value
	 *           {@link Object} value which should be put into table. If value is null then empty string is put.
	 */
	public void putValue(final Integer rowNumber, final ImpexHeaderValue key, final Object value)
	{
		impexTable.put(rowNumber, key, value != null ? value : "");
	}

	/**
	 * Adds row with row number equal to max(rowNumber) + 1 {@link #putValue(Integer, ImpexHeaderValue, Object)}
	 * 
	 * @param row
	 *           map of header and value.
	 */
	public void addRow(final Map<ImpexHeaderValue, Object> row)
	{
		final int nextRow = impexTable.rowKeySet().isEmpty() ? 0 : (Collections.max(impexTable.rowKeySet()) + 1);
		row.forEach((key, value) -> putValue(nextRow, key, value));
	}

	/**
	 * Finds impex row by rowIndex
	 *
	 * @param rowIndex
	 * @return ImpexRow
	 */
	public ImpexRow getRow(final Integer rowIndex)
	{
		return new ImpexRow(impexTable.row(rowIndex));
	}

	/**
	 * @return type code for the Impex.
	 */
	public String getTypeCode()
	{
		return typeCode;
	}

	/**
	 * Sets type code for the Impex
	 * 
	 * @param typeCode
	 */
	public void setTypeCode(final String typeCode)
	{
		this.typeCode = typeCode;
	}

	/**
	 * @return table represents impex structure.
	 */
	public Table<Integer, ImpexHeaderValue, Object> getImpexTable()
	{
		return impexTable;
	}
}
