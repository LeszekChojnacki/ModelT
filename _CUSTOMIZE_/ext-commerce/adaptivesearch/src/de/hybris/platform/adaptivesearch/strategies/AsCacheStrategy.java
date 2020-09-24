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
package de.hybris.platform.adaptivesearch.strategies;

import java.util.function.Function;


/**
 * Abstraction for interactions with the cache.
 */
public interface AsCacheStrategy
{
	/**
	 * Checks if the cache is enabled for the given {@link AsCacheScope}.
	 *
	 * @return <code>true</code> if the cache is enabled, <code>false</code> otherwise
	 */
	boolean isEnabled(final AsCacheScope cacheScope);

	/**
	 * Returns the value in the cache for the given key. If the key is null or the value is not yet in the cache,
	 * valueLoader will be used to load the missing value.
	 *
	 * @param cacheKey
	 *           - the cache key
	 * @param valueLoader
	 *           - function to load values
	 *
	 * @return the cached value
	 */
	<V> V getWithLoader(final AsCacheKey cacheKey, Function<AsCacheKey, V> valueLoader);

	/**
	 * Clears the cache by removing all cached entries.
	 */
	void clear();

	/**
	 * Returns number of elements currently in the cache.
	 */
	long getSize();

	/**
	 * Returns the hit count.
	 */
	long getHits();

	/**
	 * Returns the miss count.
	 */
	long getMisses();
}
