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
package com.hybris.backoffice.solrsearch.resolvers;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

/**
 * Provides list of FieldValue for classification catalog version.
 */
public class ClassificationCatalogVersionValueProvider implements FieldValueProvider
{

	private FieldNameProvider fieldNameProvider;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		if (model instanceof ProductModel)
		{
			final List<Long> classificationCatalogVersionsIds = ((ProductModel) model).getClassificationClasses().stream()
					.map(classificationClass -> classificationClass.getCatalogVersion().getPk().getLong()).distinct()
					.collect(Collectors.toList());

			final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty, null);

			final List<FieldValue> fieldsValues = new ArrayList<>();
			for (final String fieldName : fieldNames)
			{
				for (final Long catalogVersionId : classificationCatalogVersionsIds)
				{
					fieldsValues.add(new FieldValue(fieldName, catalogVersionId));
				}
			}

			return fieldsValues;
		}
		return Collections.emptyList();
	}

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}
}
