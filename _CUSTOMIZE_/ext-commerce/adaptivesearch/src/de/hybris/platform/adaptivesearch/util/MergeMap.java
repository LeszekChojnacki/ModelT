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
package de.hybris.platform.adaptivesearch.util;

import java.util.List;
import java.util.Map;


/**
 * A {@link Map} that provides additional merge operations. Due to performance reasons the map does not provide
 * predictable iteration order, however it is possible to get an ordered list of values by calling
 * {@link #orderedValues()}.
 */
public interface MergeMap<K, V> extends Map<K, V>
{
	/**
	 * Returns the highest rank of the values in the map.
	 *
	 * @return the highest rank of the values in the map
	 */
	int getHighestRank();

	/**
	 * Returns the lowest rank of the values in the map.
	 *
	 * @return the lowest rank of the values in the map
	 */
	int getLowestRank();

	/**
	 * Associates the given value with the given key in this map. The new value is added with an higher rank.
	 *
	 * @param key
	 *           - the key
	 * @param value
	 *           - the value
	 *
	 * @return the previous value, or null if there was no mapping for the key
	 */
	V mergeBefore(K key, V value);

	/**
	 * Merges both maps, all entries from source map are added to this map. The new values are added with an higher rank,
	 * the rank hierarchy from the source map is preserved.
	 *
	 * If the merge function returns null, the mapping is removed.
	 *
	 * @param source
	 *           - the source map
	 * @param mergeFunction
	 *           - the merge function
	 */
	void mergeBefore(final MergeMap<K, V> source, MergeFunction<K, V> mergeFunction);

	/**
	 * Associates the given value with the given key in this map. The new value is added with a lower rank.
	 *
	 * @param key
	 *           - the key
	 * @param value
	 *           - the value
	 *
	 * @return the previous value, or null if there was no mapping for the key
	 */
	V mergeAfter(K key, V value);

	/**
	 * Merges both maps, all entries from source map are added to this map. The new values are added with a lower rank, the
	 * rank hierarchy from the source map is preserved.
	 *
	 * If the merge function returns null, the mapping is removed.
	 *
	 * @param source
	 *           - the source map
	 * @param mergeFunction
	 *           - the merge function
	 */
	void mergeAfter(final MergeMap<K, V> source, MergeFunction<K, V> mergeFunction);

	/**
	 * Returns an ordered {@link List} of the values contained in this map.
	 *
	 * @return an ordered {@link List} of the values contained in this map
	 */
	List<V> orderedValues();
}
