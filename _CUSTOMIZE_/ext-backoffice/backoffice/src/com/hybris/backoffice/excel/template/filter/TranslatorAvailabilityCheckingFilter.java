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
package com.hybris.backoffice.excel.template.filter;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;


/**
 * Filter which checks whether given {@link AttributeDescriptorModel} has an corresponding translator in registry or not
 */
public class TranslatorAvailabilityCheckingFilter implements ExcelFilter<AttributeDescriptorModel>
{
	private ExcelTranslatorRegistry excelTranslatorRegistry;

	@Override
	public boolean test(@Nonnull final AttributeDescriptorModel attributeDescriptor)
	{
		return excelTranslatorRegistry.getTranslator(attributeDescriptor).isPresent();
	}

	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}
}
