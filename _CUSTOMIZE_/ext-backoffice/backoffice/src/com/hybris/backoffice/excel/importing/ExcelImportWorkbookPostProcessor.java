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

import com.hybris.backoffice.excel.importing.data.ExcelImportResult;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Backoffice Excel mechanism dedicated to postprocessing importing process. The postprocessor is invoked when
 * {@link com.hybris.backoffice.excel.data.Impex} for all {@link com.hybris.backoffice.excel.data.SelectedAttribute} is
 * transformed.
 */
public interface ExcelImportWorkbookPostProcessor
{

	/**
	 * Allows to modifying {@link com.hybris.backoffice.excel.data.Impex} object which will be imported by adding new
	 * content or removing it.
	 * 
	 * @param excelImportResult
	 *           - object represents importing result. The object consists of
	 *           {@link org.apache.poi.ss.usermodel.Workbook} and generated impex object.
	 */
	void process(final ExcelImportResult excelImportResult);

	/**
	 * Uses list of {@link com.hybris.backoffice.excel.validators.ExcelAttributeValidator} to validate excel's workbook.
	 * 
	 * @param workbook
	 *           - excel's workbook
	 * @return Collection of {@link ExcelValidationResult}
	 */
	Collection<ExcelValidationResult> validate(final Workbook workbook);
}
