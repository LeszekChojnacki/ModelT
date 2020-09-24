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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Adds a special character {@value com.hybris.backoffice.excel.template.ExcelTemplateConstants.Mark#UNIQUE} to given
 * input, when the given attribute is unique
 */
public class UniqueExcelMarkAppender implements ExcelMarkAppender<ExcelAttributeDescriptorAttribute>
{

	private ExcelFilter<AttributeDescriptorModel> uniqueFilter;

	@Override
	public String apply(final String s, final ExcelAttributeDescriptorAttribute excelAttributeDescriptorAttribute)
	{
		return uniqueFilter.test(excelAttributeDescriptorAttribute.getAttributeDescriptorModel())
				? (s + ExcelTemplateConstants.SpecialMark.UNIQUE.getMark()) : s;
	}

	@Override
	public int getOrder()
	{
		return 10_000;
	}

	@Required
	public void setUniqueFilter(final ExcelFilter<AttributeDescriptorModel> uniqueFilter)
	{
		this.uniqueFilter = uniqueFilter;
	}
}
