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

import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.util.ExcelDateUtils;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for date type. The validator checks whether given date is a correct date for format defined
 * in {@link ExcelDateUtils#getDateTimeFormat()}
 */
public class ExcelDateValidator implements ExcelValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelDateValidator.class);
	public static final String VALIDATION_INCORRECTTYPE_DATE_MESSAGE_KEY = "excel.import.validation.incorrecttype.date";
	private ExcelDateUtils excelDateUtils;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
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

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank()
				&& StringUtils.equals(attributeDescriptor.getAttributeType().getCode(), Date.class.getCanonicalName());
	}

	public ExcelDateUtils getExcelDateUtils()
	{
		return excelDateUtils;
	}

	@Required
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}
}
