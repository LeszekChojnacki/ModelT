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
package de.hybris.platform.solrfacetsearch.indexer.strategies;

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorker;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerFactory;
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Strategy for performing the indexing process. This strategy is normally responsible for creating and triggering the
 * execution of the indexer workers (see {@link IndexerWorker} and {@link IndexerWorkerFactory}).
 */
public interface IndexerStrategy
{
	/**
	 * Sets the index operation to be used.
	 *
	 * @param indexOperation
	 *           - the index operation
	 */
	void setIndexOperation(final IndexOperation indexOperation);

	/**
	 * Sets the facet search configuration to be used.
	 *
	 * @param facetSearchConfig
	 *           - the index operation
	 */
	void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig);

	/**
	 * Sets the indexed type to be used.
	 *
	 * @param indexedType
	 *           - the index operation
	 */
	void setIndexedType(final IndexedType indexedType);

	/**
	 * Sets the index operation to be used.
	 *
	 * @param indexedProperties
	 *           - the indexed properties
	 */
	void setIndexedProperties(final Collection<IndexedProperty> indexedProperties);

	/**
	 * Sets the pks of the items to be indexed.
	 *
	 * @param pks
	 *           - the pks of the items to be indexed
	 */
	void setPks(final List<PK> pks);

	/**
	 * Sets the index to be used.
	 *
	 * @param index
	 *           - the index
	 */
	void setIndex(final Index index);

	/**
	 * Sets the indexer hints to be used.
	 *
	 * @param indexerHints
	 *           - the indexer hints to be used
	 */
	void setIndexerHints(final Map<String, String> indexerHints);

	/**
	 * Executes the strategy.
	 *
	 * @throws IndexerException
	 *            if an error occurs during the strategy execution
	 */
	void execute() throws IndexerException;
}
