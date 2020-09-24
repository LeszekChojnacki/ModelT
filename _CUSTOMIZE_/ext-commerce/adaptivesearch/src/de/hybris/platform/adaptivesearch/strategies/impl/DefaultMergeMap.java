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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.data.AbstractAsConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.util.MergeFunction;
import de.hybris.platform.adaptivesearch.util.MergeMap;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;


/**
 * Default implementation of {@link MergeMap}.
 */
public class DefaultMergeMap<K, T extends AbstractAsConfiguration, R extends AbstractAsConfiguration>
		extends HashMap<K, AsConfigurationHolder<T, R>> implements MergeMap<K, AsConfigurationHolder<T, R>>
{
	protected static final RankComparator RANK_COMPARATOR = new RankComparator();

	private int highestRank;
	private int lowestRank;

	// we don't expect/need any MergeMap implementation to be Serializable
	private transient Comparator<AsConfigurationHolder<T, R>> comparator;

	public DefaultMergeMap()
	{
		highestRank = 0;
		lowestRank = 0;
	}

	public DefaultMergeMap(final Comparator<AsConfigurationHolder<T, R>> comparator)
	{
		highestRank = 0;
		lowestRank = 0;
		this.comparator = comparator;
	}

	@Override
	public void clear()
	{
		highestRank = 0;
		lowestRank = 0;
		super.clear();
	}

	@Override
	public AsConfigurationHolder<T, R> put(final K key, final AsConfigurationHolder<T, R> value)
	{
		return mergeAfter(key, value);
	}

	@Override
	public int getHighestRank()
	{
		return highestRank;
	}

	@Override
	public int getLowestRank()
	{
		return lowestRank;
	}

	@Override
	public AsConfigurationHolder<T, R> mergeBefore(final K key, final AsConfigurationHolder<T, R> value)
	{
		highestRank--;
		value.setRank(highestRank);

		return super.put(key, value);
	}

	@Override
	public void mergeBefore(final MergeMap<K, AsConfigurationHolder<T, R>> source,
			final MergeFunction<K, AsConfigurationHolder<T, R>> mergeFunction)
	{
		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final int range = source.getLowestRank() - source.getHighestRank();
		final int baseRank = highestRank - 1 - range - source.getHighestRank();
		highestRank = highestRank - 1 - range;

		for (final Entry<K, AsConfigurationHolder<T, R>> entry : source.entrySet())
		{
			final AsConfigurationHolder<T, R> configurationHolder = super.compute(entry.getKey(),
					(key, value) -> mergeFunction.apply(key, value, entry.getValue()));

			if (configurationHolder != null)
			{
				configurationHolder.setRank(baseRank + entry.getValue().getRank());
			}
		}
	}


	@Override
	public AsConfigurationHolder<T, R> mergeAfter(final K key, final AsConfigurationHolder<T, R> value)
	{
		lowestRank++;
		value.setRank(lowestRank);

		return super.put(key, value);
	}

	@Override
	public void mergeAfter(final MergeMap<K, AsConfigurationHolder<T, R>> source,
			final MergeFunction<K, AsConfigurationHolder<T, R>> mergeFunction)
	{
		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final int range = source.getLowestRank() - source.getHighestRank();
		final int baseRank = lowestRank + 1 - source.getHighestRank();
		lowestRank = lowestRank + 1 + range;

		for (final Entry<K, AsConfigurationHolder<T, R>> entry : source.entrySet())
		{
			final AsConfigurationHolder<T, R> configurationHolder = super.compute(entry.getKey(),
					(key, value) -> mergeFunction.apply(key, value, entry.getValue()));

			if (configurationHolder != null)
			{
				configurationHolder.setRank(baseRank + entry.getValue().getRank());
			}
		}
	}

	@Override
	public List<AsConfigurationHolder<T, R>> orderedValues()
	{
		if (comparator == null)
		{
			return values().stream().sorted(RANK_COMPARATOR).collect(Collectors.toList());
		}
		else
		{
			return values().stream().sorted(comparator.thenComparing(RANK_COMPARATOR)).collect(Collectors.toList());
		}
	}

	@Override
	public boolean equals(final Object o)
	{
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	protected static final class RankComparator implements Comparator<AsConfigurationHolder<?, ?>>, Serializable
	{
		@Override
		public int compare(final AsConfigurationHolder<?, ?> configurationHolder1,
				final AsConfigurationHolder<?, ?> configurationHolder2)
		{
			return Integer.compare(configurationHolder1.getRank(), configurationHolder2.getRank());
		}
	}
}
