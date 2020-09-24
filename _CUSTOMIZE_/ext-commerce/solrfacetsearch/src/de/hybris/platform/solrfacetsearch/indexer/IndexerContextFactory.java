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
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Collection;


/**
 * Implementations of this interface are responsible for creating and destroying instances of {@link IndexerContext}.
 * <p>
 * Code creating/destroying contexts should normally use the following pattern:
 *
 * <pre>
 * try
 * {
 * 	final IndexerContext context = indexerContextFactory.createContext(externalIndexOperation, indexerOperation,
 * 			facetSearchConfig, indexedType, indexedProperties, pks);
 *
 * 	// call additional setters on the context object
 *
 * 	indexerContextFactory.prepareContext();
 *
 * 	// call the setter for the PKs.
 *
 * 	indexerContextFactory.initializeContext();
 *
 * 	// put your logic here
 *
 * 	indexerContextFactory.destroyContext();
 * }
 * catch (final IndexerException | ... | RuntimeException e)
 * {
 * 	indexerContextFactory.destroyContext(e);
 * 	throw e;
 * }
 * </pre>
 */
public interface IndexerContextFactory<T extends IndexerContext>
{
	/**
	 * Creates a new indexer context and sets it as the current one.
	 *
	 * @param indexOperationId
	 *           - the index operation id
	 * @param indexOperation
	 *           - the index operation
	 * @param externalIndexOperation
	 *           - indicates if the index operation is external
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param indexedProperties
	 *           - the properties to index
	 *
	 * @return the new context
	 */
	T createContext(long indexOperationId, IndexOperation indexOperation, boolean externalIndexOperation,
			final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties);

	/**
	 * Prepares the context and executes the after prepare context listeners (see
	 * {@link ExtendedIndexerListener#afterPrepareContext(IndexerContext)}).
	 *
	 * @throws IndexerException
	 *            if an error occurs during the listeners execution
	 */
	void prepareContext() throws IndexerException;

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
	 * Destroys the current context. Before destroying an active context it executes the after index listeners (see
	 * {@link IndexerListener#afterIndex(IndexerContext)}).
	 *
	 * @throws IndexerException
	 *            if an error occurs during the listeners execution
	 */
	void destroyContext() throws IndexerException;

	/**
	 * Destroys the current context because an exception occurred. Before destroying an active context it executes the
	 * after index error listeners (see {@link IndexerListener#afterIndexError(IndexerContext)}).
	 */
	void destroyContext(Exception exception);
}
