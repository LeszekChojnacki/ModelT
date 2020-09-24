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

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.importing.data.TypeSystem;


/**
 * Allows to read the information from the hidden TypeSystem sheet in previously exported Excel file.
 * 
 * @param <T>
 *           represents the hidden TypeSystem sheet, needed to access data kept in TypeSystem's sheet
 */
public interface ExcelTypeSystemService<T extends TypeSystem>
{
	/**
	 * reads hidden TypeSystem sheet from given {@link Workbook} and returns it.
	 *
	 * @param workbook
	 *           should contain hidden TypeSystem
	 * @return POJO which contains data read from {@link Workbook}'s TypeSystem
	 */
	T loadTypeSystem(Workbook workbook);
}
