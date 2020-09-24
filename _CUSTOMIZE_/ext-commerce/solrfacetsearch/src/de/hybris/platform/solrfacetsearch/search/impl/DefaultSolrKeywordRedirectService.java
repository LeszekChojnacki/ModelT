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

import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.handler.KeywordRedirectHandler;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;
import de.hybris.platform.solrfacetsearch.search.KeywordRedirectSorter;
import de.hybris.platform.solrfacetsearch.search.KeywordRedirectValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SolrFacetSearchKeywordDao;
import de.hybris.platform.solrfacetsearch.search.SolrKeywordRedirectService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link SolrKeywordRedirectService}
 */
public class DefaultSolrKeywordRedirectService implements SolrKeywordRedirectService
{
	private SolrFacetSearchKeywordDao solrFacetSearchKeywordDao;
	private CommonI18NService commonI18NService;
	private Map<KeywordRedirectMatchType, KeywordRedirectHandler> redirectHandlers;
	private KeywordRedirectSorter keywordRedirectSorter;

	@Override
	public List<KeywordRedirectValue> getKeywordRedirect(final SearchQuery query)
	{
		final String theQuery = query.getUserQuery();
		final List<KeywordRedirectValue> result = new ArrayList<KeywordRedirectValue>();

		if (StringUtils.isNotBlank(theQuery))
		{
			final Collection<SolrFacetSearchKeywordRedirectModel> redirects = findKeywordRedirects(query);

			for (final SolrFacetSearchKeywordRedirectModel redirect : redirects)
			{
				handleKeywordMatch(result, theQuery, redirect);
			}
		}

		return result;
	}

	@Override
	public SolrSearchResult attachKeywordRedirect(final SolrSearchResult searchResult)
	{
		final List<KeywordRedirectValue> result = getSingleKeywordRedirect(searchResult.getSearchQuery());

		if (!result.isEmpty())
		{
			searchResult.setKeywordRedirects(result);
		}

		return searchResult;
	}

	protected List<KeywordRedirectValue> getSingleKeywordRedirect(final SearchQuery query)
	{
		final List<KeywordRedirectValue> keywordRedirects = getKeywordRedirect(query);
		final List<KeywordRedirectValue> result = new ArrayList<KeywordRedirectValue>();

		if (!keywordRedirects.isEmpty())
		{
			final int FIRST_ELEMENT_INDEX = 0; //Only the first keyword redirect match is returned			
			result.add(keywordRedirects.get(FIRST_ELEMENT_INDEX));
		}

		return result;
	}

	protected List<SolrFacetSearchKeywordRedirectModel> findKeywordRedirects(final SearchQuery searchQuery)
	{
		String langIso = searchQuery.getLanguage();
		if (StringUtils.isBlank(langIso))
		{
			langIso = commonI18NService.getCurrentLanguage().getIsocode();
		}

		final List<SolrFacetSearchKeywordRedirectModel> result = solrFacetSearchKeywordDao
				.findKeywords(searchQuery.getFacetSearchConfig().getName(), langIso);

		return keywordRedirectSorter.sort(result);
	}

	protected void handleKeywordMatch(final List<KeywordRedirectValue> result, final String theQuery,
			final SolrFacetSearchKeywordRedirectModel redirect)
	{
		final KeywordRedirectHandler handler = redirectHandlers.get(redirect.getMatchType());
		if (handler != null && handler.keywordMatches(theQuery, redirect.getKeyword(), redirect.getIgnoreCase().booleanValue()))
		{
			result.add(new KeywordRedirectValue(redirect.getKeyword(), redirect.getMatchType(), redirect.getRedirect()));
		}
	}

	@Required
	public void setSolrFacetSearchKeywordDao(final SolrFacetSearchKeywordDao solrFacetSearchKeywordDao)
	{
		this.solrFacetSearchKeywordDao = solrFacetSearchKeywordDao;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setRedirectHandlers(final Map<KeywordRedirectMatchType, KeywordRedirectHandler> redirectHandlers)
	{
		this.redirectHandlers = redirectHandlers;
	}

	@Required
	public void setKeywordRedirectSorter(final KeywordRedirectSorter keywordRedirectSorter)
	{
		this.keywordRedirectSorter = keywordRedirectSorter;
	}

}
