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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;


/**
 * Implementations of this interface should be responsible for creating {@link IndexerBatchStrategy}
 */
public interface IndexerBatchStrategyFactory
{
	/**
	 * Create new IndexerBatchStrategy.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @return the new IndexerBatchStrategy
	 *
	 * @throws IndexerException
	 *            if an error occurs during the strategy creation
	 */
	IndexerBatchStrategy createIndexerBatchStrategy(FacetSearchConfig facetSearchConfig) throws IndexerException;
}
