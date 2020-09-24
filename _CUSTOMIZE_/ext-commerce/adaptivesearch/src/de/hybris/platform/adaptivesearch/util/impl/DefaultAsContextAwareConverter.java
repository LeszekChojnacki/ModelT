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
package de.hybris.platform.adaptivesearch.util.impl;

import de.hybris.platform.adaptivesearch.AsRuntimeException;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link ContextAwareConverter}.
 */
public class DefaultAsContextAwareConverter<S, T, C> implements ContextAwareConverter<S, T, C>
{
	private Class<T> targetClass;
	private List<ContextAwarePopulator<S, T, C>> populators;

	@Override
	public T convert(final S source, final C context)
	{
		final T target = createTarget();

		if (CollectionUtils.isNotEmpty(populators))
		{
			for (final ContextAwarePopulator<S, T, C> populator : populators)
			{
				populator.populate(source, target, context);
			}
		}

		return target;
	}

	protected T createTarget()
	{
		try
		{
			return targetClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new AsRuntimeException(e);
		}
	}

	public Class<T> getTargetClass()
	{
		return targetClass;
	}

	public void setTargetClass(final Class<T> targetClass)
	{
		this.targetClass = targetClass;
	}

	public List<ContextAwarePopulator<S, T, C>> getPopulators()
	{
		return populators;
	}

	@Required
	public void setPopulators(final List<ContextAwarePopulator<S, T, C>> populators)
	{
		this.populators = populators;
	}
}
