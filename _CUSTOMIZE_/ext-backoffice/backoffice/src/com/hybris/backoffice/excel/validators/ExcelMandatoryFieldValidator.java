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
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator which checks whether cell is not empty. The validator is invoked only for cells where
 * {@link AttributeDescriptorModel#getOptional()} returns false. If field is localized then validator checks whether
 * {@link ImportParameters#isoCode} is equals to {@link CommonI18NService#getCurrentLanguage()}.
 */
public class ExcelMandatoryFieldValidator implements ExcelValidator
{

	public static final String VALIDATION_MANDATORY_FIELD_MESSAGE_KEY = "excel.import.validation.mandatory.field";

	private CommonI18NService commonI18NService;

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final boolean hasValue = importParameters.getCellValue() != null
				&& StringUtils.isNotBlank(importParameters.getCellValue().toString());
		return hasValue ? ExcelValidationResult.SUCCESS
				: new ExcelValidationResult(new ValidationMessage(VALIDATION_MANDATORY_FIELD_MESSAGE_KEY));
	}

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		if (BooleanUtils.isTrue(attributeDescriptor.getOptional()))
		{
			return false;
		}
		return BooleanUtils.isFalse(attributeDescriptor.getLocalized())
				|| commonI18NService.getCurrentLanguage().getIsocode().equals(importParameters.getIsoCode());
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
