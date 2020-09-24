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
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This interface represents a context valid for the duration of an indexer index operation, however it is not valid
 * inside an indexer batch (an indexer batch may run on a different thread or even on a different machine).
 */
public interface IndexerContext
{
	enum Status
	{
		CREATED, STARTING, EXECUTING, STOPPING, COMPLETED, FAILED
	}

	/**
	 * Returns the index operation id.
	 *
	 * @return the index operation id
	 */
	long getIndexOperationId();

	/**
	 * Returns the index operation.
	 *
	 * @return the index operation
	 */
	IndexOperation getIndexOperation();

	/**
	 * Returns {@code true} if the index operation is external. An external operation does not use a default indexer
	 * query.
	 *
	 * @return {@code true} if the index operation is external, {@code false} otherwise
	 */
	boolean isExternalIndexOperation();

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
	 * Returns the indexed properties to be indexed.
	 *
	 * @return the indexed properties to be indexed
	 */
	Collection<IndexedProperty> getIndexedProperties();

	/**
	 * Returns the pks of the items to be indexed.
	 *
	 * @return pks of the items to be indexed
	 */
	List<PK> getPks();

	/**
	 * Sets the pks of the items to be indexed.
	 *
	 * @param pks
	 *           - the pks of the items to be indexed
	 *
	 * @throws IllegalStateException
	 *            if this method is called and the status is not {@link Status#CREATED} or {@link Status#STARTING}
	 */
	void setPks(final List<PK> pks);

	/**
	 * Returns the index to use during the indexer operation.
	 *
	 * @return the index to use
	 */
	Index getIndex();

	/**
	 * Sets the index to use during the indexer operation.
	 *
	 * @param index
	 *           - the index to use
	 */
	void setIndex(Index index);

	/**
	 * Returns a mutable {@link Map} that can be used to store indexer hints.
	 *
	 * @return the map containing the indexer hints
	 */
	Map<String, String> getIndexerHints();

	/**
	 * Returns a mutable {@link Map} that can be used to store attributes associated with this {@link IndexerContext}.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();

	/**
	 * Returns the current status for this {@link IndexerContext}
	 *
	 * @return the current status
	 */
	Status getStatus();

	/**
	 * Returns all failure causing exceptions for this {@link IndexerContext}.
	 */
	List<Exception> getFailureExceptions();
}
