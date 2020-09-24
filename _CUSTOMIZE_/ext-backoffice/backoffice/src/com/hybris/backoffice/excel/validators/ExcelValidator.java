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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Map;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Excel validator interface used by excel translators. This validator should be used for validating an excel cell. For
 * each excel cell, excel import mechanism finds appropriate excel translator which can handle the cell. The translator
 * has list of validators and for each validator
 * {@link ExcelValidator#canHandle(ImportParameters, AttributeDescriptorModel)} is invoked. If a validator can handle
 * the cell then {@link ExcelValidator#validate(ImportParameters, AttributeDescriptorModel, Map)} is invoked and list of
 * validation result is collected.
 */
public interface ExcelValidator
{

	/**
	 * Set of entries names inside media content zip attached to excel file
	 */
	String CTX_MEDIA_CONTENT_ENTRIES = "mediaContentEntries";

	/**
	 * Validates given cell and returns validation result. If cell doesn't have validation issues then
	 * {@link ExcelValidationResult#SUCCESS} should be returned.
	 * 
	 * @param importParameters
	 * @param attributeDescriptor
	 * @param context
	 *           - map which can be used as a cache. The map is shared between all request for given excel sheet.
	 * @return {@link ExcelValidationResult}
	 */
	ExcelValidationResult validate(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor,
			final Map<String, Object> context);

	/**
	 * Indicates whether given validator can handle a cell based on attribute descriptor and import parameters.
	 * 
	 * @param importParameters
	 * @param attributeDescriptor
	 * @return boolean whether validator can handle the cell.
	 */
	boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor);

}
