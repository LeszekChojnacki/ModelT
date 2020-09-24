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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;


public class DateValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(DateValueResolver.class);
	private static final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> {
		final DateFormat solrDateFormat = new SimpleDateFormat(BackofficesolrsearchConstants.SOLR_DATE_FORMAT);
		solrDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return solrDateFormat;
	});
	private ModelService modelService;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		Object date = null;
		String fieldName = null;

		if (indexedProperty.isLocalized())
		{
			if (resolverContext.getQualifier() != null)
			{
				final LanguageModel lang = resolverContext.getQualifier().getValueForType(LanguageModel.class);
				final Locale locale = resolverContext.getQualifier().getValueForType(Locale.class);

				if (lang != null && locale != null)
				{
					date = modelService.getAttributeValue(model, indexedProperty.getName(), locale);
					fieldName = String.format("%s_%s_%s", indexedProperty.getName(), lang.getIsocode(), indexedProperty.getType());
				}
				else
				{
					LOG.warn("Cannot index localized property {} due to missing lang qualifier provider", indexedProperty.getName());
				}
			}
		}
		else
		{
			date = modelService.getAttributeValue(model, indexedProperty.getName());
			fieldName = String.format("%s_%s", indexedProperty.getName(), indexedProperty.getType());
		}

		if (date instanceof Date)
		{
			document.addField(fieldName, DATE_FORMAT.get().format(date));
		}
		else if (date != null)
		{
			LOG.warn("Indexing property {} of %s is not a reference", indexedProperty.getName(), model.getItemtype());
		}
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
}
