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
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This interface represents a context valid for the duration of an indexer batch. Each batch runs on a separate thread
 * and this context is only valid for the corresponding thread.
 */
public interface IndexerBatchContext
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
	 * Return the pks to be indexed in this batch.
	 *
	 * @return pks to be indexed in this batch.
	 */
	List<PK> getPks();

	/**
	 * Sets the pks to be indexed in this batch.
	 *
	 * @param pks
	 *           - the pks to be indexed in this batch.
	 */
	void setPks(List<PK> pks);

	/**
	 * Returns the items to be indexed in this batch.
	 *
	 * @return items to be indexed in this batch
	 */
	List<ItemModel> getItems();

	/**
	 * Sets the items to be indexed in this batch.
	 *
	 * @param items
	 *           - the items to be indexed in this batch
	 *
	 * @throws IllegalStateException
	 *            if this method is called and the status is not {@link Status#CREATED} or {@link Status#STARTING}
	 */
	void setItems(final List<ItemModel> items);

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
	 * Returns a mutable {@link Map} that can be used to store attributes associated with this
	 * {@link IndexerBatchContext}.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();

	/**
	 * Returns a mutable {@link List} that can be used to store attributes associated with this
	 * input documents.
	 *
	 * @return the map containing the indexed property values
	 */
	List<InputDocument> getInputDocuments();

	/**
	 * Returns the current status for this {@link IndexerBatchContext}
	 *
	 * @return the current status
	 */
	Status getStatus();

	/**
	 * Returns all failure causing exceptions for this {@link IndexerBatchContext}.
	 */
	List<Exception> getFailureExceptions();
}
