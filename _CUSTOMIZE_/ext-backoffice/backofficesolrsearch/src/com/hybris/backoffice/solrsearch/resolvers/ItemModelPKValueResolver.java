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
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class ItemModelPKValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{

	private ModelService modelService;

	public static final Logger LOG = LoggerFactory.getLogger(ItemModelPKValueResolver.class);


	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		final Optional<ItemModel> targetModel = getTargetModel(model, indexedProperty);
		if (targetModel.isPresent())
		{
			final Long pkLongValue = targetModel.get().getPk().getLong();
			document.addField(String.format("%s_%s", indexedProperty.getName(), indexedProperty.getType()), pkLongValue);
		}
	}

	protected Optional<ItemModel> getTargetModel(final ItemModel model, final IndexedProperty indexedProperty)
	{
		final String providerParameter = indexedProperty.getValueProviderParameter();
		if (StringUtils.isNotEmpty(providerParameter))
		{
			return Optional.ofNullable(getModelService().getAttributeValue(model, providerParameter));
		}
		else
		{
			return getTargetModel(model);
		}
	}

	protected Optional<ItemModel> getTargetModel(final ItemModel sourceModel)
	{
		return Optional.ofNullable(sourceModel);
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
