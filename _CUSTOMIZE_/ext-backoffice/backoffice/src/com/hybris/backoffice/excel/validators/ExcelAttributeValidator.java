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

import java.util.Map;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.AbstractExcelImportWorkbookDecorator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Excel validator interface used by excel attribute's translator. This validator should be used for validating an
 * excel's cell. For each excel's cell, excel mechanism parses default values and cell values in order to create
 * {@link ImportParameters}. An implementation of this interface should use parsed values from {@link} to validate
 * user's input. Each instance of {@link AbstractExcelImportWorkbookDecorator} contains list of
 * {@link ExcelAttributeValidator} and appropriate validators are chosen based on
 * {@link ExcelAttributeValidator#canHandle(ExcelAttribute, ImportParameters)} method. If
 * {@link ExcelAttributeValidator#canHandle(ExcelAttribute, ImportParameters)} returns true then
 * {@link ExcelAttributeValidator#validate(ExcelAttribute, ImportParameters, Map)} method will be invoked.
 *
 * @param <T>
 *           subclass of {@link ExcelAttribute}
 */
public interface ExcelAttributeValidator<T extends ExcelAttribute>
{

	/**
	 * Indicates whether validator is able to validate given cell value.
	 * 
	 * @param excelAttribute
	 * @param importParameters
	 * @return boolean whether validator can validate the cell
	 */
	boolean canHandle(@Nonnull T excelAttribute, @Nonnull ImportParameters importParameters);

	/**
	 * Validates given cell and returns validation result. If cell doesn't have validation issues then
	 * {@link ExcelValidationResult#SUCCESS} should be returned.
	 * 
	 * @param excelAttribute
	 * @param importParameters
	 * @param context
	 *           map which can be used as a cache. The map is shared between all request for given excel sheet.
	 * @return
	 */
	ExcelValidationResult validate(@Nonnull T excelAttribute, @Nonnull ImportParameters importParameters,
			@Nonnull Map<String, Object> context);
}
