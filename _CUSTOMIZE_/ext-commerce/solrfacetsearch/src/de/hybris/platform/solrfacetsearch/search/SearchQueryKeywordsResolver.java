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

import java.util.List;


/**
 * Implementations of this interface should provide the set of keywords based on the user query.
 */
public interface SearchQueryKeywordsResolver
{
	/**
	 * Method to resolve the keywords
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param userQuery
	 *           - the user query
	 *
	 * @return the resolved keywords
	 */
	List<Keyword> resolveKeywords(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, String userQuery);
}
