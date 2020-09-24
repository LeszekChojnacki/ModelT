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
package de.hybris.platform.solrfacetsearch.search.context;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;


/**
 * Implementations of this interface are responsible for creating and destroying instances of {@link FacetSearchContext}
 * .
 * <p>
 * Code creating/destroying contexts should normally use the following pattern:
 *
 * <pre>
 * try
 * {
 * 	final FacetSearchContext facetSearchContext = facetSearchContextFactory.createContext(facetSearchConfig, indexedType, searchQuery);
 *
 * 	// call additional setters on the context object
 *
 * 	facetSearchContextFactory.initializeContext();
 *
 * 	// put your logic here
 *
 * 	facetSearchContextFactory.destroyContext();
 * }
 * catch (final FacetSearchException | ... | RuntimeException e)
 * {
 * 	facetSearchContextFactory.destroyContext(e);
 * 	throw e;
 * }
 * </pre>
 */
public interface FacetSearchContextFactory<T extends FacetSearchContext>
{
	/**
	 * Creates a new facet search context and sets it as current one.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param searchQuery
	 *           - the search
	 *
	 * @return the new context
	 */
	T createContext(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final SearchQuery searchQuery);

	/**
	 * Initializes the current context and executes before search query listeners (see
	 * {@link FacetSearchListener#beforeSearch(FacetSearchContext)}).
	 *
	 * @throws FacetSearchException
	 *            if an error occurs during the listeners execution
	 */
	void initializeContext() throws FacetSearchException;

	/**
	 * Returns the current context.
	 *
	 * @return the current context
	 */
	T getContext();

	/**
	 * Destroys the current context. Before destroying an active context it executes the after search query listeners
	 * (see {@link FacetSearchListener#afterSearch(FacetSearchContext)}).
	 *
	 * @throws FacetSearchException
	 *            if an error occurs during the listeners execution
	 */
	void destroyContext() throws FacetSearchException;

	/**
	 * Destroys the current context because an exception occurred. Before destroying an active context it executes the
	 * after batch error listeners (see {@link FacetSearchListener#afterSearchError(FacetSearchContext)}).
	 */
	void destroyContext(Exception exception);
}
