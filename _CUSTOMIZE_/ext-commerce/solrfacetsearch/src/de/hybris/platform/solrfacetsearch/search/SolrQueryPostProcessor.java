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

import org.apache.solr.client.solrj.SolrQuery;


/**
 * APi defining a SOLR query post processor. It provides a direct access to the query right before performing search on
 * solr's index. There could be multiple post-processors that are configured for the {@link SolrQueryConverter}
 * implementation.
 *
 * @deprecated Since 5.7, use a FacetSearchListener instead.
 */
@Deprecated
public interface SolrQueryPostProcessor
{

	/**
	 * Processes the {@link SolrQuery} object according to some custom business logic.
	 * 
	 * @param query
	 *           - {@link SolrQuery} that is being processed
	 * @param solrSearchQuery
	 *           - {@link SearchQuery} the search data from the user request
	 * 
	 * @return {@link SolrQuery} - the outcome of the post-processing
	 */
	SolrQuery process(final SolrQuery query, final SearchQuery solrSearchQuery);

}
