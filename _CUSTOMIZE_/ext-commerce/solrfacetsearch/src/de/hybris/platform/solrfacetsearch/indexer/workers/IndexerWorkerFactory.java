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
package de.hybris.platform.solrfacetsearch.indexer.workers;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;


/**
 * Implementations of this interface are responsible for creating instances of {@link IndexerWorker}.
 */
public interface IndexerWorkerFactory
{
	/**
	 * Creates a new indexer worker.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 *
	 * @return the new worker
	 *
	 * @throws IndexerException
	 *            if an error occurs during the worker creation
	 */
	IndexerWorker createIndexerWorker(final FacetSearchConfig facetSearchConfig) throws IndexerException;
}
