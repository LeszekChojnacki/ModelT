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

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel's classification validator for mandatory fields. The Validator is invoked only when
 * {@link ExcelClassificationAttribute#isMandatory()} returns true. If attribute is mandatory then the validator checks
 * whether {@link ImportParameters#getCellValue()} is not blank
 */
public class ExcelMandatoryClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	private CommonI18NService commonI18NService;

	/**
	 * Checks whether {@link ExcelClassificationAttribute#isMandatory()} returns true.
	 * 
	 * @param excelAttribute
	 * @param importParameters
	 * @return true when attribute is mandatory.
	 */
	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			final @Nonnull ImportParameters importParameters)
	{
		if (!excelAttribute.isMandatory())
		{
			return false;
		}
		if (excelAttribute.isLocalized())
		{
			return commonI18NService.getCurrentLanguage().getIsocode().equals(excelAttribute.getIsoCode());
		}
		return true;
	}

	/**
	 * Checks whether {@link ImportParameters#getCellValue()} is not blank.
	 *
	 * @param excelAttribute
	 * @param importParameters
	 * @param context
	 *           map which can be used as a cache. The map is shared between all request for given excel sheet.
	 * @return {@link ExcelValidationResult} which contains validation messages. If validator does not return any
	 *         validation issues then {@link ExcelValidationResult#SUCCESS} will be returned.
	 */
	@Override
	public ExcelValidationResult validate(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters, @Nonnull final Map<String, Object> context)
	{
		if (importParameters.isCellValueBlank())
		{
			return new ExcelValidationResult(new ValidationMessage("excel.import.validation.mandatory.classification.field"));
		}
		return ExcelValidationResult.SUCCESS;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
