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

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.impl.CategoryCodeValueProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


public class CategoryPKValueProvider extends CategoryCodeValueProvider
{

	private FieldNameProvider fieldNameProvider;

	@Override
	protected List<FieldValue> createFieldValue(final CategoryModel category, final IndexedProperty indexedProperty)
	{
		final PK value = (PK) getPropertyValue(category, ItemModel.PK);
		final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty, null);

		return fieldNames.stream().map(fieldName -> new FieldValue(fieldName, value.getLong())).collect(Collectors.toList());
	}

	protected FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	@Override
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		super.setFieldNameProvider(fieldNameProvider);
		this.fieldNameProvider = fieldNameProvider;
	}

}
