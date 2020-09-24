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

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelExportResult;


/**
 * Excel decorator used by {@link ExcelExportWorkbookPostProcessor} mechanism. Each decorator can modify exported
 * workbook by adding new content
 */
public interface ExcelExportWorkbookDecorator extends Ordered
{

	/**
	 * Decorates exported {@link org.apache.poi.ss.usermodel.Workbook} object.
	 * 
	 * @param excelExportResult
	 *           - export result which contains exported workbook, list of selected items, list of selected attributes and
	 *           list of additional attributes
	 */
	void decorate(final ExcelExportResult excelExportResult);
}
