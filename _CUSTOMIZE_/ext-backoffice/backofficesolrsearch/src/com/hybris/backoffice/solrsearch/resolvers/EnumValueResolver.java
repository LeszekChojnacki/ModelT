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

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnumValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(EnumValueResolver.class);

	private ModelService modelService;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
	{
		final String indexedPropertyName = indexedProperty.getName();
		try
		{
			final Object modelProperty = getModelService().getAttributeValue(model, indexedPropertyName);
			if (isHybrisEnum(modelProperty))
			{
				final HybrisEnumValue hybrisEnum = (HybrisEnumValue) modelProperty;
				document.addField(indexedProperty, getEnumValue(hybrisEnum));
			}
			else
			{
				LOG.warn("Resolving value for IndexedProperty: {} has failed because it's not a HybrisEnumValue",
						indexedProperty.getName());
			}
		}
		catch (final Exception ex)
		{
			LOG.debug(String.format("Cannot resolve property '%s' for type %s", indexedProperty.getName(),
					model.toString()), ex);
		}
	}

	protected boolean isHybrisEnum(final Object modelProperty)
	{
		return modelProperty instanceof HybrisEnumValue;
	}

	protected String getEnumValue(final HybrisEnumValue hybrisEnumValue)
	{
		return hybrisEnumValue.getCode();
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
