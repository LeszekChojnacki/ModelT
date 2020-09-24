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
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.providers.ProductCategoryAssignmentResolver;


public class VariantAwareCategoryPKValueResolver extends AbstractValueResolver<ProductModel, Object, Object>
{

	private static final Logger LOG = LoggerFactory.getLogger(VariantAwareCategoryPKValueResolver.class);

	private ProductCategoryAssignmentResolver categoryAttributeValueProvider;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ProductModel product,
			final AbstractValueResolver.ValueResolverContext<Object, Object> resolverContext)
	{
		final Collection<CategoryModel> categories = getCategoryAttributeValueProvider().getIndexedCategories(product);
		categories.forEach(category -> {
			try
			{
				document.addField(indexedProperty, category.getPk().getLongValue());
			}
			catch (final FieldValueProviderException exc)
			{
				LOG.warn("Could not resolve index property: " + indexedProperty, exc);
			}
		});

	}

	public ProductCategoryAssignmentResolver getCategoryAttributeValueProvider()
	{
		return categoryAttributeValueProvider;
	}

	@Required
	public void setCategoryAttributeValueProvider(final ProductCategoryAssignmentResolver categoryAttributeValueProvider)
	{
		this.categoryAttributeValueProvider = categoryAttributeValueProvider;
	}
}
