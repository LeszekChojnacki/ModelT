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
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.SolrQueryConverter;
import de.hybris.platform.solrfacetsearch.search.SolrResultPostProcessor;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContextFactory;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Required;


/**
 * Legacy implementation of {@link FacetSearchStrategy}. It uses the old query building process for compatibility
 * purposes.
 *
 * @deprecated since 6.4, use DefaultFacetSearchStrategy instead.
 */
@Deprecated
public class LegacyFacetSearchStrategy extends AbstractFacetSearchStrategy
{

	private static final String ENCODING = "UTF-8";
	private static final Logger LOG = Logger.getLogger(LegacyFacetSearchStrategy.class);

	private SolrIndexService solrIndexService;
	private SolrSearchProviderFactory solrSearchProviderFactory;
	private I18NService i18NService;
	private CommonI18NService commonI18NService;
	private FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory;
	private Converter<SearchResultConverterData, SolrSearchResult> facetSearchResultConverter;

	private SolrQueryConverter solrQueryConverter;
	private List<SolrResultPostProcessor> resultPostProcessors;

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18nService)
	{
		i18NService = i18nService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public FacetSearchContextFactory<FacetSearchContext> getFacetSearchContextFactory()
	{
		return facetSearchContextFactory;
	}

	@Required
	public void setFacetSearchContextFactory(final FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory)
	{
		this.facetSearchContextFactory = facetSearchContextFactory;
	}

	public Converter<SearchResultConverterData, SolrSearchResult> getFacetSearchResultConverter()
	{
		return facetSearchResultConverter;
	}

	@Required
	public void setFacetSearchResultConverter(
			final Converter<SearchResultConverterData, SolrSearchResult> facetSearchResultConverter)
	{
		this.facetSearchResultConverter = facetSearchResultConverter;
	}

	public SolrQueryConverter getSolrQueryConverter()
	{
		return solrQueryConverter;
	}

	@Required
	public void setSolrQueryConverter(final SolrQueryConverter solrQueryConverter)
	{
		this.solrQueryConverter = solrQueryConverter;
	}

	public List<SolrResultPostProcessor> getResultPostProcessors()
	{
		return resultPostProcessors;
	}

	public void setResultPostProcessors(final List<SolrResultPostProcessor> resultPostProcessors)
	{
		this.resultPostProcessors = resultPostProcessors;
	}

	@Override
	public SearchResult search(final SearchQuery searchQuery, final Map<String, String> searchHints) throws FacetSearchException
	{
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

			final SearchResult searchResult = doSearch(facetSearchContext, index, solrClient);

			return searchResult;
		}
		catch (FacetSearchException | SolrServiceException | SolrServerException | RuntimeException e)
		{
			facetSearchContextFactory.destroyContext(e);
			throw new FacetSearchException(e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	protected SearchResult doSearch(final FacetSearchContext searchContext, final Index index, final SolrClient solrClient)
			throws FacetSearchException, SolrServerException
	{
		SearchResult searchResult;
		try
		{
			searchResult = queryInternal(searchContext, index, solrClient);
		}
		catch (final SolrException e)
		{
			if (!canUseFallbackLanguage(e))
			{
				throw new FacetSearchException(e.getMessage(), e);
			}

			searchResult = queryUsingFallbackLanguage(searchContext, index, solrClient, e);
		}

		facetSearchContextFactory.getContext().setSearchResult(searchResult);
		facetSearchContextFactory.destroyContext();

		return searchResult;
	}

	protected SearchResult queryInternal(final FacetSearchContext searchContext, final Index index, final SolrClient solrClient)
			throws FacetSearchException, SolrServerException
	{
		final SearchQuery searchQuery = searchContext.getSearchQuery();

		final SolrQuery solrSearchQuery = solrQueryConverter.convertSolrQuery(searchQuery);
		if (LOG.isDebugEnabled())
		{
			try
			{
				LOG.debug("Solr Query: \n" + URLDecoder.decode(solrSearchQuery.toString(), ENCODING));
			}
			catch (final UnsupportedEncodingException ex)
			{
				throw new FacetSearchException(ex);
			}
		}

		try
		{
			final SolrRequest.METHOD method = resolveQueryMethod(searchContext.getFacetSearchConfig());

			final QueryResponse queryResponse = solrClient.query(index.getName(), solrSearchQuery, method);

			final SearchResultConverterData searchResultConverterData = new SearchResultConverterData();
			searchResultConverterData.setFacetSearchContext(searchContext);
			searchResultConverterData.setQueryResponse(queryResponse);

			final SolrSearchResult searchResult = facetSearchResultConverter.convert(searchResultConverterData);
			return applySearchResultsPostProcessors(searchResult);
		}
		catch (final IOException e)
		{
			throw new FacetSearchException(e);
		}
	}

	protected boolean canUseFallbackLanguage(final SolrException exception)
	{
		return exception.getMessage().contains("undefined field") && i18NService.isLocalizationFallbackEnabled();
	}

	protected SearchResult queryUsingFallbackLanguage(final FacetSearchContext searchContext, final Index index,
			final SolrClient solrClient, final SolrException exception) throws FacetSearchException
	{
		final SearchQuery searchQuery = searchContext.getSearchQuery();

		final LanguageModel language = commonI18NService.getLanguage(searchQuery.getLanguage());
		final List<LanguageModel> languages = language.getFallbackLanguages();

		for (final LanguageModel lang : languages)
		{
			searchQuery.setLanguage(lang.getIsocode());
			try
			{
				return queryInternal(searchContext, index, solrClient);
			}
			catch (final SolrException | SolrServerException ex)
			{
				LOG.warn(ex);
			}
		}
		throw new FacetSearchException("Cannot query using fallback languages: " + languages, exception);
	}

	/**
	 * Apply search result post processors, which provide direct access to {@link SolrSearchResult} instance and allow
	 * implementing any search result oriented logic.
	 */
	protected SearchResult applySearchResultsPostProcessors(final SearchResult searchResult)
	{

		SearchResult postProcessingResult = searchResult;
		for (final SolrResultPostProcessor postProcessor : getResultPostProcessors())
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Processing solr search result post-processor : " + postProcessor.getClass());
			}
			postProcessingResult = postProcessor.process(postProcessingResult);
		}
		return postProcessingResult;

	}

	public String convertSearchQueryToString(final SearchQuery query) throws FacetSearchException
	{
		checkQuery(query);
		return getSolrQueryConverter().convertSolrQuery(query).toString();
	}
}
