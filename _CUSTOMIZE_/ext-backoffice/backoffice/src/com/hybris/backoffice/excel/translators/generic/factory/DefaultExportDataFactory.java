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

import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Default implementation of {@link ExportDataFactory}. The service prepares value to export based on hierarchical
 * structure of unique attributes
 */
public class DefaultExportDataFactory implements ExportDataFactory
{

	private ModelService modelService;

	@Override
	public Optional<String> create(final RequiredAttribute rootUniqueAttribute, final Object objectToExport)
	{
		final List<String> exportedValues = new ArrayList<>();
		if (objectToExport instanceof Collection)
		{
			for (final Object singleObject : (Collection) objectToExport)
			{
				exportedValues.add(exportSingleValue(singleObject, rootUniqueAttribute));
			}
		}
		else
		{
			exportedValues.add(exportSingleValue(objectToExport, rootUniqueAttribute));
		}
		return Optional.of(String.join(ImportParameters.MULTIVALUE_SEPARATOR, exportedValues));
	}

	private String exportSingleValue(final Object objectToExport, final RequiredAttribute uniqueAttribute)
	{
		final Object loadedValue = uniqueAttribute.isRoot() ? objectToExport
				: loadValue(objectToExport, uniqueAttribute.getQualifier());
		if (uniqueAttribute.getChildren().isEmpty())
		{
			return Objects.toString(loadedValue, StringUtils.EMPTY);
		}

		final String value = uniqueAttribute.getChildren().stream().map(children -> exportSingleValue(loadedValue, children))
				.collect(Collectors.joining(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR));
		return value.matches(":*") ? StringUtils.EMPTY : value;
	}

	private Object loadValue(final Object object, final String qualifier)
	{
		return object == null ? null : modelService.getAttributeValue(object, qualifier);
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
