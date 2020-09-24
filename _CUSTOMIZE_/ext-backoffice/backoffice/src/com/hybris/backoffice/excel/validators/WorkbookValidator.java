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
package com.hybris.backoffice.excel.validators;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Interface for validating the workbook. The validator is invoked only once for whole workbook.
 */
public interface WorkbookValidator
{
	/**
	 * Validates workbook. If workbook doesn't have validation issues then empty list should be returned.
	 * 
	 * @param workbook
	 * @return list of validation results. If workbook doesn't have validation issues then empty list should be returned.
	 */
	List<ExcelValidationResult> validate(final Workbook workbook);
}
