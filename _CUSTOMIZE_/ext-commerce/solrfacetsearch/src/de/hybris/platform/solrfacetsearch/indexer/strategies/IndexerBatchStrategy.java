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
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Strategy for performing batch indexing.
 */
public interface IndexerBatchStrategy
{
	/**
	 * Sets the external index operation to be used.
	 *
	 * @param externalIndexOperation
	 *           - the external index operation
	 */
	void setExternalIndexOperation(boolean externalIndexOperation);

	/**
	 * Sets the facet search config to be used.
	 *
	 * @param facetSearchConfig
	 *           - the facet search config to be used
	 */
	void setFacetSearchConfig(FacetSearchConfig facetSearchConfig);

	/**
	 * Sets the index to be used
	 *
	 * @param index
	 *           - the index to be used
	 */
	void setIndex(Index index);

	/**
	 * Sets the indexed properties to be used
	 *
	 * @param indexedProperties
	 *           - the indexed properties to be used
	 */
	void setIndexedProperties(Collection<IndexedProperty> indexedProperties);

	/**
	 * Sets the indexed type to be used
	 *
	 * @param indexedType
	 *           - the indexed type to be used
	 */
	void setIndexedType(IndexedType indexedType);

	/**
	 * Sets the indexer hints to be used
	 *
	 * @param indexerHints
	 *           - the indexer hints to be used
	 */
	void setIndexerHints(Map<String, String> indexerHints);

	/**
	 * Sets the index operation to be used
	 *
	 * @param indexOperation
	 *           - the index operation to be used
	 */
	void setIndexOperation(IndexOperation indexOperation);

	/**
	 * Sets the indexed operation id to be used
	 *
	 * @param indexOperationId
	 *           - the indexed operation id to be used
	 */
	void setIndexOperationId(long indexOperationId);

	/**
	 * Sets the pks to be used
	 *
	 * @param pks
	 *           - the pks to be used
	 */
	void setPks(List<PK> pks);

	/**
	 * Executes the strategy.
	 *
	 * @throws InterruptedException
	 *            throw if a thread is interrupted
	 * @throws IndexerException
	 *            if an error occurs during the strategy execution
	 */
	void execute() throws InterruptedException, IndexerException;
}
