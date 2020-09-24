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

import de.hybris.platform.core.PK;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.enums.ConverterType;
import de.hybris.platform.solrfacetsearch.loader.ModelLoader;
import de.hybris.platform.solrfacetsearch.loader.ModelLoadingException;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;
import de.hybris.platform.solrfacetsearch.search.Breadcrumb;
import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.KeywordRedirectValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.SearchResultGroupCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;


public class SolrSearchResult implements SearchResult, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(SolrSearchResult.class);

	private ConvertersMapping convertersMapping;

	private SearchQuery searchQuery;
	private QueryResponse queryResponse;

	private long numberOfResults;
	private List<Document> documents;
	private List<SolrDocument> solrDocuments;
	private List<SearchResultGroupCommand> groupCommands;
	private Map<String, Facet> facets;

	private List<Breadcrumb> breadcrumbs;
	private List<KeywordRedirectValue> keywordRedirects;

	private List<IndexedTypeSort> availableNamedSorts;

	private IndexedTypeSort currentNamedSort;

	private final transient Map<String, Object> attributes;

	public SolrSearchResult()
	{
		numberOfResults = 0;
		documents = new ArrayList<>();
		groupCommands = new ArrayList<>();
		facets = new LinkedHashMap<>();

		breadcrumbs = new ArrayList<>();
		keywordRedirects = new ArrayList<>();
		availableNamedSorts = new ArrayList<>();

		attributes = new LinkedHashMap<>();
	}

	public ConvertersMapping getConvertersMapping()
	{
		return convertersMapping;
	}

	public void setConvertersMapping(final ConvertersMapping convertersMapping)
	{
		this.convertersMapping = convertersMapping;
	}

	public SearchQuery getSearchQuery()
	{
		return searchQuery;
	}

	public void setSearchQuery(final SearchQuery searchQuery)
	{
		this.searchQuery = searchQuery;
	}

	public QueryResponse getQueryResponse()
	{
		return queryResponse;
	}

	public void setQueryResponse(final QueryResponse queryResponse)
	{
		this.queryResponse = queryResponse;
	}

	@Override
	public long getNumberOfResults()
	{
		return numberOfResults;
	}

	public void setNumberOfResults(final long numberOfResults)
	{
		this.numberOfResults = numberOfResults;
	}

	@Override
	public List<Document> getDocuments()
	{
		return documents;
	}

	public void setDocuments(final List<Document> documents)
	{
		this.documents = documents;
	}

	public List<SolrDocument> getSolrDocuments()
	{
		return solrDocuments;
	}

	public void setSolrDocuments(final List<SolrDocument> solrDocuments)
	{
		this.solrDocuments = solrDocuments;
	}

	@Override
	public List<SearchResultGroupCommand> getGroupCommands()
	{
		return groupCommands;
	}

	public void setGroupCommands(final List<SearchResultGroupCommand> groupCommands)
	{
		this.groupCommands = groupCommands;
	}

	public Map<String, Facet> getFacetsMap()
	{
		return facets;
	}

	public void setFacetsMap(final Map<String, Facet> facets)
	{
		this.facets = facets;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs()
	{
		return breadcrumbs;
	}

	public void setBreadcrumbs(final List<Breadcrumb> breadcrumbs)
	{
		this.breadcrumbs = breadcrumbs;
	}

	@Override
	public List<KeywordRedirectValue> getKeywordRedirects()
	{
		return this.keywordRedirects;
	}

	@Override
	public List<IndexedTypeSort> getAvailableNamedSorts() {
		return availableNamedSorts;
	}

	public void setAvailableNamedSorts(final List<IndexedTypeSort> availableNamedSorts) {
		this.availableNamedSorts = availableNamedSorts;
	}

	@Override
	public IndexedTypeSort getCurrentNamedSort() {
		return currentNamedSort;
	}

	public void setCurrentNamedSort(final IndexedTypeSort currentNamedSort) {
		this.currentNamedSort = currentNamedSort;
	}

	public void setKeywordRedirects(final List<KeywordRedirectValue> keywordRedirects)
	{
		this.keywordRedirects = keywordRedirects;
	}

	public FacetSearchConfig getFacetSearchConfig()
	{
		return searchQuery.getFacetSearchConfig();
	}

	public IndexedType getIndexedType()
	{
		return searchQuery.getIndexedType();
	}

	@Override
	public int getOffset()
	{
		return searchQuery.getOffset();
	}

	@Override
	public int getPageSize()
	{
		return searchQuery.getPageSize();
	}

	@Override
	public boolean hasNext()
	{
		return (getOffset() + 1) * getPageSize() < getNumberOfResults();
	}

	@Override
	public boolean hasPrevious()
	{
		return getOffset() > 0;
	}

	@Override
	public long getNumberOfPages()
	{
		final int pageSize = getPageSize();
		return (getNumberOfResults() + pageSize - 1) / pageSize;
	}

	@Override
	public List<String> getIdentifiers()
	{
		// Retrieve the identifiers of the matching documents
		final List<String> identifiers = new ArrayList<String>();

		for (final SolrDocument solrDocument : solrDocuments)
		{
			identifiers.add((String) solrDocument.getFieldValue("id"));
		}

		return identifiers;
	}

	@Override
	public List<PK> getResultPKs() throws FacetSearchException
	{
		if (solrDocuments == null)
		{
			throw new IllegalStateException("Collection of Solr Documents must not be null");
		}

		if (solrDocuments.isEmpty())
		{
			return Collections.emptyList();
		}

		final List<PK> result = new ArrayList<PK>(solrDocuments.size());
		for (final SolrDocument solrDocument : solrDocuments)
		{
			final Long pk = (Long) solrDocument.getFirstValue("pk");
			if (pk == null)
			{
				throw new FacetSearchException("SolrDocument does not contain field 'pk'");
			}
			result.add(PK.fromLong(pk.longValue()));
		}
		return result;
	}

	@Override
	public List<String> getResultCodes() throws FacetSearchException
	{
		try
		{
			final ModelLoader<?> modelLoader = getModelLoader(searchQuery.getIndexedType());
			return modelLoader.loadCodes(solrDocuments);
		}
		catch (final ModelLoadingException e)
		{
			throw new FacetSearchException(e.getMessage(), e);
		}
	}

	@Override
	public List<? extends ItemModel> getResults() throws FacetSearchException
	{
		try
		{
			final ModelLoader<ItemModel> modelLoader = getModelLoader(searchQuery.getIndexedType());
			return modelLoader.loadModels(solrDocuments);
		}
		catch (final ModelLoadingException e)
		{
			throw new FacetSearchException(e.getMessage(), e);
		}
	}

	/**
	 * @return unmodifiable, lazy list of search result DTO's
	 * @throws IllegalStateException
	 *            when no converter was registered for result type or query was not passed in the constructor
	 */
	@Override
	public <T> List<T> getResultData(final ConverterType converterType)
	{
		if (getSearchQuery() == null)
		{
			throw new IllegalStateException("Query must be set to use result converters");
		}

		final Converter<SolrResult, T> solrResultConverter = getConverter(converterType);
		if (solrResultConverter == null)
		{
			throw new IllegalStateException("Result converter must be registered.");
		}

		if (solrDocuments == null || solrDocuments.isEmpty())
		{
			return Collections.<T> emptyList();
		}

		final List<T> resultData = new ArrayList<T>(solrDocuments.size());

		for (final SolrDocument document : solrDocuments)
		{
			resultData.add(solrResultConverter.convert(new SolrResult(document, getSearchQuery())));
		}
		return resultData;
	}

	public void addFacet(final Facet facet)
	{
		facets.put(facet.getName(), facet);
	}

	@Override
	public Set<String> getFacetNames()
	{
		return facets.keySet();
	}

	@Override
	public boolean containsFacet(final String name)
	{
		return facets.containsKey(name);
	}

	@Override
	public Facet getFacet(final String name)
	{
		final Facet facet = facets.get(name);
		if (facet == null)
		{
			LOG.debug("No facet with name [" + name + "] found. Return null.");
		}
		return facet;
	}

	@Override
	public List<Facet> getFacets()
	{
		return new ArrayList<Facet>(facets.values());
	}

	@Override
	public String getSpellingSuggestion()
	{
		if (this.getQueryResponse() != null && this.getQueryResponse().getSpellCheckResponse() != null)
		{
			return this.getQueryResponse().getSpellCheckResponse().getCollatedResult();
		}
		return null;
	}

	@Override
	public SearchQueryInfo getQueryInfo()
	{
		final NamedList object = (NamedList) this.queryResponse.getResponseHeader().get("params");
		final String query = (String) object.get("q");
		return new SearchQueryInfo(query, this.getNumberOfResults(), searchQuery.getFacetSearchConfig().getName(),
				searchQuery.getLanguage(), Calendar.getInstance().getTime());
	}

	@Override
	public QueryResponse getSolrObject()
	{
		return queryResponse;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	protected Converter getConverter(final ConverterType converterType)
	{
		if (converterType == null || convertersMapping == null || convertersMapping.getConverterForType(converterType) == null)
		{
			final String name = searchQuery.getIndexedType().getSolrResultConverter();
			return name == null ? null : Registry.getApplicationContext().getBean(name, Converter.class);
		}
		return convertersMapping.getConverterForType(converterType);
	}

	protected ModelLoader getModelLoader(final IndexedType indexType)
	{
		final String name = indexType.getModelLoader();
		return name == null ? null : Registry.getApplicationContext().getBean(name, ModelLoader.class);
	}
}
