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

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;

/**
 * Default excel validator for boolean value. The validator checks whether value is equals "true" or "false" (ignoring case).
 */
public class ExcelBooleanValidator implements ExcelValidator
{

	public static final String VALIDATION_INCORRECTTYPE_BOOLEAN_MESSAGE_KEY = "excel.import.validation.incorrecttype.boolean";

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final boolean isBooleanValue = StringUtils.equalsAnyIgnoreCase(importParameters.getCellValue().toString(), "true", "false");
		return isBooleanValue ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(new ValidationMessage(
				VALIDATION_INCORRECTTYPE_BOOLEAN_MESSAGE_KEY, importParameters.getCellValue()));
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank()
				&& StringUtils.equals(attributeDescriptor.getAttributeType().getCode(), Boolean.class.getCanonicalName());
	}

}
