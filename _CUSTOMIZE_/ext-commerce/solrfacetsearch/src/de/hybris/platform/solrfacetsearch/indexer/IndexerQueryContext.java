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

import java.util.List;
import java.util.Map;


/**
 * This interface represents a context valid for the duration of an indexer query.
 */
public interface IndexerQueryContext
{
	enum Status
	{
		CREATED, STARTING, EXECUTING, STOPPING, COMPLETED, FAILED
	}

	/**
	 * Returns the facet search configuration.
	 *
	 * @return the facet search configuration
	 */
	FacetSearchConfig getFacetSearchConfig();

	/**
	 * Returns the indexed type.
	 *
	 * @return the indexed type
	 */
	IndexedType getIndexedType();

	/**
	 * Returns the query.
	 *
	 * @return the query
	 */
	String getQuery();

	/**
	 * Returns the query parameters.
	 *
	 * @return the query parameters
	 */
	Map<String, Object> getQueryParameters();

	/**
	 * Returns the current status of the query context
	 *
	 * @return the current status
	 */
	Status getStatus();

	/**
	 * Returns all failure causing exceptions for this {@link IndexerQueryContext}.
	 */
	List<Exception> getFailureExceptions();

	/**
	 * Returns a {@link Map} instance that can be used to store attributes. The attributes are only valid for the
	 * duration of the index process.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();
}
