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
package com.hybris.backoffice.excel.template;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.data.SelectedAttributeQualifier;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;


/**
 * Service responsible for operation on excel file.
 *
 * @deprecated since 1808. This service was split for 4 separated services:
 *             <ul>
 *             <li>{@link ExcelCellService}</li>
 *             <li>{@link ExcelHeaderService}</li>
 *             <li>{@link ExcelSheetService}</li>
 *             <li>{@link ExcelWorkbookService}</li>
 *             <li>{@link AttributeNameFormatter}</li>
 *             </ul>
 */
@Deprecated
public interface ExcelTemplateService
{

	/**
	 * Creates workbook object based on inputStream of excel file. If inputStream doesn't contains excel file then new
	 * empty Workbook will be returned.
	 * 
	 * @param is
	 *           Input stream of excel file
	 * @return {@link Workbook} object which represents excel file
	 * @deprecated since 1808. Use {@link ExcelWorkbookService#createWorkbook(InputStream)} instead
	 */
	Workbook createWorkbook(InputStream is);

	/**
	 * Returns sheet which contains metadata about type system
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return {@link Sheet} which contains information about type system
	 * @deprecated since 1808. Use {@link ExcelWorkbookService#getMetaInformationSheet(Workbook)} instead
	 */
	Sheet getTypeSystemSheet(Workbook workbook);

	/**
	 * Returns all sheets names except utility sheets.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return list of sheets names
	 * @deprecated since 1808. Use {@link ExcelSheetService#getSheetsNames(Workbook)} instead
	 */
	List<String> getSheetsNames(Workbook workbook);

	/**
	 * Returns all sheets except utility sheets.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return list of sheets
	 * @deprecated since 1808. Use {@link ExcelSheetService#getSheets(Workbook)} instead
	 */
	List<Sheet> getSheets(Workbook workbook);

	/**
	 * Returns cell value as a string value. In case when cell contains formula then the formula is evaluated and result
	 * of the evaluation is returned.
	 *
	 * @param cell
	 *           {@link Cell}
	 * @return string value of cell
	 * @deprecated since 1808. Use {@link ExcelCellService#getCellValue(Cell)} instead
	 */
	String getCellValue(Cell cell);

	/**
	 * Returns list of selected attributes for given sheet, based on metainformation from type system sheet.
	 *
	 * @param typeSystemSheet
	 *           {@link Sheet} contains metainformation about attributes for each type sheets
	 * @param typeSheet
	 *           {@link Sheet} contains data for given sheet
	 * @return list of selected attributes
	 * @deprecated since 1808. Use {@link ExcelHeaderService#getHeaders(Sheet, Sheet)} instead
	 */
	List<SelectedAttribute> getHeaders(Sheet typeSystemSheet, Sheet typeSheet);

	/**
	 * Returns qualifiers of attributes which at selected in the typSheet
	 *
	 * @param typeSystemSheet
	 *           {@link Sheet} contains metainformation about attributes for each type sheets
	 * @param typeSheet
	 *           {@link Sheet} contains data for given sheet
	 * @return list of selected attributes.
	 * @deprecated since 1808. Use {@link ExcelHeaderService#getSelectedAttributesQualifiers(Sheet, Sheet)} instead
	 */
	List<SelectedAttributeQualifier> getSelectedAttributesQualifiers(Sheet typeSystemSheet, Sheet typeSheet);

	/**
	 * Finds column index based on selected attribute. If column doesn't exist then -1 will be returned.
	 *
	 * @param typeSystemSheet
	 *           - sheet which contains information about type system
	 * @param sheet
	 *           sheet for current type
	 * @param selectedAttribute
	 *           attribute for which column index should be found.
	 * @return column index for given selected attribute. If column doesn't exist then -1 will be returned.
	 * @deprecated since 1808. Use {@link ExcelSheetService#findColumnIndex(Sheet, Sheet, ExcelAttribute)} instead
	 */
	int findColumnIndex(final Sheet typeSystemSheet, final Sheet sheet, SelectedAttribute selectedAttribute);

