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

import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.math.NumberUtils;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to validate Number types
 */
public class ExcelNumberClassificationFieldValidator extends AbstractSingleClassificationFieldValidator
{

	public static final String VALIDATION_INCORRECTTYPE_NUMBER_MESSAGE_KEY = "excel.import.validation.incorrecttype.number";

	@Override
	public boolean canHandleSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return excelAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.NUMBER;
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		final boolean isCreatable = NumberUtils.isCreatable(importParameters.getCellValue().toString());
		return isCreatable ? ExcelValidationResult.SUCCESS
				: new ExcelValidationResult(
						new ValidationMessage(VALIDATION_INCORRECTTYPE_NUMBER_MESSAGE_KEY, importParameters.getCellValue()));
	}

}
