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

import com.hybris.backoffice.excel.data.ExcelExportResult;


/**
 * Backoffice Excel mechanism dedicated to postprocessing exporting process. The postprocessor is invoked when
 * {@link org.apache.poi.ss.usermodel.Workbook} with all {@link com.hybris.backoffice.excel.data.SelectedAttribute} is
 * exported.
 */
public interface ExcelExportWorkbookPostProcessor
{

	/**
	 * Allows to modify exported {@link org.apache.poi.ss.usermodel.Workbook} by adding new content or removing it.
	 *
	 * @param excelExportResult
	 *           - object represents exporting result. The object consists of exported
	 *           {@link org.apache.poi.ss.usermodel.Workbook}, selected attributes, additional attributes and exported
	 *           items.
	 */
	void process(final ExcelExportResult excelExportResult);
}
