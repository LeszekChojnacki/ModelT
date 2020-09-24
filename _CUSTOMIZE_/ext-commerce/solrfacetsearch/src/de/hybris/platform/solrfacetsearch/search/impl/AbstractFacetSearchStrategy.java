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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.QueryMethod;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.Keyword;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQueryCatalogVersionsResolver;
import de.hybris.platform.solrfacetsearch.search.SearchQueryCurrencyResolver;
import de.hybris.platform.solrfacetsearch.search.SearchQueryKeywordsResolver;
import de.hybris.platform.solrfacetsearch.search.SearchQueryLanguageResolver;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.springframework.beans.factory.annotation.Required;


/*
 * Concrete implementations of {@link FacetSearchStrategy} should normally extend this class.
 */
public abstract class AbstractFacetSearchStrategy implements FacetSearchStrategy
{
	protected static final String EXECUTE="execute";

	private SearchQueryLanguageResolver searchQueryLanguageResolver;
	private SearchQueryCurrencyResolver searchQueryCurrencyResolver;
	private SearchQueryKeywordsResolver searchQueryKeywordsResolver;
	private SearchQueryCatalogVersionsResolver searchQueryCatalogVersionsResolver;

	public SearchQueryLanguageResolver getSearchQueryLanguageResolver()
	{
		return searchQueryLanguageResolver;
	}

	@Required
	public void setSearchQueryLanguageResolver(final SearchQueryLanguageResolver searchQueryLanguageResolver)
	{
		this.searchQueryLanguageResolver = searchQueryLanguageResolver;
	}

	public SearchQueryCurrencyResolver getSearchQueryCurrencyResolver()
	{
		return searchQueryCurrencyResolver;
	}

	@Required
	public void setSearchQueryCurrencyResolver(final SearchQueryCurrencyResolver searchQueryCurrencyResolver)
	{
		this.searchQueryCurrencyResolver = searchQueryCurrencyResolver;
	}

	public SearchQueryKeywordsResolver getSearchQueryKeywordsResolver()
	{
		return searchQueryKeywordsResolver;
	}

	@Required
	public void setSearchQueryKeywordsResolver(final SearchQueryKeywordsResolver searchQueryKeywordsResolver)
	{
		this.searchQueryKeywordsResolver = searchQueryKeywordsResolver;
	}

	public SearchQueryCatalogVersionsResolver getSearchQueryCatalogVersionsResolver()
	{
		return searchQueryCatalogVersionsResolver;
	}

	@Required
	public void setSearchQueryCatalogVersionsResolver(final SearchQueryCatalogVersionsResolver searchQueryCatalogVersionsResolver)
	{
		this.searchQueryCatalogVersionsResolver = searchQueryCatalogVersionsResolver;
	}

	protected void checkQuery(final SearchQuery query)
	{
		validateParameterNotNull(query, "Parameter 'query' can not be null!");

		final FacetSearchConfig facetSearchConfig = query.getFacetSearchConfig();
		final IndexedType indexedType = query.getIndexedType();

		checkLanguage(query, facetSearchConfig, indexedType);
		checkCurrency(query, facetSearchConfig, indexedType);
		checkKeywords(query, facetSearchConfig, indexedType);
		checkCatalogVersions(query, facetSearchConfig, indexedType);
	}

	protected void checkLanguage(final SearchQuery query, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		if (query.getLanguage() == null)
		{
			final LanguageModel language = searchQueryLanguageResolver.resolveLanguage(facetSearchConfig, indexedType);
			if (language != null)
			{
				final String languageCode = language.getIsocode();
				if (StringUtils.isNotEmpty(languageCode))
				{
					query.setLanguage(languageCode);
				}
			}
		}
	}

	protected void checkCurrency(final SearchQuery query, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		if (query.getCurrency() == null)
		{
			final CurrencyModel currency = searchQueryCurrencyResolver.resolveCurrency(facetSearchConfig, indexedType);
			if (currency != null)
			{
				final String currencyCode = currency.getIsocode();
				if (StringUtils.isNotEmpty(currencyCode))
				{
					query.setCurrency(currencyCode);
				}
			}
		}
	}

	protected void checkKeywords(final SearchQuery query, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		if (query.getKeywords() == null && StringUtils.isNotBlank(query.getUserQuery()))
		{
			final List<Keyword> keywords = searchQueryKeywordsResolver.resolveKeywords(facetSearchConfig, indexedType,
					query.getUserQuery());
			if (CollectionUtils.isNotEmpty(keywords))
			{
				query.setKeywords(keywords);
			}
		}
	}

	protected void checkCatalogVersions(final SearchQuery query, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType)
	{
		if (CollectionUtils.isEmpty(query.getCatalogVersions()))
		{
			query.setCatalogVersions(searchQueryCatalogVersionsResolver.resolveCatalogVersions(facetSearchConfig, indexedType));
		}
	}

	protected void checkContext(final FacetSearchContext facetSearchContext)
	{
		checkNamedSort(facetSearchContext);
	}

	protected void checkNamedSort(final FacetSearchContext facetSearchContext)
	{
		if(facetSearchContext.getNamedSort() == null)
		{
			if (CollectionUtils.isNotEmpty(facetSearchContext.getAvailableNamedSorts()))
			{
				final SearchQuery searchQuery = facetSearchContext.getSearchQuery();
				final Optional<IndexedTypeSort> first = facetSearchContext.getAvailableNamedSorts().stream()
						.filter(sort -> StringUtils.equals(searchQuery.getNamedSort(), sort.getCode())).findFirst();

				if(first.isPresent())
				{
					facetSearchContext.setNamedSort(first.get());
				}
				else
				{
					facetSearchContext.setNamedSort(facetSearchContext.getAvailableNamedSorts().get(0));
				}
			}
			else
			{
				facetSearchContext.setNamedSort(null);
			}
		}
	}

	protected SolrRequest.METHOD resolveQueryMethod(final FacetSearchConfig facetSearchConfig)
	{
		final QueryMethod queryMethod = facetSearchConfig.getSolrConfig().getQueryMethod();

		return queryMethod == null || queryMethod == QueryMethod.GET ? SolrRequest.METHOD.GET : SolrRequest.METHOD.POST;
	}
}
