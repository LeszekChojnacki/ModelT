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
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.proxy.LabelServiceProxy;


public class ItemModelLabelValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{

	public static final Logger LOG = LoggerFactory.getLogger(ItemModelLabelValueResolver.class);

	private LabelServiceProxy labelServiceProxy;
	private ModelService modelService;


	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		final ItemModel itemModel = provideModel(model, indexedProperty);
		if (itemModel != null)
		{
			final Qualifier qualifier = resolverContext.getQualifier();
			if (qualifier != null)
			{
				final LanguageModel language = qualifier.getValueForType(LanguageModel.class);
				final Locale locale = qualifier.getValueForType(Locale.class);

				if (locale != null && language != null)
				{
					final String value = getLabelServiceProxy().getObjectLabel(itemModel, locale);
					document.addField(
							String.format("%s_%s_%s", indexedProperty.getName(), language.getIsocode(), indexedProperty.getType()),
							value);
				}
				else
				{
					throw new IllegalStateException(
							String.format("Locale cannot be resolved for indexed property %s", indexedProperty.getName()));
				}
			}
			else
			{
				throw new IllegalStateException(String.format("Indexed property must be localized and of type string/text to use %s while %s is not",
						ItemModelLabelValueResolver.class.getSimpleName(), indexedProperty.getName()));
			}
		}
	}

	protected ItemModel provideModel(final ItemModel model, final IndexedProperty indexedProperty)
	{
		final String providerParameter = indexedProperty.getValueProviderParameter();
		if (StringUtils.isNotEmpty(providerParameter))
		{
			return getModelService().getAttributeValue(model, providerParameter);
		}
		else
		{
			return provideModel(model);
		}
	}

	protected ItemModel provideModel(final ItemModel model)
	{
		return model;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public LabelServiceProxy getLabelServiceProxy()
	{
		return labelServiceProxy;
	}

	public void setLabelServiceProxy(final LabelServiceProxy labelServiceProxy)
	{
		this.labelServiceProxy = labelServiceProxy;
	}

}
