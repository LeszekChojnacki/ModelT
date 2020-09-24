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
package com.hybris.backoffice.excel.translators.classification;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.importing.ExcelImportContext;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;
import com.hybris.backoffice.excel.translators.generic.factory.RequiredAttributesFactory;


public class DefaultClassificationAttributeHeaderValueCreator implements ClassificationAttributeHeaderValueCreator
{
	private static final String PATTERN = "@${columnId}${references}[system='${systemId}',version='${systemVersion}',translator=de.hybris.platform.catalog.jalo.classification.impex.ClassificationAttributeTranslator]";
	private RequiredAttributesFactory requiredAttributesFactory;

	@Override
	public String create(final @Nonnull ExcelClassificationAttribute attribute, final @Nonnull ExcelImportContext ignored)
	{
		final Map<String, String> params = new HashMap<>();
		params.put("columnId", getColumnId(attribute));
		params.put("systemId", getSystemId(attribute));
		params.put("systemVersion", getSystemVersion(attribute));
		params.put("references", getReferences(attribute).orElse(StringUtils.EMPTY));
		return new StrSubstitutor(params).replace(PATTERN);
	}

	private static String getColumnId(final ExcelClassificationAttribute attribute)
	{
		return attribute.getQualifier();
	}

	private static String getSystemId(final ExcelClassificationAttribute attribute)
	{
		return attribute.getAttributeAssignment().getSystemVersion().getCatalog().getId();
	}

	private static String getSystemVersion(final ExcelClassificationAttribute attribute)
	{
		return attribute.getAttributeAssignment().getSystemVersion().getVersion();
	}

	private Optional<String> getReferences(final ExcelClassificationAttribute attribute)
	{
		return Optional.ofNullable(attribute.getAttributeAssignment().getReferenceType()) //
				.map(requiredAttributesFactory::create) //
				.map(this::createReferencesHeader);
	}

	/**
	 * Creates Impex-like, string representation based on {@link RequiredAttribute}'s hierarchical structure. For example
	 * for the attributes:
	 * <ul>
	 * <li>catalog
	 * <ul>
	 * <li>id</li>
	 * <li>version</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * the string representation will be: <strong>(catalog(id,version))</strong>
	 *
	 * @param requiredAttribute
	 *           hierarchical structure of required attributes
	 * @return ImpEx-like string representation
	 */
	private String createReferencesHeader(final RequiredAttribute requiredAttribute)
	{
		String joinedChildrenValues = requiredAttribute.getChildren() //
				.stream() //
				.map(this::createReferencesHeader) //
				.collect(Collectors.joining(ExcelTemplateConstants.MULTI_VALUE_DELIMITER));
		if (!requiredAttribute.getChildren().isEmpty())
		{
			joinedChildrenValues = String.format("(%s)", joinedChildrenValues);
		}
		return requiredAttribute.getQualifier() + joinedChildrenValues;
	}

	@Required
	public void setRequiredAttributesFactory(final RequiredAttributesFactory requiredAttributesFactory)
	{
		this.requiredAttributesFactory = requiredAttributesFactory;
	}
}
