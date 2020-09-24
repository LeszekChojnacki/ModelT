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
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is a mockup implementation of IndexedTypeFieldsValuesProvider: adds additional field: name 'arbitraryField1',
 * value 'TOP' if product belongs to supercategory.
 *
 * Implementer of {@link IndexedTypeFieldsValuesProvider} must be aware of the solr indexer field naming conventions.
 */
public class MockupIndexTypeValuesProvider implements IndexedTypeFieldsValuesProvider
{

	/**
	 * for an arbitraty field name, the name must be postfixed eith "_TYPE", where TYPE is a solr field type.
	 */
	public static final String NAME = "arbitraryField1";
	public static final String TARGET_SUPER_CATEGORY = "topseller";
	private ModelService modelService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final Object model)
			throws FieldValueProviderException
	{
		final List<FieldValue> fieldValues = new ArrayList<>();

		Collection<CategoryModel> categories = null;
		categories = modelService.getAttributeValue(model, "supercategories");

		if (categories != null && !categories.isEmpty())
		{
			String catName = null;
			for (final CategoryModel category : categories)
			{
				catName = modelService.getAttributeValue(category, "code");
				if (TARGET_SUPER_CATEGORY.equals(catName))
				{
					fieldValues.add(new FieldValue(NAME + "_" + "string", "TOP"));
				}
			}
		}

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
		return Collections.emptySet();
	}


	@Override
	public Map<String, String> getFieldNamesMapping()
	{
		// YTODO Auto-generated method stub
		return Collections.emptyMap();
	}

}
