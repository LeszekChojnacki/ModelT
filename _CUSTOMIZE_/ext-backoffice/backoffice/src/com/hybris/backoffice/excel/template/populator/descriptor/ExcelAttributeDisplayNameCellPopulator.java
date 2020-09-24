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
package com.hybris.backoffice.excel.template.populator.descriptor;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.ExcelCellPopulator;


class ExcelAttributeDisplayNameCellPopulator implements ExcelCellPopulator<ExcelAttributeDescriptorAttribute>
{
	private AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter;

	@Override
	public String apply(final ExcelAttributeContext<ExcelAttributeDescriptorAttribute> populatorContext)
	{
		return attributeNameFormatter.format(populatorContext);
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}
}
