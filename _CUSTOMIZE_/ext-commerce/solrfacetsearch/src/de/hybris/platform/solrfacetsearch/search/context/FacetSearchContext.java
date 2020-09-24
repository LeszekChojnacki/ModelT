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
package de.hybris.platform.solrfacetsearch.search.context;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This interface represents a context valid for the duration of a search.
 */
public interface FacetSearchContext
{
	enum Status
	{
		CREATED, STARTING, EXECUTING, STOPPING, COMPLETED, FAILED
	}

	/**
	 * Returns the facet search configuration.
	 *
	 * @return the facet search configuration
	 */
	FacetSearchConfig getFacetSearchConfig();

	/**
	 * Returns the indexed type.
	 *
	 * @return the indexed type
	 */
	IndexedType getIndexedType();

	/**
	 * Returns the search query.
	 *
	 * @return the search query
	 */
	SearchQuery getSearchQuery();

	/**
	 * Returns the search result.
	 *
	 * @return the search result
	 */
	SearchResult getSearchResult();

	/**
	 * Sets the search result.
	 */
	void setSearchResult(SearchResult searchResult);

	/**
	 * Returns the parent session catalog versions (the catalog versions that are in the session when this context is
	 * created).
	 *
	 * @return the parent session catalog versions
	 */
	Collection<CatalogVersionModel> getParentSessionCatalogVersions();

	/**
	 * Returns a mutable {@link Map} that can be used to store search hints.
	 *
	 * @return the map containing the search hints
	 */
	Map<String, String> getSearchHints();

	/**
	 * Returns the current status for this {@link FacetSearchContext}
	 *
	 * @return the current status
	 */
	Status getStatus();

	/**
	 * Returns all failure causing exceptions for this {@link FacetSearchContext}.
	 */
	List<Exception> getFailureExceptions();

	/**
	 * Returns a {@link Map} instance that can be used to store attributes. The attributes are only valid for the duration
	 * of the search process.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();


	/**
	 * Return list of available named sorts.
	 *
	 * @return List of {@link IndexedTypeSort}
	 */
	List<IndexedTypeSort> getAvailableNamedSorts();

	/**
	 * Return current named sort.
	 *
	 * @return Current named sort {@link IndexedTypeSort}
	 */
	IndexedTypeSort getNamedSort();

	/**
	 * Sets current named sort {@link IndexedTypeSort}.
	 */
	void setNamedSort(IndexedTypeSort namedSort);

}
