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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.ExcelCellPopulator;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;


class ExcelReferenceFormatCellPopulator implements ExcelCellPopulator<ExcelAttributeDescriptorAttribute>
{

	private ExcelTranslatorRegistry registry;

	@Override
	public String apply(final ExcelAttributeContext<ExcelAttributeDescriptorAttribute> populatorContext)
	{
		final AttributeDescriptorModel attributeDescriptor = populatorContext
				.getExcelAttribute(ExcelAttributeDescriptorAttribute.class).getAttributeDescriptorModel();
		return registry.getTranslator(attributeDescriptor)
				.map(excelValueTranslator -> excelValueTranslator.referenceFormat(attributeDescriptor)).orElse(StringUtils.EMPTY);
	}

	@Required
	public void setRegistry(final ExcelTranslatorRegistry registry)
	{
		this.registry = registry;
	}
}
