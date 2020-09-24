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

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;


class ClassificationFullNamePopulator implements ExcelClassificationCellPopulator
{
	private AttributeNameFormatter<ExcelClassificationAttribute> attributeNameFormatter;

	@Override
	public String apply(final @Nonnull ExcelAttributeContext<ExcelClassificationAttribute> context)
	{
		return attributeNameFormatter.format(context);
	}

	public AttributeNameFormatter<ExcelClassificationAttribute> getAttributeNameFormatter()
	{
		return attributeNameFormatter;
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelClassificationAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}
}
