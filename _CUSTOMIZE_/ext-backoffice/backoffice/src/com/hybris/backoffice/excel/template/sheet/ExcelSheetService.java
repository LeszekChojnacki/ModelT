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
package com.hybris.backoffice.excel.template.sheet;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * Service responsible for operation on excel's sheet.
 */
public interface ExcelSheetService
{
	/**
	 * Creates an empty row in given sheet.
	 *
	 * @param sheet
	 *           where the row will be created
	 * @return row
	 */
	default Row createEmptyRow(final @Nonnull Sheet sheet)
	{
		return sheet.createRow(sheet.getLastRowNum() + 1);
	}

	/**
	 * Creates a new type sheet in given workbook. If a sheet with given typeCode already exists, it is returned without
	 * creating an extra one.
	 *
	 * @param workbook
	 *           workbook which will be extended by a new sheet
	 * @param typeCode
	 *           a new sheet name
	 * @return newly created of already existed Sheet.
	 */
	Sheet createOrGetTypeSheet(@WillNotClose final Workbook workbook, @Nonnull final String typeCode);

	/**
	 * Creates a new sheet and adds it to given workbook.
	 *
	 * @param workbook
	 *           workbook which will be extended by a new sheet
	 * @param sheetName
	 *           a new sheet name
	 * @return newly created Sheet.
	 */
	Sheet createTypeSheet(@WillNotClose final Workbook workbook, @Nonnull final String sheetName);

	/**
	 * Creates a new utility sheet in given workbook. If a sheet with given name already exists, it is returned without
	 * creating an extra one.
	 * 
	 * @param workbook
	 *           workbook which will be extended by a new sheet
	 * @param sheetName
	 *           a new sheet name.
	 * @return newly created or already existed Sheet.
	 */
	Sheet createOrGetUtilitySheet(@WillNotClose final Workbook workbook, @Nonnull final String sheetName);

	/**
	 * Finds sheet name for given type code.
	 * 
	 * @param workbook
	 *           workbook
	 * @param typeCode
	 *           type code
	 * @return found type code.
	 */
	String findSheetNameForTypeCode(@WillNotClose final Workbook workbook, final String typeCode);

	/**
	 * Finds type code for given sheet name.
	 *
	 * @param workbook
	 *           workbook
	 * @param sheetName
	 *           sheet name
	 * @return found type code.
	 */
	String findTypeCodeForSheetName(@WillNotClose final Workbook workbook, final String sheetName);

	/**
	 * Finds column index based on selected attribute. If column doesn't exist then -1 will be returned.
	 *
	 * @param typeSystemSheet
	 *           sheet which contains information about type system
	 * @param sheet
	 *           sheet for current type
	 * @param excelAttribute
	 *           attribute for which column index should be found
	 * @return column index for given selected attribute. If column doesn't exist then -1 will be returned.
	 */
	int findColumnIndex(final Sheet typeSystemSheet, @Nonnull final Sheet sheet, final ExcelAttribute excelAttribute);

	/**
	 * Returns sheet in given workbook
	 * 
	 * @param workbook
	 *           workbook
	 * @param sheetName
	 *           name of the looked for sheet
	 * @return found or not found sheet
	 */
	default Optional<Sheet> getSheet(@WillNotClose final Workbook workbook, @Nonnull final String sheetName)
	{
		return Optional.ofNullable(workbook.getSheet(sheetName));
	}

	/**
	 * Returns all sheets except utility sheets.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return collection of sheets
	 */
	Collection<Sheet> getSheets(@WillNotClose final Workbook workbook);

	/**
	 * Returns all sheets names except utility sheets.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return collection of sheets names
	 */
	Collection<String> getSheetsNames(@WillNotClose final Workbook workbook);

}
