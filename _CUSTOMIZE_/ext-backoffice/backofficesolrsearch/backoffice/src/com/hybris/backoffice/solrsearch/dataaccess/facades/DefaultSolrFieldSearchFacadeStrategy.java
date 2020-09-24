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
package com.hybris.backoffice.solrsearch.dataaccess.facades;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.suggester.SolrAutoSuggestService;
import de.hybris.platform.solrfacetsearch.suggester.SolrSuggestion;
import de.hybris.platform.solrfacetsearch.suggester.exceptions.SolrAutoSuggestException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.converters.FullTextSearchDataConverter;
import com.hybris.backoffice.solrsearch.daos.SolrFieldSearchDAO;
import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchService;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchMode;
import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.backoffice.widgets.advancedsearch.engine.PageableWithFullTextDataCallback;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.search.AutosuggestionSupport;
import com.hybris.cockpitng.dataaccess.facades.search.FieldSearchFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.search.OrderedFieldSearchFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.search.data.AutosuggestionQueryData;
import com.hybris.cockpitng.search.data.FullTextSearchData;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.pageable.FullTextSearchPageable;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;
import com.hybris.cockpitng.widgets.collectionbrowser.CollectionBrowserController;


public class DefaultSolrFieldSearchFacadeStrategy<T extends ItemModel> implements AutosuggestionSupport,
		OrderedFieldSearchFacadeStrategy<T>
{

	/**
	 * Strategy name to be used in configuration when preferred search strategy is to be changed
	 */
	public static final String STRATEGY_NAME = "solr";

	private static final Logger LOG = LoggerFactory.getLogger(DefaultSolrFieldSearchFacadeStrategy.class);
	private BackofficeFacetSearchService facetSearchService;
	private BackofficeFacetSearchConfigService facetSearchConfigService;
	private CommonI18NService commonI18NService;
	private SolrFieldSearchDAO solrFieldSearchDAO;
	private SolrAutoSuggestService solrAutoSuggestService;
	private FullTextSearchDataConverter fullTextSearchDataConverter;
	private int strategyLoadOrder;


	@Override
	public boolean canHandle(final String typeCode)
	{
		return canHandle(typeCode, new DefaultContext());
	}

	@Override
	public boolean canHandle(final String typeCode, final Context context)
	{
		if (context != null)
		{
			if (context.getAttribute(FieldSearchFacadeStrategy.CONTEXT_ORIGINAL_QUERY) != null)
			{
				final Object query = context.getAttribute(FieldSearchFacadeStrategy.CONTEXT_ORIGINAL_QUERY);
				if (query instanceof AdvancedSearchQueryData
						&& ((AdvancedSearchQueryData) query).getAdvancedSearchMode() != AdvancedSearchMode.SIMPLE)
				{
					return false;
				}
			}

			final Object modelPageable = context.getAttribute(CollectionBrowserController.MODEL_PAGEABLE);
			if (modelPageable != null)
			{
				final boolean isSolrPageable = modelPageable instanceof PageableWithFullTextDataCallback
						&& ((PageableWithFullTextDataCallback) modelPageable).getPageable() instanceof BackofficeSolrPageable;
				return isSolrPageable && facetSearchConfigService.isSolrSearchConfiguredForType(typeCode);
			}
		}
		return facetSearchConfigService.isSolrSearchConfiguredForType(typeCode);
	}

	@Override
	public boolean isSortable(final DataType type, final String attributeQualifier, final Context context)
	{
		return false;
	}

	@Override
	public Pageable<T> search(final SearchQueryData queryData)
	{
		return queryData != null ? new BackofficeSolrPageable<>(queryData) : new PageableList<>(Collections.emptyList(), 1);
	}

	@Override
	public Map<String, Collection<String>> getAutosuggestionsForQuery(final AutosuggestionQueryData queryData)
	{
		final SolrIndexedTypeModel indexedType = facetSearchConfigService.getSolrIndexedType(queryData.getSearchType());
		if (indexedType != null)
		{
			try
			{
				final SolrSuggestion solrSuggestion = solrAutoSuggestService.getAutoSuggestionsForQuery(
						commonI18NService.getCurrentLanguage(), indexedType, queryData.getQueryText());
				return solrSuggestion.getSuggestions();
			}
			catch (final SolrAutoSuggestException e)
			{
				LOG.warn("Couldn't retrieve auto suggestions for query: {}, and type: {}", queryData.getQueryText(),
						queryData.getSearchType(), e);
			}
		}
		return Collections.emptyMap();
	}

	@Required
	public void setFacetSearchService(final BackofficeFacetSearchService facetSearchService)
	{
		this.facetSearchService = facetSearchService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setFacetSearchConfigService(final BackofficeFacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Required
	public void setSolrFieldSearchDAO(final SolrFieldSearchDAO solrFieldSearchDAO)
	{
		this.solrFieldSearchDAO = solrFieldSearchDAO;
	}

	@Required
	public void setSolrAutoSuggestService(final SolrAutoSuggestService solrAutoSuggestService)
	{
		this.solrAutoSuggestService = solrAutoSuggestService;
	}

	@Required
	public void setFullTextSearchDataConverter(final FullTextSearchDataConverter fullTextSearchDataConverter)
	{
		this.fullTextSearchDataConverter = fullTextSearchDataConverter;
	}

	@Override
	public int getOrder()
	{
		return strategyLoadOrder;
	}

	public void setOrder(final int order)
	{
		this.strategyLoadOrder = order;
	}

	@Override
	public String getStrategyName()
	{
		return STRATEGY_NAME;
	}


	private class BackofficeSolrPageable<P> implements FullTextSearchPageable<P>
	{

		private final SearchQueryData searchQueryData;
		private List<P> currentPageCache;
		private int pageSize;
		private int totalCount = 0;
		private int currentStart = 0;
		private boolean initialized;
		private String typeCode;
		private FullTextSearchData fullTextSearchData;

		public BackofficeSolrPageable(final SearchQueryData searchQueryData)
		{
			if (searchQueryData != null)
			{
				typeCode = searchQueryData.getSearchType();
				pageSize = searchQueryData.getPageSize();
			}

			this.searchQueryData = searchQueryData;
		}

		private void initialize()
		{
			if (!initialized)
			{
				getCurrentPage();
				initialized = true;
			}
		}

		@Override
		public List<P> getCurrentPage()
		{
			if (currentPageCache == null)
			{
				final List<P> result = getResults(createSearchQuery(pageSize, currentStart));

				initialized = true;
				cacheCurrentPage(result);
				return result;
			}
			return currentPageCache;
		}

		private List<P> getResults(final BackofficeSearchQuery searchQuery)
		{
			try
			{
				final SearchResult searchResult = facetSearchService.search(searchQuery);
				fullTextSearchData = prepareFullTextSearchData(searchResult, searchQuery.getIndexedType());
				final List<PK> resultPKs = searchResult.getResultPKs();
				final List<Long> pksLong = resultPKs.stream().map(PK::getLong).collect(Collectors.toList());

				final List result;
				if (pksLong.isEmpty())
				{
					result = Collections.emptyList();
					currentStart = 0;
					totalCount = 0;
				}
				else
				{
					result = solrFieldSearchDAO.findAll(typeCode, pksLong);
					currentStart = searchResult.getOffset();
					totalCount = (int) searchResult.getNumberOfResults();
				}

				if (resultPKs.size() != result.size())
				{
					LOG.warn("Solr query returned {} pks, flexibleSearch found {} items. Probable cause is that solr has documents "
							+ "in the index for items which have been removed from the platform or some restrictions are applied "
							+ "on products search", Integer.valueOf(resultPKs.size()), Integer.valueOf(result.size()));
				}
				return result;
			}
			catch (final FacetSearchException e)
			{
				LOG.error("Facet search has failed", e);
			}
			return Collections.emptyList();
		}

		private FullTextSearchData prepareFullTextSearchData(final SearchResult searchResult, final IndexedType indexedType)
		{
			final String autocorrection = searchResult.getSpellingSuggestion();
			final Collection<FacetData> facets = fullTextSearchDataConverter.convertFacets(searchResult.getFacets(),
					searchResult.getBreadcrumbs(), indexedType);
			return new FullTextSearchData(facets, autocorrection);
		}

		private void cacheCurrentPage(final List<P> result)
		{
			currentPageCache = result;
		}

		@Override
		public void refresh()
		{
			invalidateCurrentPageCache();
			initialized = false;
		}

		private void invalidateCurrentPageCache()
		{
			currentPageCache = null;
		}

		@Override
		public int getPageSize()
		{
			return pageSize;
		}

		@Override
		public String getTypeCode()
		{
			return typeCode;
		}

		@Override
		public boolean hasNextPage()
		{
			if (pageSize <= 0)
			{
				return false;
			}
			initialize();
			return totalCount > (currentStart + pageSize);
		}

		@Override
		public List<P> nextPage()
		{
			if (hasNextPage())
			{
				currentStart += pageSize;
				invalidateCurrentPageCache();
				return getCurrentPage();
			}
			return Collections.emptyList();
		}

		@Override
		public boolean hasPreviousPage()
		{
			initialize();
			return currentStart > 0;
		}

		@Override
		public List<P> previousPage()
		{
			if (hasPreviousPage())
			{
				currentStart -= pageSize;
				if (currentStart < 0)
				{
					currentStart = 0;
				}
				invalidateCurrentPageCache();
				return getCurrentPage();
			}
			return Collections.emptyList();
		}

		@Override
		public List<P> setPageSize(final int pageSize)
		{
			if (this.pageSize != pageSize)
			{
				this.pageSize = pageSize;
				invalidateCurrentPageCache();
			}
			return getCurrentPage();
		}

		@Override
		public int getTotalCount()
		{
			initialize();
			return totalCount;
		}

		@Override
		public int getPageNumber()
		{
			initialize();
			return currentStart;
		}

		@Override
		public void setPageNumber(final int pageNo)
		{
			initialize();
			if (pageNo != currentStart)
			{
				currentStart = pageNo;
				invalidateCurrentPageCache();
			}
		}

		@Override
		public SortData getSortData()
		{
			return searchQueryData.getSortData();
		}

		@Override
		public void setSortData(final SortData sortData)
		{
			searchQueryData.setSortData(sortData);
			invalidateCurrentPageCache();
		}

		@Override
		public List<P> getAllResults()
		{
			final int zeroOffset = 0;
			return getResults(createSearchQuery(getTotalCount(), zeroOffset));
		}

		@Override
		public FullTextSearchData getFullTextSearchData()
		{
			return fullTextSearchData;
		}

		protected BackofficeSearchQuery createSearchQuery(final int pageSize, final int offset)
		{
			final BackofficeSearchQuery solrSearchQuery = facetSearchService.createBackofficeSolrSearchQuery(searchQueryData);
			solrSearchQuery.setPageSize(pageSize);
			solrSearchQuery.setOffset(offset);
			return solrSearchQuery;
		}
	}

}
