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
import de.hybris.platform.core.model.type.MapTypeModel;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for string value. The validator checks whether cell value is String.
 */
public class ExcelStringValidator implements ExcelValidator
{

	public static final String VALIDATION_INCORRECTTYPE_STRING_MESSAGE_KEY = "excel.import.validation.incorrecttype.string";


	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final boolean canBeCasted = importParameters.getCellValue() instanceof String;
		return canBeCasted ? ExcelValidationResult.SUCCESS
				: new ExcelValidationResult(
						new ValidationMessage(VALIDATION_INCORRECTTYPE_STRING_MESSAGE_KEY, importParameters.getCellValue()));
	}


	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank()
				&& (StringUtils.equals(attributeDescriptor.getAttributeType().getCode(), String.class.getCanonicalName())
						|| attributeDescriptor.getAttributeType() instanceof MapTypeModel);
	}

}
