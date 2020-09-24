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
package com.hybris.backoffice.excel.template.populator.extractor;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.category.model.CategoryModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;


/**
 * Formats {@link ExcelClassificationAttribute} to readable format that includes its ClassificationSystem,
 * ClassificationVersion, ClassificationClass, name and locale.
 */
public class ClassificationFullNameExtractor
{

	private static final String LOCALIZED_PATTERN = "${owner}.${attribute}[${locale}] - ${systemId}/${version}";
	private static final String NON_LOCALIZED_PATTERN = "${owner}.${attribute} - ${systemId}/${version}";

	/**
	 * Formats given {@link ExcelClassificationAttribute} to a human readable format
	 *
	 * @param excelAttribute
	 *           the object that contains information about the attribute
	 * @return human readable information about the {@link ExcelClassificationAttribute}
	 */
	public String extract(final ExcelClassificationAttribute excelAttribute)
	{
		final Map<String, String> params = new HashMap<>();
		params.put("systemId", getSystemId(excelAttribute));
		params.put("version", getSystemVersion(excelAttribute));
		params.put("owner", getClassificationClassName(excelAttribute));
		params.put("attribute", getClassificationAttributeName(excelAttribute));
		if (StringUtils.isNotBlank(excelAttribute.getIsoCode()))
		{
			params.put("locale", excelAttribute.getIsoCode());
			return new StrSubstitutor(params).replace(LOCALIZED_PATTERN);
		}
		return new StrSubstitutor(params).replace(NON_LOCALIZED_PATTERN);
	}

	private static String getSystemId(final ExcelAttribute excelAttribute)
	{
		return Optional.of(excelAttribute) //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.map(ExcelClassificationAttribute::getAttributeAssignment) //
				.map(ClassAttributeAssignmentModel::getSystemVersion) //
				.map(ClassificationSystemVersionModel::getCatalog) //
				.map(CatalogModel::getId) //
				.orElse(StringUtils.EMPTY);
	}

	private static String getClassificationClassName(final ExcelAttribute excelAttribute)
	{
		return Optional.of(excelAttribute) //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.map(ExcelClassificationAttribute::getAttributeAssignment) //
				.map(ClassAttributeAssignmentModel::getClassificationClass) //
				.map(CategoryModel::getCode) //
				.orElse(StringUtils.EMPTY);
	}

	private static String getSystemVersion(final ExcelAttribute excelAttribute)
	{
		return Optional.of(excelAttribute) //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.map(ExcelClassificationAttribute::getAttributeAssignment) //
				.map(ClassAttributeAssignmentModel::getSystemVersion) //
				.map(CatalogVersionModel::getVersion) //
				.orElse(StringUtils.EMPTY);
	}

	private static String getClassificationAttributeName(final ExcelAttribute excelAttribute)
	{
		return Optional.of(excelAttribute) //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.map(ExcelClassificationAttribute::getAttributeAssignment) //
				.map(ClassAttributeAssignmentModel::getClassificationAttribute) //
				.map(ClassificationAttributeModel::getCode) //
				.orElse(StringUtils.EMPTY);
	}
}
