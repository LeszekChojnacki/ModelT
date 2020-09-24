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
package de.hybris.platform.ruleengineservices.compiler.impl;

import static com.google.common.collect.Lists.newArrayList;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerListenersFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DefaultRuleCompilerListenersFactory implements RuleCompilerListenersFactory, ApplicationContextAware
{
	private Collection<Class<?>> supportedTypes;

	private ApplicationContext applicationContext;
	private Map<Class<?>, List<Object>> listeners;

	public Collection<Class<?>> getSupportedTypes()
	{
		return supportedTypes;
	}

	@Required
	public void setSupportedTypes(final Collection<Class<?>> supportedTypes)
	{
		this.supportedTypes = supportedTypes;
	}

	protected ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
		this.listeners = loadListeners();
	}

	protected Map<Class<?>, List<Object>> loadListeners()
	{
		final Map<Class<?>, List<Object>> listenersMap = new HashMap<>();

		final Map<String, RuleCompilerListenerDefinition> listenerDefinitionsMap = applicationContext
				.getBeansOfType(RuleCompilerListenerDefinition.class);
		final List<RuleCompilerListenerDefinition> listenerDefinitions = new ArrayList<RuleCompilerListenerDefinition>(
				listenerDefinitionsMap.values());

		Collections.sort(listenerDefinitions);

		for (final Class<?> type : supportedTypes)
		{
			final List<Object> listenersForType = registerListenersForType(listenerDefinitions, type);
			listenersMap.put(type, listenersForType);
		}

		return listenersMap;
	}

	protected List<Object> registerListenersForType(final List<RuleCompilerListenerDefinition> listenerDefinitions, final Class<?> type)
	{
		final List<Object> listenersForType = newArrayList();
		for (final RuleCompilerListenerDefinition listenerDefinition : listenerDefinitions)
		{
			final Object listener = listenerDefinition.getListener();
			if (listener != null)
			{
				final Class<?> listenerType = listener.getClass();
				if (type.isAssignableFrom(listenerType))
				{
					listenersForType.add(listener);
				}
			}
		}
		return listenersForType;
	}

	@Override
	public <T> List<T> getListeners(final Class<T> listenerType)
	{
		final List<Object> listenersForType = listeners.get(listenerType);
		if (listenersForType != null)
		{
			return (List<T>) Collections.unmodifiableList(listenersForType);
		}

		return Collections.emptyList();
	}
}
