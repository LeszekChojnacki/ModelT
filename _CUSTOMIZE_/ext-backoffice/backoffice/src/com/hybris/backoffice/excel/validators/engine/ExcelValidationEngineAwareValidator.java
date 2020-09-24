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
package com.hybris.backoffice.excel.validators.engine;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Validator which uses platform validation engine. The validator is invoked for every cell in excel and the validation
 * is not translator aware.
 */
public interface ExcelValidationEngineAwareValidator
{

	/**
	 * Validates currently processed cell's value
	 *
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @return validation result
	 */
	ExcelValidationResult validate(final ExcelAttribute excelAttribute, final ImportParameters importParameters);

}
