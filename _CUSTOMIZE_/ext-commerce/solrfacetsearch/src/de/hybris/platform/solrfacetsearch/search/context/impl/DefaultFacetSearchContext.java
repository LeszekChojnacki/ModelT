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
package de.hybris.platform.solrfacetsearch.search.context.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link FacetSearchContext}.
 */
public class DefaultFacetSearchContext implements FacetSearchContext
{
	private FacetSearchConfig facetSearchConfig;
	private IndexedType indexedType;
	private SearchQuery searchQuery;
	private SearchResult searchResult;
	private Collection<CatalogVersionModel> parentSessionCatalogVersions;
	private final Map<String, String> searchHints;

	private Status status;
	private final Map<String, Object> attributes;
	private final List<Exception> failureExceptions;

	private List<IndexedTypeSort> availableNamedSorts;
	private IndexedTypeSort namedSort;

	public DefaultFacetSearchContext()
	{
		searchHints = new HashMap<String, String>();
		attributes = new HashMap<String, Object>();
		failureExceptions = new ArrayList<Exception>();
		availableNamedSorts = new ArrayList<>();
	}

	@Override
	public FacetSearchConfig getFacetSearchConfig()
	{
		return facetSearchConfig;
	}

	public void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig)
	{
		this.facetSearchConfig = facetSearchConfig;
	}

	@Override
	public IndexedType getIndexedType()
	{
		return indexedType;
	}

	public void setIndexedType(final IndexedType indexedType)
	{
		this.indexedType = indexedType;
	}

	@Override
	public SearchQuery getSearchQuery()
	{
		return searchQuery;
	}

	public void setSearchQuery(final SearchQuery searchQuery)
	{
		this.searchQuery = searchQuery;
	}

	@Override
	public SearchResult getSearchResult()
	{
		return searchResult;
	}

	@Override
	public void setSearchResult(final SearchResult searchResult)
	{
		this.searchResult = searchResult;
	}

	@Override
	public Collection<CatalogVersionModel> getParentSessionCatalogVersions()
	{
		return parentSessionCatalogVersions;
	}

	public void setParentSessionCatalogVersions(final Collection<CatalogVersionModel> parentSessionCatalogVersions)
	{
		this.parentSessionCatalogVersions = parentSessionCatalogVersions;
	}

	@Override
	public Map<String, String> getSearchHints()
	{
		return searchHints;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(final Status status)
	{
		this.status = status;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	public void addFailureException(final Exception exception)
	{
		failureExceptions.add(exception);
	}

	@Override
	public List<Exception> getFailureExceptions()
	{
		return failureExceptions;
	}

	@Override
	public List<IndexedTypeSort> getAvailableNamedSorts() {
		return availableNamedSorts;
	}

	public void setAvailableNamedSorts(List<IndexedTypeSort> availableNamedSorts) {
		this.availableNamedSorts = availableNamedSorts;
	}

	@Override
	public IndexedTypeSort getNamedSort() {
		return namedSort;
	}

	@Override
	public void setNamedSort(IndexedTypeSort namedSort) {
		this.namedSort = namedSort;
	}
}
