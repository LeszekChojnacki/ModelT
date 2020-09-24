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
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Implementations of this interface are responsible for running the queries required for the indexing process.
 */
public interface IndexerQueriesExecutor
{
	/**
	 * Gets the pks of all the items that will be indexed. The query will be run in the context of the current session
	 * user.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param query
	 *           - a string representing the query
	 * @param queryParameters
	 *           - the parameters for the query
	 *
	 * @return the list of pks that represent the items to be indexed
	 *
	 * @throws IndexerException
	 *            in case of error
	 */
	List<PK> getPks(FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final String query,
			final Map<String, Object> queryParameters) throws IndexerException;

	/**
	 * Gets the model instances based on a list of pks. The pks list passed as parameter is normally just a subset of all
	 * the items that will be indexed. The query will be run in the context of the current session user
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param pks
	 *           - the list of pks
	 *
	 * @return the list of items to be indexed
	 *
	 * @throws IndexerException
	 *            in case of error
	 */
	List<ItemModel> getItems(FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final Collection<PK> pks)
			throws IndexerException;
}
