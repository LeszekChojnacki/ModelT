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


/**
 * Represents a merge function.
 *
 * @param <K>
 *           - the type of the keys
 * @param <V>
 *           - the type of the values
 *
 * @since 6.7
 */
@FunctionalInterface
public interface MergeFunction<K, V>
{
	/**
	 * Applies this mapping function to the given arguments.
	 *
	 * @param key
	 *           the key
	 * @param oldValue
	 *           the old value
	 * @param value
	 *           the value
	 *
	 * @return the new value
	 */
	V apply(K key, V oldValue, V value);
}
