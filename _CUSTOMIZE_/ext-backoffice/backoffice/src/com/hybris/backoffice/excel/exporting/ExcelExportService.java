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
package com.hybris.backoffice.excel.exporting;

import de.hybris.platform.core.model.ItemModel;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.SelectedAttribute;


/**
 * Service for exporting data to excel workbook
 */
public interface ExcelExportService
{

	/**
	 * Exports a template excel workbook. Its purpose is to provide an empty excel file that a user can fill with data,
	 * and then re-import. The template contains sheets for the {@code typeCode} and all of it's non-abstract sub-types,
	 * with all the metadata (like the available attributes).
	 *
	 * @param typeCode
	 *           the type for which the template workbook will be created
	 * @return the template workbook
	 */
	Workbook exportTemplate(String typeCode);

	/**
	 * Exports data to excel workbook
	 *
	 * @param selectedItems
	 *           list of items which will be exported
	 * @param selectedAttributes
	 *           list of selected attributes which have metadata necessary for reimporting the data
	 * @return sheet
	 */
	Workbook exportData(final List<ItemModel> selectedItems, final List<SelectedAttribute> selectedAttributes);

	/**
	 * Exports data to excel workbook for given type
	 *
	 * @param typeCode
	 *           type to export
	 * @param selectedAttributes
	 *           list of selected attributes which have metadata necessary for reimporting the data
	 * @return sheet
	 */
	Workbook exportData(String typeCode, final List<SelectedAttribute> selectedAttributes);
}
