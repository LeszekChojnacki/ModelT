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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.ValueProviderSelectionStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link ValueProviderSelectionStrategy}.
 */
public class DefaultValueProviderSelectionStrategy implements ValueProviderSelectionStrategy, ApplicationContextAware
{
	private String defaultValueProviderId;
	private ApplicationContext applicationContext;

	public String getDefaultValueProviderId()
	{
		return defaultValueProviderId;
	}

	@Required
	public void setDefaultValueProviderId(final String defaultValueProviderId)
	{
		this.defaultValueProviderId = defaultValueProviderId;
	}

	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@Override
	public Object getValueProvider(final String valueProviderId)
	{
		return applicationContext.getBean(valueProviderId);
	}

	@Override
	public String resolveValueProvider(final IndexedType indexedType, final IndexedProperty indexedProperty)
	{
		if (!StringUtils.isBlank(indexedProperty.getFieldValueProvider()))
		{
			return indexedProperty.getFieldValueProvider();
		}

		if (!StringUtils.isBlank(indexedType.getDefaultFieldValueProvider()))
		{
			return indexedType.getDefaultFieldValueProvider();
		}

		return defaultValueProviderId;
	}

	@Override
	public Map<String, Collection<IndexedProperty>> resolveValueProviders(final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties)
	{
		final Map<String, Collection<IndexedProperty>> valueProviders = new HashMap<>();

		for (final IndexedProperty indexedProperty : indexedProperties)
		{
			final String valueProviderId = resolveValueProvider(indexedType, indexedProperty);

			Collection<IndexedProperty> valueProviderEntries = valueProviders.get(valueProviderId);
			if (valueProviderEntries == null)
			{
				valueProviderEntries = new ArrayList<IndexedProperty>();
				valueProviders.put(valueProviderId, valueProviderEntries);
			}

			valueProviderEntries.add(indexedProperty);
		}

		return valueProviders;
	}
}
