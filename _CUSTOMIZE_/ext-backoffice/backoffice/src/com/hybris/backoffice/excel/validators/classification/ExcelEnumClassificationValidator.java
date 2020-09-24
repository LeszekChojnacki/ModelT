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
package com.hybris.backoffice.excel.validators.classification;

import de.hybris.platform.catalog.enums.ClassificationAttributeTypeEnum;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeValueModel;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


public class ExcelEnumClassificationValidator extends AbstractSingleClassificationFieldValidator
{
	public static final String VALIDATION_INCORRECT_TYPE_ENUM_MESSAGE_KEY = "excel.import.validation.incorrecttype.enum";


	@Override
	public boolean canHandleSingle(final @Nonnull ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters)
	{
		return ClassificationAttributeTypeEnum.ENUM == excelAttribute.getAttributeAssignment().getAttributeType();
	}

	@Override
	public ExcelValidationResult validate(final @Nonnull ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters, final @Nonnull Map<String, Object> context)
	{
		final String cellValue = String.valueOf(importParameters.getCellValue()).trim();
		final Collection<ClassificationAttributeValueModel> attributeValues = excelAttribute.getAttributeAssignment()
				.getAttributeValues();

		if (isImportedEnumOnValueList(cellValue, attributeValues))
		{
			return ExcelValidationResult.SUCCESS;
		}
		return new ExcelValidationResult(new ValidationMessage(VALIDATION_INCORRECT_TYPE_ENUM_MESSAGE_KEY, cellValue,
				excelAttribute.getAttributeAssignment().getClassificationAttribute().getCode()));
	}

	protected boolean isImportedEnumOnValueList(final String attributeValueCode,
			final Collection<ClassificationAttributeValueModel> attributeValues)
	{
		for (final ClassificationAttributeValueModel attributeValue : attributeValues)
		{
			if (Objects.equals(attributeValueCode, attributeValue.getCode()))
			{
				return true;
			}
		}
		return false;
	}
}
