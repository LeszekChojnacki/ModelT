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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;


public class FacetSearchQuerySpellcheckPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	public static final String SPELLCHECK_PARAM = "spellcheck";
	public static final String SPELLCHECK_QUERY_PARAM = "spellcheck.q";
	public static final String SPELLCHECK_DICTIONARY_PARAM = "spellcheck.dictionary";
	public static final String SPELLCHECK_COLLATE_PARAM = "spellcheck.collate";

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		if (searchQuery.isEnableSpellcheck() && StringUtils.isNotBlank(searchQuery.getUserQuery()))
		{
			target.add(SPELLCHECK_PARAM, "true");
			target.add(SPELLCHECK_QUERY_PARAM, searchQuery.getUserQuery());
			target.add(SPELLCHECK_DICTIONARY_PARAM, searchQuery.getLanguage());
			target.add(SPELLCHECK_COLLATE_PARAM, Boolean.TRUE.toString());
		}
	}
}
