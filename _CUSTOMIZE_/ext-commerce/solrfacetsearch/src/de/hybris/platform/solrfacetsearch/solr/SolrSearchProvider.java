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
package de.hybris.platform.solrfacetsearch.solr;

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;


/**
 * Abstraction for different search provider implementations.
 */
public interface SolrSearchProvider
{
	/**
	 * Commit type to be used when performing a commit.
	 */
	enum CommitType
	{
		HARD, SOFT
	}

	/**
	 * Resolves an index. Resolving an index only creates a search provider specific instance of {@link Index}.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the index
	 */
	Index resolveIndex(FacetSearchConfig facetSearchConfig, IndexedType indexedType, String qualifier);

	/**
	 * Returns {@link SolrClient} for specific {@link Index}, that can be used for searches.
	 *
	 * @param index
	 *           - the index
	 *
	 * @return {@link SolrClient} instance
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrClient getClient(Index index) throws SolrServiceException;

	/**
	 * Returns {@link SolrClient} for specific {@link Index}, that can be used for indexing.
	 *
	 * @param index
	 *           - the index
	 *
	 * @return {@link SolrClient} instance
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrClient getClientForIndexing(Index index) throws SolrServiceException;

	/**
	 * Creates an index (if it does not exist).
	 *
	 * @param index
	 *           - the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void createIndex(Index index) throws SolrServiceException;

	/**
	 * Deletes an index (if it exists).
	 *
	 * @param index
	 *           - the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void deleteIndex(Index index) throws SolrServiceException;

	/**
	 * Exports the configuration to a specific index.
	 *
	 * @param index
	 *           - the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void exportConfig(Index index) throws SolrServiceException;

	/**
	 * Deletes all documents from an index.
	 *
	 * @param index
	 *           - the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void deleteAllDocuments(final Index index) throws SolrServiceException;

	/**
	 * Deletes old documents from an index. Old documents are documents that were not created/updated since a given index
	 * operation.
	 *
	 * @param index
	 *           - the index
	 * @param indexOperationId
	 *           - the index operation id
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void deleteOldDocuments(final Index index, final long indexOperationId) throws SolrServiceException;

	/**
	 * Deletes documents matching the provided list of pks
	 *
	 * @param index
	 *           - the index
	 * @param pks
	 *           - the pks list
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void deleteDocumentsByPk(Index index, Collection<PK> pks) throws SolrServiceException;

	/**
	 * Performs a commit on the given index. An hard commit makes sure that indexed documents are persisted and visible,
	 * a soft commit makes sure indexed documents are visible.
	 *
	 * @param index
	 *           - the index
	 * @param commitType
	 *           - the commit type
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void commit(Index index, CommitType commitType) throws SolrServiceException;

	/**
	 * Optimizes a given index. It does not wait for the operation to complete.
	 *
	 * @param index
	 *           - the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void optimize(Index index) throws SolrServiceException;
}
