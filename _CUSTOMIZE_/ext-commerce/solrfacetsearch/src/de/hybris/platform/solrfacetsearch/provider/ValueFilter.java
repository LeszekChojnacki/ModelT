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
package de.hybris.platform.solrfacetsearch.provider;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;


/**
 * Value filter
 * Used by the resolvers to
 */
public interface ValueFilter
{
	/**
	 * @param batchContext
	 * 		- The batch context
	 * @param indexedProperty
	 * 		- The indexed properties that should contains the Bean Id of the formatter bean
	 * @param value
	 * 		- The object to apply the filter on
	 * @return Object value after applying the filter
	 */
	Object doFilter(IndexerBatchContext batchContext, IndexedProperty indexedProperty, Object value);
}
