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
package com.hybris.backoffice.solrsearch.services;

import de.hybris.platform.solrfacetsearch.search.FacetSearchService;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.cockpitng.search.data.SearchQueryData;


/**
 * {@inheritDoc}
 */
public interface BackofficeFacetSearchService extends FacetSearchService
{

	/**
	 * Creates the search query based on query data
	 *
	 * @param queryData
	 *           query data on which created search query should be based
	 * @return the search query
	 */
	BackofficeSearchQuery createBackofficeSolrSearchQuery(final SearchQueryData queryData);

}
