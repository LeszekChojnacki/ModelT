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

import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;


/**
 * Defines API for all post processors operating on SOLR search results. Provides direct access to {@link SearchResult}
 * instance after retrieving search result from SOLR instance.
 *
 *
 *
 * @deprecated Since 5.7, use a FacetSearchListener instead.
 */
@Deprecated
public interface SolrResultPostProcessor
{

	/**
	 * Processes SOLR result object - {@link SolrSearchResult}.
	 *
	 * @param solrSearchResult
	 *           - {@link SolrSearchResult} to process
	 * @return processed {@link SolrSearchResult}
	 */
	SearchResult process(SearchResult solrSearchResult);

}
