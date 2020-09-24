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
package com.hybris.backoffice.cockpitng.json.impl;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.converters.PopulatorList;
import de.hybris.platform.converters.impl.AbstractConverter;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.json.MutableConverterRegistry;
import com.hybris.cockpitng.core.util.impl.ReflectionUtils;


/**
 * Default implementation of registry for {@link Converter}.
 */
public class DefaultConverterRegistry implements MutableConverterRegistry
{

	private final Object mutex = new Object();

	private final Map<Class, Class> sourceCache = new HashMap<>();

	private final Map<Class, Class> targetCache = new HashMap<>();

	private Map<Pair<Class, Class>, Converter> registry;

	private <S> Class<S> getSourceGenericParameter(final Converter<S, ?> converter)
	{
		return ReflectionUtils.extractGenericParameterType(converter.getClass(), Converter.class, 0);
	}

	private <S> Class<S> getSourcePopulatorParameter(final Converter<S, ?> converter)
	{
		if ((converter instanceof PopulatorList) && !CollectionUtils.isEmpty(((PopulatorList) converter).getPopulators()))
		{
			final Populator populator = ((PopulatorList<?, ?>) converter).getPopulators().iterator().next();
			return ReflectionUtils.extractGenericParameterType(populator.getClass(), Populator.class, 0);
		}
		else
		{
			return null;
		}
	}

	private <T> Class<T> getTargetGenericParameter(final Converter<?, T> converter)
	{
		return ReflectionUtils.extractGenericParameterType(converter.getClass(), Converter.class, 1);
	}

	private <T> Class<T> getTargetPopulatorParameter(final Converter<?, T> converter)
	{
		if ((converter instanceof PopulatorList) && !CollectionUtils.isEmpty(((PopulatorList) converter).getPopulators()))
		{
			final Populator populator = ((PopulatorList<?, ?>) converter).getPopulators().iterator().next();
			return ReflectionUtils.extractGenericParameterType(populator.getClass(), Populator.class, 1);
		}
		else
		{
			return null;
		}
	}

	private <T> Class<T> getTargetReflection(final Converter<?, T> converter)
	{
		if (converter instanceof AbstractConverter)
		{
			final Field field = org.springframework.util.ReflectionUtils.findField(converter.getClass(), "targetClass");
			if (field != null)
			{
				return ReflectionUtils.getField(field, converter);
			}
		}

		return null;
	}

	@Override
	public <S, T> Pair<Class<S>, Class<T>> getConverterParameters(final Converter<S, T> converter)
	{
		Class<S> sourceClass = getSourceGenericParameter(converter);
		Class<T> destinationClass = getTargetGenericParameter(converter);
		if (sourceClass == null && destinationClass == null)
		{
			sourceClass = getSourcePopulatorParameter(converter);
			destinationClass = getTargetPopulatorParameter(converter);
		}
		if (destinationClass == null)
		{
			destinationClass = getTargetReflection(converter);
		}
		return new ImmutablePair<>(sourceClass, destinationClass);
	}

	@Override
	public <S, T> Class<S> getSourceClass(final Class<? extends T> targetClass)
	{
		synchronized (mutex)
		{
			if (sourceCache.containsKey(targetClass))
			{
				return sourceCache.get(targetClass);
			}
		}

		final Optional<Pair<Class, Class>> filteredEntry = getRegistry().entrySet().stream()
				.filter(entry -> entry.getKey().getRight().isAssignableFrom(targetClass)).map(Map.Entry::getKey)
				.sorted(new SourceComparator(targetClass)).findFirst();

		Class<S> result;
		if (filteredEntry.isPresent())
		{
			result = filteredEntry.get().getLeft();
		}
		else
		{
			result = null;
		}

		synchronized (mutex)
		{
			if (!sourceCache.containsKey(targetClass))
			{
				sourceCache.put(targetClass, result);
			}
			else
			{
				result = sourceCache.get(targetClass);
			}
		}

		return result;
	}

	@Override
	public <S, T> Converter<S, T> getConverterForTarget(final Class<? extends T> targetClass)
	{
		final Class<S> sourceClass = getSourceClass(targetClass);
		if (sourceClass != null)
		{
			return getRegistry().get(new ImmutablePair<>(sourceClass, targetClass));
		}
		else
		{
			return null;
		}
	}

	@Override
	public <S, T> Class<T> getTargetClass(final Class<? extends S> sourceClass)
	{
		synchronized (mutex)
		{
			if (targetCache.containsKey(sourceClass))
			{
				return targetCache.get(sourceClass);
			}
		}

		final Optional<Pair<Class, Class>> filteredEntry = getRegistry().entrySet().stream()
				.filter(entry -> entry.getKey().getLeft().isAssignableFrom(sourceClass)).map(Map.Entry::getKey)
				.sorted(new SourceComparator(sourceClass)).findFirst();

		Class<T> result;
		if (filteredEntry.isPresent())
		{
			result = filteredEntry.get().getLeft();
		}
		else
		{
			result = null;
		}

		synchronized (mutex)
		{
			if (!targetCache.containsKey(sourceClass))
			{
				targetCache.put(sourceClass, result);
			}
			else
			{
				result = targetCache.get(sourceClass);
			}
		}

		return result;
	}

	@Override
	public <S, T> Converter<S, T> getConverterForSource(final Class<? extends S> sourceClass)
	{
		final Class<T> targetClass = getTargetClass(sourceClass);
		if (targetClass != null)
		{
			return getRegistry().get(new ImmutablePair<>(sourceClass, targetClass));
		}
		else
		{
			return null;
		}
	}

	protected Map<Pair<Class, Class>, Converter> getRegistry()
	{
		return registry;
	}

	@Required
	public void setRegistry(final Map<Pair<Class, Class>, Converter> registry)
	{
		this.registry = new HashMap<>(registry);
	}

	@Override
	public <S, D> void addConverter(final Converter<S, D> converter, final Class<? extends S> source,
			final Class<? extends D> destination)
	{
		this.registry.put(new ImmutablePair<>(source, destination), converter);
	}

	protected static class SourceComparator implements Comparator<Pair<Class, Class>>
	{

		private final Class<?> sourceClass;

		public SourceComparator(final Class<?> sourceClass)
		{
			this.sourceClass = sourceClass;
		}

		protected int getSteps(Class clazz)
		{
			int result = 0;

			while (!ObjectUtils.equals(clazz, sourceClass) && clazz != null)
			{
				clazz = clazz.getSuperclass();
				result++;
			}

			return result;
		}

		@Override
		public int compare(final Pair<Class, Class> o1, final Pair<Class, Class> o2)
		{
			return Integer.compare(getSteps(o2.getLeft()), getSteps(o1.getLeft()));
		}
	}

}
