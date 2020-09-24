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
 * Strategy for validation engine used by excel mechanism.
 */
public interface ExcelValidationEngineAwareStrategy
{

	/**
	 * Indicates whether current strategy is able to handle given attribute
	 * 
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return whether current strategy is able to handle given attribute
	 */
	boolean canHandle(final ImportParameters importParameters, final ExcelAttribute excelAttribute);

	/**
	 * Validates current value based on backoffice's validation engine
	 * 
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return result of validation
	 */
	ExcelValidationResult validate(final ImportParameters importParameters, final ExcelAttribute excelAttribute);

}