	/**
	 * Creates a new type sheet in given workbook. If a sheet with given typeCode already exists, it is returned without
	 * creating an extra one.
	 *
	 * @param typeCode
	 *           a new sheet name
	 * @param workbook
	 *           workbook which will be extended by a new sheet
	 * @return sheet
	 * @deprecated since 1808. Use {@link ExcelSheetService#createTypeSheet(Workbook, String)} instead
	 */
	Sheet createTypeSheet(final String typeCode, final Workbook workbook);


	/**
	 * Finds type code for given sheet name.
	 * 
	 * @param sheetName
	 *           sheet name.
	 * @param workbook
	 *           workbook.
	 * @return found type code.
	 * @deprecated since 1808. Use {@link ExcelSheetService#findTypeCodeForSheetName(Workbook, String)} instead
	 */
	String findTypeCodeForSheetName(String sheetName, Workbook workbook);

	/**
	 * Finds sheet name for given type code.
	 * 
	 * @param typeCode
	 *           type code
	 * @param workbook
	 *           workbook
	 * @return found type code.
	 * @deprecated since 1808. Use {@link ExcelSheetService#findSheetNameForTypeCode(Workbook, String)} instead
	 */
	String findSheetNameForTypeCode(String typeCode, Workbook workbook);

	/**
	 * Adds a new sheet to given workbook.
	 *
	 * @param typeName
	 *           a new sheet name.
	 * @param workbook
	 *           workbook which will be extended by a new sheet
	 * @deprecated since 1808. Use {@link ExcelSheetService#createTypeSheet(Workbook, String)} instead
	 */
	void addTypeSheet(final String typeName, final Workbook workbook);

	/**
	 * Inserts value to the sheet's header (first row)
	 *
	 * @param sheet
	 *           where the value will be inserted
	 * @param selectedAttribute
	 *           a pojo which allows to retrieve value to insert
	 * @param columnIndex
	 *           index of a column for inserted value
	 * @deprecated since 1808. Use {@link ExcelHeaderService#insertAttributeHeader(Sheet, ExcelAttribute, int)} instead
	 */
	void insertAttributeHeader(final Sheet sheet, final SelectedAttribute selectedAttribute, final int columnIndex);

	/**
	 * A shortcut for {@link #insertAttributeHeader(Sheet, SelectedAttribute, int)} It is possible to inserts all values
	 * to the header at once instead of invoking {@link #insertAttributeHeader(Sheet, SelectedAttribute, int)} for every
	 * attribute separately
	 *
	 * @param sheet
	 *           where the value will be inserted
	 * @param selectedAttributes
	 *           a pojo which allows to retrieve value to insert
	 * @deprecated since 1808. Use {@link ExcelHeaderService#insertAttributesHeader(Sheet, Collection)} instead
	 */
	void insertAttributesHeader(final Sheet sheet, final Collection<SelectedAttribute> selectedAttributes);

	/**
	 * Inserts given value to a given cell
	 *
	 * @param cell
	 *           where the value will be inserted
	 * @param object
	 *           a value to insert
	 * @deprecated since 1808. Use {@link ExcelCellService#insertAttributeValue(Cell, Object)} instead
	 */
	void insertAttributeValue(final Cell cell, final Object object);

	/**
	 * Creates an empty row in given sheet.
	 *
	 * @param sheet
	 *           where the row will be created
	 * @return row
	 * @deprecated since 1808. Use {@link ExcelSheetService#createEmptyRow(Sheet)} instead
	 */
	Row createEmptyRow(final Sheet sheet);

	/**
	 * Returns displayed header name based on attribute descriptor
	 * 
	 * @param attributeDescriptorModel
	 *           attribute descriptor of selected attribute
	 * @param isoCode
	 *           - isoCode of language
	 * @return displayed header name based on attribute descriptor
	 * @deprecated since 1808. Use {@link DisplayNameAttributeNameFormatter#format(ExcelAttributeContext)} instead
	 */
	String getAttributeDisplayName(AttributeDescriptorModel attributeDescriptorModel, final String isoCode);
}
