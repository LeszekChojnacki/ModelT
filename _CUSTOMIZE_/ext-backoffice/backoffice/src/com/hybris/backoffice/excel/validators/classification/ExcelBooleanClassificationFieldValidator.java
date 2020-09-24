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

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to validate boolean types
 */
public class ExcelBooleanClassificationFieldValidator extends AbstractSingleClassificationFieldValidator
{

	public static final String VALIDATION_INCORRECTTYPE_BOOLEAN_MESSAGE_KEY = "excel.import.validation.incorrecttype.boolean";

	@Override
	public boolean canHandleSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return excelAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.BOOLEAN;
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		final boolean isBooleanValue = StringUtils.equalsAnyIgnoreCase(importParameters.getCellValue().toString(), "true", "false");
		return isBooleanValue ? ExcelValidationResult.SUCCESS
				: new ExcelValidationResult(
						new ValidationMessage(VALIDATION_INCORRECTTYPE_BOOLEAN_MESSAGE_KEY, importParameters.getCellValue()));
	}

}
