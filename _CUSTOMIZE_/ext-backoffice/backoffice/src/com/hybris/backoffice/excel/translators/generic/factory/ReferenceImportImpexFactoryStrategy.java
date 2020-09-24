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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Default implementation of {@link ImportImpexFactoryStrategy} which creates {@link Impex} objects for non part-of
 * attributes.
 */
public class ReferenceImportImpexFactoryStrategy implements ImportImpexFactoryStrategy
{
	@Override
	public boolean canHandle(final RequiredAttribute rootUniqueAttribute, final ImportParameters importParameters)
	{
		return !rootUniqueAttribute.isPartOf();
	}

	@Override
	public Impex create(final RequiredAttribute rootUniqueAttribute, final ImportParameters importParameters)
	{
		final Impex impex = new Impex();
		final ImpexForType impexForCurrentType = impex.findUpdates(importParameters.getTypeCode());
		impexForCurrentType.putValue(0,
				prepareImpexHeader(rootUniqueAttribute, rootUniqueAttribute.isUnique(), rootUniqueAttribute.isMandatory()),
				prepareImpexValue(rootUniqueAttribute, importParameters));
		return impex;
	}

	protected ImpexHeaderValue prepareImpexHeader(final RequiredAttribute rootUniqueAttribute, final boolean unique,
			final boolean mandatory)
	{
		final String header = innerPrepareImpexHeader(rootUniqueAttribute);
		return new ImpexHeaderValue.Builder(header).withUnique(unique).withMandatory(mandatory)
				.withQualifier(rootUniqueAttribute.getQualifier()).build();
	}

	private static String innerPrepareImpexHeader(final RequiredAttribute requiredAttribute)
	{
		final List<String> childrenValues = new ArrayList<>();
		for (final RequiredAttribute child : requiredAttribute.getChildren())
		{
			final String childValue = innerPrepareImpexHeader(child);
			childrenValues.add(childValue);
		}
		String joinedChildrenValues = String.join(ExcelTemplateConstants.MULTI_VALUE_DELIMITER, childrenValues);

		if (!requiredAttribute.getChildren().isEmpty())
		{
			joinedChildrenValues = String.format("(%s)", joinedChildrenValues);
		}
		return requiredAttribute.getQualifier() + joinedChildrenValues;
	}

	protected String prepareImpexValue(final RequiredAttribute rootUniqueAttribute, final ImportParameters importParameters)
	{

		final List<String> multiValues = new ArrayList<>();
		for (final Map<String, String> params : importParameters.getMultiValueParameters())
		{
			final List<String> values = prepareImpexValueForSingleValue(rootUniqueAttribute, params);
			if (hasValue(values))
			{
				multiValues.add(String.join(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR, values));
			}
		}

		return String.join(ExcelTemplateConstants.MULTI_VALUE_DELIMITER, multiValues);
	}

	private static List<String> prepareImpexValueForSingleValue(final RequiredAttribute attribute,
			final Map<String, String> params)
	{
		final List<String> values = new ArrayList<>();
		final String attributeKey = String.format("%s.%s", attribute.getEnclosingType(), attribute.getQualifier());
		if (attribute.getChildren().isEmpty() && params.containsKey(attributeKey))
		{
			values.add(params.get(attributeKey));
		}

		for (final RequiredAttribute child : attribute.getChildren())
		{
			values.addAll(prepareImpexValueForSingleValue(child, params));
		}
		return values.stream().map(value -> value == null ? StringUtils.EMPTY : value).collect(Collectors.toList());
	}

	private static boolean hasValue(final List<String> values)
	{
		return values.stream().anyMatch(StringUtils::isNotBlank);
	}
}
