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
package com.hybris.backoffice.excel.template.populator.appender;

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Adds a special character {@value com.hybris.backoffice.excel.template.ExcelTemplateConstants.Mark#MANDATORY} to given
 * input, when the given attribute is mandatory
 */
public class MandatoryExcelMarkAppender implements ExcelMarkAppender<ExcelAttribute>
{

	private CommonI18NService commonI18NService;

	@Override
	public String apply(final String s, final ExcelAttribute excelAttribute)
	{
		return isMandatory(excelAttribute) && !hasDefaultValue(excelAttribute)
				? (s + ExcelTemplateConstants.SpecialMark.MANDATORY.getMark())
				: s;
	}

	private boolean isMandatory(final ExcelAttribute excelAttribute)
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

	private static boolean hasDefaultValue(final ExcelAttribute excelAttribute)
	{
		if (excelAttribute instanceof ExcelAttributeDescriptorAttribute)
		{
			return ((ExcelAttributeDescriptorAttribute) excelAttribute).getAttributeDescriptorModel().getDefaultValue() != null;
		}

		return false;
	}

	@Override
	public int getOrder()
	{
		return 1000;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
