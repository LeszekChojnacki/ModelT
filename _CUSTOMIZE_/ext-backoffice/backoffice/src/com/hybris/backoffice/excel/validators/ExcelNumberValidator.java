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
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for number value. The validator checks cell value is {@link NumberUtils#isCreatable(String)}.
 */
public class ExcelNumberValidator implements ExcelValidator
{

	public static final String VALIDATION_INCORRECTTYPE_NUMBER_MESSAGE_KEY = "excel.import.validation.incorrecttype.number";

	private TypeService typeService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final boolean isCreatable = NumberUtils.isCreatable(importParameters.getCellValue().toString());
		return isCreatable ? ExcelValidationResult.SUCCESS
				: new ExcelValidationResult(
						new ValidationMessage(VALIDATION_INCORRECTTYPE_NUMBER_MESSAGE_KEY, importParameters.getCellValue()));
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank()
				&& typeService.isAssignableFrom(Number.class.getCanonicalName(), attributeDescriptor.getAttributeType().getCode());
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
