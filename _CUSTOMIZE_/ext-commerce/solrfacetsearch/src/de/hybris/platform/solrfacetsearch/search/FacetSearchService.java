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


import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.Map;


/**
 * Implementations of this interface, should be responsible for searching operations.
 */
public interface FacetSearchService
{
	/**
	 * Creates the search query
	 *
	 * @param facetSearchConfig
	 *           the facet search configuration
	 * @param indexedType
	 *           the indexed type
	 * @return the search query
	 */
	SearchQuery createSearchQuery(FacetSearchConfig facetSearchConfig, IndexedType indexedType);

	/**
	 * Creates the search query and populates it
	 *
	 * @param facetSearchConfig
	 *           the facet search configuration
	 * @param indexedType
	 *           the indexed type
	 * @return the search query
	 */
	SearchQuery createPopulatedSearchQuery(FacetSearchConfig facetSearchConfig, IndexedType indexedType);

	/**
	 * Creates the search query, populates, adds free text query fields and user query field
	 *
	 * @param facetSearchConfig
	 *           the facet search configuration
	 * @param indexedType
	 *           the indexed type
	 * @param userQuery
	 *           the user query
	 * @return the search query
	 */
	SearchQuery createFreeTextSearchQuery(FacetSearchConfig facetSearchConfig, IndexedType indexedType, String userQuery);

	/**
	 * Creates the search query from the template, populates, adds free text query fields and user query field
	 *
	 * @param facetSearchConfig
	 *           the facet search configuration
	 * @param indexedType
	 *           the indexed type
	 * @param queryTemplateName
	 *           template name to create a query from
	 * @return the search query
	 */
	SearchQuery createSearchQueryFromTemplate(FacetSearchConfig facetSearchConfig, IndexedType indexedType,
			String queryTemplateName);

	/**
	 * Creates the search query from the template, populates, adds free text query fields and user query field
	 *
	 * @param facetSearchConfig
	 *           the facet search configuration
	 * @param indexedType
	 *           the indexed type
	 * @param queryTemplateName
	 *           template name to create a query from
	 * @param userQuery
	 *           the user query
	 * @return the search query
	 */
	SearchQuery createFreeTextSearchQueryFromTemplate(FacetSearchConfig facetSearchConfig, IndexedType indexedType,
			String queryTemplateName, String userQuery);

	/**
	 * Does the search with the specific search query.
	 *
	 * @param query
	 *           the query to be searched
	 * @return the search result of the given query
	 * @throws FacetSearchException
	 *            throws FacetSearchException if the search query is not correctly configured
	 */
	SearchResult search(SearchQuery query) throws FacetSearchException;

	/**
	 * Same as {@link #search(SearchQuery)} but allows to pass the search hints as parameter.
	 *
	 * @param query
	 *           the query to be searched
	 * @param searchHints
	 *           the search hints
	 * @return the search result of the given query
	 * @throws FacetSearchException
	 *            throws FacetSearchException if the search query is not correctly configured
	 */
	SearchResult search(SearchQuery query, Map<String, String> searchHints) throws FacetSearchException;
}
