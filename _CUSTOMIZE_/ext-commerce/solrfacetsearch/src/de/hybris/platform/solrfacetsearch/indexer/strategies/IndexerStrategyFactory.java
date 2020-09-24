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
 * Implementations of this interface are responsible for creating instances of {@link IndexerStrategy}.
 */
public interface IndexerStrategyFactory
{
	/**
	 * Creates a new indexer strategy.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 *
	 * @return the new indexer strategy
	 *
	 * @throws IndexerException
	 *            if an error occurs during the strategy creation
	 *
	 */
	IndexerStrategy createIndexerStrategy(FacetSearchConfig facetSearchConfig) throws IndexerException;
}
