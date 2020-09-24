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

import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;

import java.util.List;


/**
 * Dao to manage keywords.
 */
public interface SolrFacetSearchKeywordDao
{

	/**
	 * Finds the keywords for given configuration and language.
	 * 
	 * @param facetSearchConfigName
	 *           configuration name.
	 * @param langIso
	 *           language.
	 * @return collection of keywords.
	 */
	List<SolrFacetSearchKeywordRedirectModel> findKeywords(String facetSearchConfigName, String langIso);

	/**
	 * Finds the keywords for given configuration, language, keyword and matchtype.
	 * 
	 * @param keyword
	 *           keyword string.
	 * @param matchType
	 *           match type.
	 * @param facetSearchConfigName
	 *           configuration.
	 * @param langIso
	 *           language.
	 */
	List<SolrFacetSearchKeywordRedirectModel> findKeywords(String keyword, KeywordRedirectMatchType matchType,
			String facetSearchConfigName, String langIso);

}
