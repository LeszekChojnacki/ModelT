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

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContextFactory;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.IOUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link FacetSearchStrategy}.
 */
public class DefaultFacetSearchStrategy extends AbstractFacetSearchStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSearchStrategy.class);

	private SolrSearchProviderFactory solrSearchProviderFactory;
	private FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory;

	private Converter<SearchQueryConverterData, SolrQuery> facetSearchQueryConverter;
	private Converter<SearchResultConverterData, SearchResult> facetSearchResultConverter;

	private SolrIndexService solrIndexService;

	public FacetSearchContextFactory<FacetSearchContext> getFacetSearchContextFactory()
	{
		return facetSearchContextFactory;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	@Required
	public void setFacetSearchContextFactory(final FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory)
	{
		this.facetSearchContextFactory = facetSearchContextFactory;
	}

	public Converter<SearchQueryConverterData, SolrQuery> getFacetSearchQueryConverter()
	{
		return facetSearchQueryConverter;
	}

	@Required
	public void setFacetSearchQueryConverter(final Converter<SearchQueryConverterData, SolrQuery> facetSearchQueryConverter)
	{
		this.facetSearchQueryConverter = facetSearchQueryConverter;
	}

	public Converter<SearchResultConverterData, SearchResult> getFacetSearchResultConverter()
	{
		return facetSearchResultConverter;
	}

	@Required
	public void setFacetSearchResultConverter(final Converter<SearchResultConverterData, SearchResult> facetSearchResultConverter)
	{
		this.facetSearchResultConverter = facetSearchResultConverter;
	}

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	@Override
	public SearchResult search(final SearchQuery searchQuery, final Map<String, String> searchHints) throws FacetSearchException
	{
		//	check the SearchQuery object
		checkQuery(searchQuery);

		SolrClient solrClient = null;

		try
		{
			final FacetSearchConfig facetSearchConfig = searchQuery.getFacetSearchConfig();
			final IndexedType indexedType = searchQuery.getIndexedType();

			final FacetSearchContext facetSearchContext = facetSearchContextFactory.createContext(facetSearchConfig, indexedType,
					searchQuery);
			facetSearchContext.getSearchHints().putAll(searchHints);

			facetSearchContextFactory.initializeContext();
			checkContext(facetSearchContext);

			if (MapUtils.isNotEmpty(searchHints))
			{
				final boolean execute = Boolean.parseBoolean(searchHints.get(EXECUTE));
				if (!execute)
				{
					return null;
				}
			}

			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);

			final SolrIndexModel activeIndex = solrIndexService.getActiveIndex(facetSearchConfig.getName(),
					indexedType.getIdentifier());
			final Index index = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, activeIndex.getQualifier());
			solrClient = solrSearchProvider.getClient(index);

			//	convert from hybris query to solr query
			final SearchQueryConverterData searchQueryConverterData = new SearchQueryConverterData();
			searchQueryConverterData.setFacetSearchContext(facetSearchContext);
			searchQueryConverterData.setSearchQuery(searchQuery);

			final SolrQuery solrQuery = facetSearchQueryConverter.convert(searchQueryConverterData);

			if (LOG.isDebugEnabled())
			{
				LOG.debug(solrQuery);
			}

			final SolrRequest.METHOD method = resolveQueryMethod(facetSearchConfig);

			final QueryResponse queryResponse = solrClient.query(index.getName(), solrQuery, method);

			// convert from solr response to hybris response
			final SearchResultConverterData searchResultConverterData = new SearchResultConverterData();
			searchResultConverterData.setFacetSearchContext(facetSearchContext);
			searchResultConverterData.setQueryResponse(queryResponse);

			final SearchResult searchResult = facetSearchResultConverter.convert(searchResultConverterData);

			facetSearchContextFactory.getContext().setSearchResult(searchResult);
			facetSearchContextFactory.destroyContext();

			return searchResult;
		}
		catch (SolrServiceException | SolrServerException | IOException | RuntimeException e)
		{
			facetSearchContextFactory.destroyContext(e);
			throw new FacetSearchException(e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}
}
