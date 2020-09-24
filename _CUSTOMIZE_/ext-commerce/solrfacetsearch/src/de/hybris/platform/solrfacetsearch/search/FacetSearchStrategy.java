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

import java.util.Map;


/**
 * Interface for searching strategies
 */
public interface FacetSearchStrategy
{
	/**
	 * @param query
	 *           The searchQuery object carrying the query configuration and parameters
	 * @param searchHints
	 *           The search hints
	 *
	 * @return SearchResult object
	 *
	 * @throws FacetSearchException
	 */
	SearchResult search(SearchQuery query, Map<String, String> searchHints) throws FacetSearchException;
}
