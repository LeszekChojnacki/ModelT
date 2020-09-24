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

import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;

import java.util.List;


/**
 * Sort bean for list of {@link KeywordRedirectValue}.
 */
public interface KeywordRedirectSorter
{
	/**
	 * Takes a keywordRedirectSet and return sorted list of the same type.
	 * 
	 * @param keywordRedirectList
	 *           list to sort.
	 */
	List<SolrFacetSearchKeywordRedirectModel> sort(List<SolrFacetSearchKeywordRedirectModel> keywordRedirectList);
}
