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

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for enum types. The validator checks whether enum type exists and enum value is a member of
 * the enum type.
 */
public class ExcelEnumValidator implements ExcelValidator
{

	public static final String VALIDATION_INCORRECTTYPE_ENUMVALUE_MESSAGE_KEY = "excel.import.validation.incorrecttype.enumvalue";
	public static final String VALIDATION_INCORRECTTYPE_ENUM_MESSAGE_KEY = "excel.import.validation.incorrecttype.enum";

	private EnumerationService enumerationService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final String enumCode = attributeDescriptor.getAttributeType().getCode();
		try
		{
			final List<HybrisEnumValue> enumValues = getEnumerationService().getEnumerationValues(enumCode);
			final boolean isCorrect = enumValues.stream()
					.anyMatch(enumValue -> enumValue.getCode().equals(importParameters.getCellValue()));
			return isCorrect ? ExcelValidationResult.SUCCESS
					: new ExcelValidationResult(new ValidationMessage(VALIDATION_INCORRECTTYPE_ENUMVALUE_MESSAGE_KEY,
							importParameters.getCellValue(), enumCode));
		}
		catch (final UnknownIdentifierException e)
		{
			return new ExcelValidationResult(
					new ValidationMessage(VALIDATION_INCORRECTTYPE_ENUM_MESSAGE_KEY, importParameters.getCellValue(), enumCode));
		}
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank() && attributeDescriptor.getAttributeType() instanceof EnumerationMetaTypeModel;
	}

	public EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}
}
