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
package com.hybris.backoffice.excel.template.populator;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.translators.ExcelAttributeTranslatorRegistry;


class ClassificationReferenceFormatPopulator implements ExcelClassificationCellPopulator
{

	private ExcelAttributeTranslatorRegistry registry;

	@Override
	public String apply(final @Nonnull ExcelAttributeContext<ExcelClassificationAttribute> populatorContext)
	{
		return registry.findTranslator(populatorContext.getExcelAttribute(ExcelClassificationAttribute.class))
				.map(translator -> translator.referenceFormat(populatorContext.getExcelAttribute(ExcelClassificationAttribute.class)))
				.orElse(StringUtils.EMPTY);
	}

	@Required
	public void setRegistry(final ExcelAttributeTranslatorRegistry registry)
	{
		this.registry = registry;
	}
}
