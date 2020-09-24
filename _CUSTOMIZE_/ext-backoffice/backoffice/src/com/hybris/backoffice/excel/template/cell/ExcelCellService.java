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
package com.hybris.backoffice.excel.template.cell;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;


/**
 * Service responsible for operation on excel's single cell.
 */
public interface ExcelCellService
{

	/**
	 * Returns cell value as a string value. In case when cell contains formula then the formula is evaluated and result
	 * of the evaluation is returned.
	 *
	 * @param cell
	 *           {@link Cell}
	 * @return string value of cell. It's never null - it returns EMPTY String in case of problems with retrieving value
	 */
	String getCellValue(@Nullable final Cell cell);

	/**
	 * Inserts given value to a given cell
	 *
	 * @param cell
	 *           where the value will be inserted
	 * @param object
	 *           a value to insert
	 */
	void insertAttributeValue(@Nonnull final Cell cell, @Nullable final Object object);

}
