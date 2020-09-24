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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class CategoryCodeValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{

	private String categoriesQualifier;
	private FieldNameProvider fieldNameProvider;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		Collection<CategoryModel> categories = null;
		if (model instanceof VariantProductModel)
		{
			final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
			categories = modelService.getAttributeValue(baseProduct, categoriesQualifier);
		}
		else
		{
			categories = modelService.getAttributeValue(model, categoriesQualifier);
		}
		if (categories != null && !categories.isEmpty())
		{
			final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
			for (final CategoryModel category : categories)
			{
				fieldValues.addAll(createFieldValue(category, indexedProperty));
				for (final CategoryModel superCategory : category.getAllSupercategories())
				{
					fieldValues.addAll(createFieldValue(superCategory, indexedProperty));
				}
			}
			return fieldValues;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	protected List<FieldValue> createFieldValue(final CategoryModel category, final IndexedProperty indexedProperty)
	{
		final List<FieldValue> fieldValues = new ArrayList<FieldValue>();
		final Object value = getPropertyValue(category, "code");
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
		for (final String fieldName : fieldNames)
		{
			fieldValues.add(new FieldValue(fieldName, value));
		}
		return fieldValues;
	}

	protected Object getPropertyValue(final Object model, final String propertyName)
	{
		return modelService.getAttributeValue(model, propertyName);
	}

	@Required
	public void setCategoriesQualifier(final String categoriesQualifier)
	{
		this.categoriesQualifier = categoriesQualifier;
	}


	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

}
