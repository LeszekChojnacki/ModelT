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

import java.util.Collection;
import java.util.Map;

import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to validate single media file. It is used by {@link ExcelMediaCollectionImportValidator} which splits the
 * collection of media and validates it one by one.
 */
public interface ExcelSingleMediaValidator extends ExcelValidator
{

	/**
	 * Allows to validate single media.
	 *
	 * @param context
	 *           which allows to hold information about the file.
	 * @param parameters
	 *           values of excel's cell.
	 * @return the collection of errors. If media has no errors, then the empty collection should be returned.
	 */
	Collection<ValidationMessage> validateSingleValue(final Map<String, Object> context, final Map<String, String> parameters);

}
