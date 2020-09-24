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

import java.util.Collection;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Excel decorator used by {@link DefaultExcelImportWorkbookPostProcessor} mechanism to validate values to import.
 */
public interface ExcelImportWorkbookValidationAwareDecorator extends ExcelImportWorkbookDecorator
{

	/**
	 * Uses lists of {@link com.hybris.backoffice.excel.validators.ExcelAttributeValidator} to validated given workbook.
	 * 
	 * @param workbook
	 * @return collection of validation results
	 */
	Collection<ExcelValidationResult> validate(final Workbook workbook);
}
