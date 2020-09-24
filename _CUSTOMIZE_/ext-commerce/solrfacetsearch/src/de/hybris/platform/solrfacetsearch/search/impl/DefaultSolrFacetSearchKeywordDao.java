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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;
import de.hybris.platform.solrfacetsearch.search.SolrFacetSearchKeywordDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class DefaultSolrFacetSearchKeywordDao implements SolrFacetSearchKeywordDao
{

	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<SolrFacetSearchKeywordRedirectModel> findKeywords(final String facetSearchConfigName, final String langIso)
	{
		final StringBuilder query = new StringBuilder();
		query.append("SELECT {rd.").append(SolrFacetSearchKeywordRedirectModel.PK).append("}");
		query.append(" FROM {").append(SolrFacetSearchConfigModel._TYPECODE).append(" as cfg ");
		query.append(" JOIN ").append(SolrFacetSearchKeywordRedirectModel._TYPECODE).append(" as rd ");
		query.append(" ON {cfg.").append(SolrFacetSearchConfigModel.PK).append("} = {rd.")
				.append(SolrFacetSearchKeywordRedirectModel.FACETSEARCHCONFIG).append("} ");
		query.append(" JOIN ").append(LanguageModel._TYPECODE).append(" as lang ");
		query.append(" ON  {lang.").append(LanguageModel.PK).append("} = {rd.")
				.append(SolrFacetSearchKeywordRedirectModel.LANGUAGE).append("}}");
		query.append(" WHERE {cfg.").append(SolrFacetSearchConfigModel.NAME).append("} = ?name ");
		query.append("   AND {lang.").append(LanguageModel.ISOCODE).append("} = ?iso ");

		final Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("iso", langIso);
		queryParams.put("name", facetSearchConfigName);
		final SearchResult<SolrFacetSearchKeywordRedirectModel> result = flexibleSearchService
				.<SolrFacetSearchKeywordRedirectModel> search(new FlexibleSearchQuery(query.toString(), queryParams));
		return result.getResult();
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	@Override
	public List<SolrFacetSearchKeywordRedirectModel> findKeywords(final String keyword, final KeywordRedirectMatchType matchType,
			final String facetSearchConfigName, final String langIso)
	{
		final StringBuilder query = new StringBuilder();
		query.append("SELECT {rd.").append(SolrFacetSearchKeywordRedirectModel.PK).append("}");
		query.append("  FROM {").append(SolrFacetSearchConfigModel._TYPECODE).append(" as cfg ");
		query.append("  JOIN ").append(SolrFacetSearchKeywordRedirectModel._TYPECODE).append(" as rd ");
		query.append("    ON {cfg.").append(SolrFacetSearchConfigModel.PK).append("} = {rd.")
				.append(SolrFacetSearchKeywordRedirectModel.FACETSEARCHCONFIG).append("} ");
		query.append("  JOIN ").append(LanguageModel._TYPECODE).append(" as lang ");
		query.append("    ON {lang.").append(LanguageModel.PK).append("} = {rd.")
				.append(SolrFacetSearchKeywordRedirectModel.LANGUAGE).append("}}");
		query.append(" WHERE {cfg.").append(SolrFacetSearchConfigModel.NAME).append("} = ?name ");
		query.append("   AND {lang.").append(LanguageModel.ISOCODE).append("} = ?iso ");
		query.append("   AND {rd.").append(SolrFacetSearchKeywordRedirectModel.KEYWORD).append("} = ?keyword ");
		query.append("   AND {rd.").append(SolrFacetSearchKeywordRedirectModel.MATCHTYPE).append("} = ?match ");

		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("iso", langIso);
		queryParams.put("name", facetSearchConfigName);
		queryParams.put("match", matchType);
		queryParams.put("keyword", keyword);

		final SearchResult<SolrFacetSearchKeywordRedirectModel> result = flexibleSearchService
				.<SolrFacetSearchKeywordRedirectModel> search(new FlexibleSearchQuery(query.toString(), queryParams));
		return result.getResult();
	}

}
