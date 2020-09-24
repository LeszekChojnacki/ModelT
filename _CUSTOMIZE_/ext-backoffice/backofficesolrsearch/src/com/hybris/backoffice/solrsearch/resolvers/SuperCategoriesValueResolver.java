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
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;


public class SuperCategoriesValueResolver extends CollectionValueResolver
{

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		if (model instanceof ProductModel)
		{
			final Collection<CategoryModel> supercategories = ((ProductModel) model).getSupercategories();
			document.addField(String.format("%s_%s", indexedProperty.getName(), indexedProperty.getType()),
					Boolean.valueOf(CollectionUtils.isEmpty(supercategories)));
		}
	}
}
