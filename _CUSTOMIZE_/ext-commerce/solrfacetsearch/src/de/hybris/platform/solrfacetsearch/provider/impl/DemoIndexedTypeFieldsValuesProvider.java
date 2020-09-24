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
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 
 */
public class DemoIndexedTypeFieldsValuesProvider implements IndexedTypeFieldsValuesProvider
{

	protected static final String NAME_PROPERTY = "name";
	protected static final String CODE_PROPERTY = "code";
	protected static final String CATEGORY_PROPERTY = "category";
	protected static final String MANUFACTURER_NAME_PROPERTY = "manufacturerName";

	protected static final String SUPER_CATEGORIES_ATTR_NAME = "supercategories";

	private ModelService modelService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final Object model)
			throws FieldValueProviderException
	{
		final Collection<FieldValue> result = new ArrayList<FieldValue>();
		result.addAll(getPropertyFieldsValues(model, NAME_PROPERTY));
		result.addAll(getPropertyFieldsValues(model, CODE_PROPERTY));
		result.addAll(getPropertyFieldsValues(model, MANUFACTURER_NAME_PROPERTY));
		result.addAll(getCategoryFieldsValues(model));

		return result;
	}

	protected Collection<FieldValue> getCategoryFieldsValues(final Object model)
	{
		Collection<CategoryModel> categories = null;
		if (model instanceof VariantProductModel)
		{
			final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
			categories = modelService.getAttributeValue(baseProduct, SUPER_CATEGORIES_ATTR_NAME);
		}
		else
		{
			categories = modelService.getAttributeValue(model, SUPER_CATEGORIES_ATTR_NAME);
		}
		if (categories != null && !categories.isEmpty())
		{
			final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
			for (final CategoryModel category : categories)
			{

				fieldValues.add(new FieldValue(getFieldNamesMapping().get(CATEGORY_PROPERTY), getCategoryValue(category)));
				for (final CategoryModel superCategory : category.getAllSupercategories())
				{
					fieldValues.add(new FieldValue(getFieldNamesMapping().get(CATEGORY_PROPERTY), getCategoryValue(superCategory)));
				}

			}
			return fieldValues;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	protected Collection<FieldValue> getPropertyFieldsValues(final Object model, final String propertyName)
	{
		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
		final Object value = getPropertyValue(model, propertyName);
		fieldValues.add(new FieldValue(getFieldNamesMapping().get(propertyName), value));
		return fieldValues;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


	@Override
	public Set<String> getFacets()
	{
		final Set<String> facets = new HashSet<String>();
		facets.add(CATEGORY_PROPERTY);
		facets.add(MANUFACTURER_NAME_PROPERTY);
		return facets;
	}

	@Override
	public Map<String, String> getFieldNamesMapping()
	{
		final Map<String, String> mapping = new HashMap<String, String>();
		mapping.put(CATEGORY_PROPERTY, "category_string_mv");
		mapping.put(CODE_PROPERTY, "code_string");
		mapping.put(NAME_PROPERTY, "name_string");
		mapping.put(MANUFACTURER_NAME_PROPERTY, "manufacturerName_string");
		return mapping;
	}

	protected Object getCategoryValue(final CategoryModel category)
	{
		return getPropertyValue(category, "name");
	}

	protected Object getPropertyValue(final Object model, final String propertyName)
	{
		return modelService.getAttributeValue(model, propertyName);
	}



}
