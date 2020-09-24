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

public class ExcelColumn
{

	private final SelectedAttribute selectedAttribute;
	private final Integer columnIndex;

	public ExcelColumn(final SelectedAttribute selectedAttribute, final Integer columnIndex)
	{
		this.selectedAttribute = selectedAttribute;
		this.columnIndex = columnIndex;
	}

	public SelectedAttribute getSelectedAttribute()
	{
		return selectedAttribute;
	}

	public Integer getColumnIndex()
	{
		return columnIndex;
	}
}
