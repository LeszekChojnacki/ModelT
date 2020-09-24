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
package de.hybris.platform.solrfacetsearch.search;

/**
 * Implementations of this interface are responsible for building lucene query string.
 */
public interface FreeTextQueryBuilder
{
	/**
	 * Add a free text query to the search query.
	 *
	 * @param searchQuery
	 * 		The search query to add search terms to
	 */
	String buildQuery(final SearchQuery searchQuery);
}
