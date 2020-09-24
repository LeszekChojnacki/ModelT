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

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;

import java.io.Serializable;


/**
 * Strategies that implement this interface can have the results cached.
 *
 * @param <T>
 *           - the type of the source object
 */
public interface AsCacheAwareStrategy<T>
{
	/**
	 * Returns a cache key fragment. In most cases implementing strategies can simply return <code>null</code>. If
	 * strategies can have results that depend on the context, a different implementation should be provided.
	 *
	 * @param context
	 *           - the search profile context
	 * @param object
	 *           - the source object
	 *
	 * @return the key fragment
	 */
	Serializable getCacheKeyFragment(AsSearchProfileContext context, T object);
}
