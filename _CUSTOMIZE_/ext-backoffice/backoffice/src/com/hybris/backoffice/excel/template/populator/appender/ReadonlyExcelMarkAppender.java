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

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;

/**
 * Adds a special mark if input is readonly
 */
public class ReadonlyExcelMarkAppender implements ExcelMarkAppender<ExcelAttributeDescriptorAttribute>
{

	@Override
	public String apply(final String s, final ExcelAttributeDescriptorAttribute excelAttributeDescriptorAttribute)
	{
		return isReadonly(excelAttributeDescriptorAttribute.getAttributeDescriptorModel())
				? (s + ExcelTemplateConstants.SpecialMark.READONLY.getMark()) : s;
	}

	private boolean isReadonly(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor.getReadable() && !attributeDescriptor.getWritable();
	}

	@Override
	public int getOrder()
	{
		return 40_000;
	}
}
