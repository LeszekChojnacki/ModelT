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
package de.hybris.platform.solrfacetsearch.common.impl;

import de.hybris.platform.solrfacetsearch.common.ListenersFactory;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link ListenersFactory}.
 */
public class DefaultListenersFactory implements ListenersFactory, ApplicationContextAware
{
	private Collection<Class<?>> supportedTypes;

	private ApplicationContext applicationContext;
	private Map<Class<?>, List<Object>> globalListeners;

	public Collection<Class<?>> getSupportedTypes()
	{
		return supportedTypes;
	}

	@Required
	public void setSupportedTypes(final Collection<Class<?>> supportedTypes)
	{
		this.supportedTypes = supportedTypes;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
		loadGlobalListeners();
	}

	@Override
	public <T> List<T> getListeners(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Class<T> listenerType)
	{
		final List<T> listeners = new ArrayList<>();

		final List<Object> globalListenersForType = getGlobalListeners().get(listenerType);
		if (globalListenersForType != null)
		{
			listeners.addAll((List<T>) globalListenersForType);
		}

		listeners.addAll(loadIndexConfigListeners(facetSearchConfig.getIndexConfig(), listenerType));
		listeners.addAll(loadIndexedTypeListeners(indexedType, listenerType));

		return Collections.unmodifiableList(listeners);
	}

	protected void loadGlobalListeners()
	{
		globalListeners = new HashMap<>();

		final Map<String, ListenerDefinition> listenerDefinitionsMap = applicationContext.getBeansOfType(ListenerDefinition.class);
		final List<ListenerDefinition> listenerDefinitions = new ArrayList<>(listenerDefinitionsMap.values());

		Collections.sort(listenerDefinitions);

		for (final Class<?> type : supportedTypes)
		{
			final List<Object> listeners = new ArrayList<>();

			for (final ListenerDefinition listenerDefinition : listenerDefinitions)
			{
				final Object listener = listenerDefinition.getListener();
				if (listener != null && type.isAssignableFrom(listener.getClass()))
				{
					listeners.add(listener);
				}
			}

			globalListeners.put(type, listeners);
		}
	}

	protected final <T> List<T> loadIndexConfigListeners(final IndexConfig indexConfig, final Class<T> listenerType)
	{
		return loadListeners(indexConfig.getListeners(), listenerType);
	}

	protected <T> List<T> loadIndexedTypeListeners(final IndexedType indexedType, final Class<T> listenerType)
	{
		return loadListeners(indexedType.getListeners(), listenerType);
	}

	protected <T> List<T> loadListeners(final Collection<String> beanNames, final Class<T> listenerType)
	{
		final List<T> listeners = new ArrayList<>();

		if ((beanNames != null) && !beanNames.isEmpty())
		{
			for (final String beanName : beanNames)
			{
				if (applicationContext.isTypeMatch(beanName, listenerType))
				{
					final T listener = applicationContext.getBean(beanName, listenerType);
					listeners.add(listener);
				}
			}
		}

		return listeners;
	}

	protected ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	protected Map<Class<?>, List<Object>> getGlobalListeners()
	{
		return globalListeners;
	}
}
