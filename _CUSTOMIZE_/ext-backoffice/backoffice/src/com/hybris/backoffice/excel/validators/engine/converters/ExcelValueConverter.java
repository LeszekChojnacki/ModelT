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
package com.hybris.backoffice.excel.validators.engine.converters;

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;


/**
 * Converts string value to correct object representation.
 */
public interface ExcelValueConverter<TYPE> extends Ordered
{

	/**
	 * Indicates whether converter is able to converts given excel attribute
	 * 
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return boolean
	 */
	boolean canConvert(final ExcelAttribute excelAttribute, final ImportParameters importParameters);

	/**
	 * Converts string value into correct object representation
	 * 
	 * @param importParameters
	 *           {@link ImportParameters} list of parsed import parameters
	 * @param excelAttribute
	 *           {@link ExcelAttribute} representation of currently processed attribute
	 * @return correct object representation
	 */
	TYPE convert(final ExcelAttribute excelAttribute, final ImportParameters importParameters);
}
