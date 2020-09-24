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
package com.hybris.backoffice.excel.translators.generic.factory;

import de.hybris.platform.core.model.type.MapTypeModel;

import java.util.ArrayList;
import java.util.List;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Default implementation of {@link ReferenceFormatFactory}. The service creates reference-format value based on
 * hierarchical structure of unique attributes.
 */
public class DefaultReferenceFormatFactory implements ReferenceFormatFactory
{
	@Override
	public String create(final RequiredAttribute requiredAttribute)
	{
		final List<String> flatReferenceFormat = flatReferenceFormat(requiredAttribute);
		return String.join(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR, flatReferenceFormat);
	}

	private static List<String> flatReferenceFormat(final RequiredAttribute uniqueAttribute)
	{
		final List<String> references = new ArrayList<>();
		if (uniqueAttribute.getChildren().isEmpty())
		{
			if (uniqueAttribute.getTypeModel() instanceof MapTypeModel)
			{
				references.add("key");
				references.add("value");
			}
			else
			{
				references.add(String.format("%s.%s", uniqueAttribute.getEnclosingType(), uniqueAttribute.getQualifier()));
			}
		}
		else
		{
			for (final RequiredAttribute child : uniqueAttribute.getChildren())
			{
				references.addAll(flatReferenceFormat(child));
			}
		}
		return references;
	}
}
