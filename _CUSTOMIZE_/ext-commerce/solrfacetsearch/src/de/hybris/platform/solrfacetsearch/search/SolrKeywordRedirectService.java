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

import java.util.List;


/**
 * Service to deal with keyword redirects.
 */
public interface SolrKeywordRedirectService
{
	/**
	 * Get keyword redirect for given query. The redirects should be sorted by using {@link KeywordRedirectSorter}.
	 *
	 * @param query
	 *           used to find redirects.
	 *
	 * @return List of {@link KeywordRedirectValue}
	 */
	List<KeywordRedirectValue> getKeywordRedirect(SearchQuery query);

	/**
	 * Attach keyword redirects to a {@link SolrSearchResult}. It use {@link #getKeywordRedirect(SearchQuery)} to get the
	 * list of keywords. If keywords exists in the result new ones will be added and whole list will be sorted.
	 *
	 * @param result
	 *           result to attach redirects.
	 *
	 * @return {@link SolrSearchResult} with attached keyword redirects.
	 */
	SolrSearchResult attachKeywordRedirect(SolrSearchResult result);

}
