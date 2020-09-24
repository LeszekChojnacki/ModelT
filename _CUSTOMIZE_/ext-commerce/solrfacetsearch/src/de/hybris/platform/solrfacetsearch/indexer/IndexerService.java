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
package de.hybris.platform.solrfacetsearch.indexer;

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Implementations of this interface should be responsible for indexing operations.
 */
public interface IndexerService
{
	/**
	 * Performs a full index operation which recreates the index. All the types associated with the facet search
	 * configuration are considered. Items to be indexed are selected based on the full index query.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 *
	 * @throws IndexerException
	 *            exception is thrown when full index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void performFullIndex(FacetSearchConfig facetSearchConfig) throws IndexerException;

	/**
	 * Same as {@link #performFullIndex(FacetSearchConfig)} but allows to pass the indexer hints as parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when full index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void performFullIndex(FacetSearchConfig facetSearchConfig, Map<String, String> indexerHints) throws IndexerException;

	/**
	 * Updates some items on the index. All the types associated with the facet search configuration are considered.
	 * Items to be updated are selected based on the update index query.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 *
	 * @throws IndexerException
	 *            exception is thrown when update index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void updateIndex(FacetSearchConfig facetSearchConfig) throws IndexerException;

	/**
	 * Same as {@link #updateIndex(FacetSearchConfig)} but allows to pass the indexer hints as parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when update index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void updateIndex(FacetSearchConfig facetSearchConfig, Map<String, String> indexerHints) throws IndexerException;

	/**
	 * Updates some items on the index for a specific type. Items to be updated are selected based on the update index
	 * query.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 *
	 * @throws IndexerException
	 *            exception is thrown when update index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void updateTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType) throws IndexerException;

	/**
	 * Same as {@link #updateTypeIndex(FacetSearchConfig, IndexedType)} but allows to pass the indexer hints as
	 * parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when update index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void updateTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType, Map<String, String> indexerHints)
			throws IndexerException;

	/**
	 * Updates some items on the index for a specific type.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param pks
	 *           - pks of items to be updated
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, List<PK> pks)
			throws IndexerException;

	/**
	 * Same as {@link #updateTypeIndex(FacetSearchConfig, IndexedType, List)} but allows to pass the indexer hints as
	 * parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param pks
	 *           - pks of items to be updated
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, List<PK> pks,
			Map<String, String> indexerHints) throws IndexerException;

	/**
	 * Updates some properties of some items on the index for a specific type.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param indexedProperties
	 *           - properties to update
	 * @param pks
	 *           - pks of items to be updated
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void updatePartialTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties, List<PK> pks) throws IndexerException;

	/**
	 * Same as {@link #updatePartialTypeIndex(FacetSearchConfig, IndexedType, Collection, List)} but allows to pass the
	 * indexer hints as parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param indexedProperties
	 *           - properties to update
	 * @param pks
	 *           - pks of items to be updated
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void updatePartialTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties, List<PK> pks, Map<String, String> indexerHints)
			throws IndexerException;

	/**
	 * Removes some items from the index. All the types associated with the facet search configuration are considered.
	 * Items to be removed are selected based on the delete index query.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 *
	 * @throws IndexerException
	 *            exception is thrown when delete index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void deleteFromIndex(FacetSearchConfig facetSearchConfig) throws IndexerException;

	/**
	 * Same as {@link #deleteFromIndex(FacetSearchConfig)} but allows to pass the indexer hints as parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when delete index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void deleteFromIndex(FacetSearchConfig facetSearchConfig, Map<String, String> indexerHints) throws IndexerException;

	/**
	 * Removes some items from the index for a specific type. Items to be removed are selected based on the delete index
	 * query.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 *
	 * @throws IndexerException
	 *            exception is thrown when delete index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void deleteTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType) throws IndexerException;

	/**
	 * Same as {@link #deleteTypeIndex(FacetSearchConfig, IndexedType)} but allows to pass the indexer hints as
	 * parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when delete index query is missing in configuration or an unexpected error occurs
	 *            during indexing.
	 */
	void deleteTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType, Map<String, String> indexerHints)
			throws IndexerException;

	/**
	 * Removes some items from the index for a specific type.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param pks
	 *           - pks of items to be removed
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void deleteTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType, List<PK> pks) throws IndexerException;

	/**
	 * Same as {@link #deleteTypeIndex(FacetSearchConfig, IndexedType, List)} but allows to pass the indexer hints as
	 * parameter.
	 *
	 * @param facetSearchConfig
	 *           - configuration for indexer instance
	 * @param indexedType
	 *           - selected type
	 * @param pks
	 *           - pks of items to be removed
	 * @param indexerHints
	 *           - the indexer hints
	 *
	 * @throws IndexerException
	 *            exception is thrown when an unexpected error occurs during indexing.
	 */
	void deleteTypeIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType, List<PK> pks,
			Map<String, String> indexerHints) throws IndexerException;

}
