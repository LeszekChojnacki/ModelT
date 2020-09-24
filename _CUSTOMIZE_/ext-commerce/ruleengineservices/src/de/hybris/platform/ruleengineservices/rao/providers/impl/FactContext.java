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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.ruleengineservices.enums.FactContextType;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Encapsulates Fact Models and its conversion to RAO modes.
 *
 */
public class FactContext
{
	private final FactContextType type;
	private final Collection facts;
	private final Map<Class, List<RAOProvider>> raoProviders;

	FactContext(final FactContextType type, final Map<Class, List<RAOProvider>> raoProviders, final Collection<?> facts)
	{
		this.type = type;
		this.facts = facts;
		this.raoProviders = raoProviders;
	}

	public FactContextType getType()
	{
		return type;
	}

	public Collection getFacts()
	{
		return facts;
	}

	public Set<RAOProvider> getProviders(final Object obj)
	{
		final Set<RAOProvider> result = new HashSet<RAOProvider>();
		for (final Class clazz : getFactClasses(obj))
		{
			if (raoProviders.containsKey(clazz))
			{
				result.addAll(raoProviders.get(clazz));
			}
		}
		return result;
	}

	protected Set<Class> getFactClasses(final Object obj)
	{
		final Set<Class> result = new HashSet<Class>();
		Class clazz = obj.getClass();
		while (clazz != null)
		{
			result.add(clazz);
			clazz = clazz.getSuperclass();
		}
		return result;
	}
}
