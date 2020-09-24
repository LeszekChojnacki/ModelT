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
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.proxy.LabelServiceProxy;


public class BackofficeValueResolver extends AbstractValueResolver<ItemModel, Object, Object>
{
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeValueResolver.class);
	private LabelServiceProxy labelServiceProxy;
	private UserService userService;
	private FieldNameProvider fieldNameProvider;

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ItemModel model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		final Qualifier qualifier = resolverContext.getQualifier();
		final Locale locale = qualifier.getValueForType(Locale.class);
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, qualifier.toFieldQualifier());

		if (locale != null || CollectionUtils.isNotEmpty(fieldNames))
		{
			addBackofficeSpecificFields(document, model, locale, fieldNames);
		}
		else
		{
			throw new IllegalStateException("Locale value for qualifier " + qualifier + " could not be resolved");
		}
	}

	protected void addBackofficeSpecificFields(final InputDocument document, final ItemModel model, final Locale language,
			final Collection<String> fieldNames) throws FieldValueProviderException
	{
		final String objectLabel = getLabelServiceProxy().getObjectLabel(model, language);

		if (StringUtils.isBlank(objectLabel))
		{
			final String exceptionMessage = String.format("Couldn't retrieve %s for fields: %s", fieldNames, model.getItemtype());
			throw new FieldValueProviderException(exceptionMessage);
		}

		try
		{
			for (final String fieldName : fieldNames)
			{
				document.addField(fieldName, objectLabel);
			}
		}
		catch (final FieldValueProviderException e)
		{
			LOG.error("Couldn't add backoffice specific field", e);
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug(objectLabel);
		}
	}

	public LabelServiceProxy getLabelServiceProxy()
	{
		return labelServiceProxy;
	}

	public void setLabelServiceProxy(final LabelServiceProxy labelServiceProxy)
	{
		this.labelServiceProxy = labelServiceProxy;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}


	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

}
