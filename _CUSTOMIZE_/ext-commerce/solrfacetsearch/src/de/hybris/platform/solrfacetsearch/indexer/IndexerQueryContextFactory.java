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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Map;


/**
 * Implementations of this interface are responsible for creating and destroying instances of
 * {@link IndexerQueryContext}.
 * <p>
 * Code creating/destroying contexts should normally use the following pattern:
 *
 * <pre>
 * try
 * {
 * 	final IndexerQueryContext context = indexerQueryContextFactory.createContext(facetSearchConfig, indexedType, query,
 * 			queryParameters);
 * 
 * 	// call additional setters on the context object
 * 
 * 	indexerQueryContextFactory.initializeContext();
 * 
 * 	// put your logic here
 * 
 * 	indexerQueryContextFactory.destroyContext();
 * }
 * catch (final IndexerException | ... | RuntimeException exception)
 * {
 * 	indexerQueryContextFactory.destroyContext(exception);
 * 	throw exception;
 * }
 * </pre>
 */
public interface IndexerQueryContextFactory<T extends IndexerQueryContext>
{
	/**
	 * Creates a new indexer context and sets it as current.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param query
	 *           - the query
	 * @param queryParameters
	 *           - the query parameters
	 *
	 * @return the new context
	 *
	 * @throws IndexerException
	 *            if an error occurs during the listeners execution
	 */
	T createContext(FacetSearchConfig facetSearchConfig, IndexedType indexedType, String query, Map<String, Object> queryParameters)
			throws IndexerException;

	/**
	 * Initializes the current context and executes the before index listeners (see
	 * {@link IndexerListener#beforeIndex(IndexerContext)}).
	 *
	 * @throws IndexerException
	 *            if an error occurs during the listeners execution
	 */
	void initializeContext() throws IndexerException;

	/**
	 * Returns the current context.
	 *
	 * @return the current context
	 *
	 */
	T getContext();

	/**
	 * Destroys the current context. Before destroying an active context it executes the after query listeners (see
	 * {@link IndexerQueryListener#afterQuery(IndexerQueryContext)}).
	 *
	 * @throws IndexerException
	 *            if an error occurs during the listeners execution
	 */
	void destroyContext() throws IndexerException;

	/**
	 * Destroys the current context because an exception occurred. Before destroying an active context it executes the
	 * after query error listeners (see {@link IndexerQueryListener#afterQueryError(IndexerQueryContext)}).
	 */
	void destroyContext(Exception exception);
}
