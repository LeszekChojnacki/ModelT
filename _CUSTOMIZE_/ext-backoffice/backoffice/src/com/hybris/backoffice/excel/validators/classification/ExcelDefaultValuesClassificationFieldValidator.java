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

import java.util.Map;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to handle {@link com.hybris.backoffice.excel.importing.parser.ExcelParserException}. It occurs when given
 * default values are in incorrect format.
 */
public class ExcelDefaultValuesClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	private static final String EXCEL_IMPORT_VALIDATION_INCORRECTFORMAT = "excel.import.validation.incorrectformat";

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return importParameters.hasFormatErrors();
	}

	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		return new ExcelValidationResult(new ValidationMessage(EXCEL_IMPORT_VALIDATION_INCORRECTFORMAT,
				importParameters.getCellValue(), importParameters.getFormatError()));
	}
}
