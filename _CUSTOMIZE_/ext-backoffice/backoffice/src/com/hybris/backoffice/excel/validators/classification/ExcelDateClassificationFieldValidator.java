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

import java.time.format.DateTimeParseException;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.util.ExcelDateUtils;
import com.hybris.backoffice.excel.validators.ExcelDateValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to validate Date types
 */
public class ExcelDateClassificationFieldValidator extends AbstractSingleClassificationFieldValidator
{

	private static final Logger LOG = LoggerFactory.getLogger(ExcelDateValidator.class);
	public static final String VALIDATION_INCORRECTTYPE_DATE_MESSAGE_KEY = "excel.import.validation.incorrecttype.date";
	private ExcelDateUtils excelDateUtils;

	@Override
	public boolean canHandleSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return excelAttribute.getAttributeAssignment().getAttributeType() == ClassificationAttributeTypeEnum.DATE;
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		try
		{
			excelDateUtils.importDate((String) importParameters.getCellValue());
		}
		catch (final DateTimeParseException e)
		{
			LOG.debug("Wrong date format " + importParameters.getCellValue(), e);
			return new ExcelValidationResult(
					new ValidationMessage(VALIDATION_INCORRECTTYPE_DATE_MESSAGE_KEY, importParameters.getCellValue()));
		}
		return ExcelValidationResult.SUCCESS;
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}
}
