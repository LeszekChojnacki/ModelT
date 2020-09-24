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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class ReferenceValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(ReferenceValueResolver.class);
	private ModelService modelService;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
					throws FieldValueProviderException
	{
		Object attributeValue = null;
		String fieldName = null;

		if (indexedProperty.isLocalized())
		{
			if (resolverContext.getQualifier() != null)
			{
				final LanguageModel lang = resolverContext.getQualifier().getValueForType(LanguageModel.class);
				final Locale locale = resolverContext.getQualifier().getValueForType(Locale.class);

				if (lang != null && locale != null)
				{
					fieldName = String.format("%s_%s_%s", indexedProperty.getName(), lang.getIsocode(), indexedProperty.getType());
					attributeValue = modelService.getAttributeValue(model, indexedProperty.getName(), locale);
				}
				else
				{
					LOG.warn("Cannot index localized property {} due to missing lang qualifier provider", indexedProperty.getName());
				}
			}
		}
		else
		{
			attributeValue = modelService.getAttributeValue(model, indexedProperty.getName());
			fieldName = String.format("%s_%s", indexedProperty.getName(), indexedProperty.getType());
		}

		if (attributeValue instanceof ItemModel)
		{
			document.addField(fieldName, ((ItemModel) attributeValue).getPk().getLong());
		}
		else if (attributeValue != null)
		{
			LOG.warn("Indexing property {} of {} is not a reference", indexedProperty.getName(), model.getItemtype());
		}
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}
}
