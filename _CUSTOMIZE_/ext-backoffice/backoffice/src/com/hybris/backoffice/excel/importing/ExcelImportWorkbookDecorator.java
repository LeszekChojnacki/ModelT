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

import javax.annotation.Nonnull;

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.importing.data.ExcelImportResult;


/**
 * Excel decorator used by {@link DefaultExcelImportWorkbookPostProcessor} mechanism. Each decorator can modify impex
 * which will be imported by adding new content
 */
public interface ExcelImportWorkbookDecorator extends Ordered
{

	/**
	 * Decorates {@link com.hybris.backoffice.excel.data.Impex} object.
	 * 
	 * @param excelImportResult
	 *           - import result which contains workbook and transformed {@link com.hybris.backoffice.excel.data.Impex}
	 *           object.
	 */
	void decorate(final @Nonnull ExcelImportResult excelImportResult);

}
