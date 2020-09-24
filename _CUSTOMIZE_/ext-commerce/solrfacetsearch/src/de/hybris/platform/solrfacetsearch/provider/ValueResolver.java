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
package de.hybris.platform.solrfacetsearch.provider;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;

import java.util.Collection;


/**
 * <p>
 * Implementations of this interface are responsible for resolving the values to be indexed. This interface works at the
 * indexed property level.
 * </p>
 *
 * @see TypeValueResolver
 */
public interface ValueResolver<T extends ItemModel>
{
	/**
	 * Resolves the values to be indexed. The indexed properties that use the same value resolver are grouped and this
	 * method is called once for each one of these groups.
	 *
	 * @param document
	 *           - document that will be indexed, all the resolved values should be added as fields to this document
	 * @param batchContext
	 *           - the current indexer batch context
	 * @param indexedProperties
	 *           - the indexed properties that use the same value resolver
	 * @param model
	 *           - the values should be resolved for this model instance
	 *
	 * @throws FieldValueProviderException
	 *            if an error occurs
	 */
	void resolve(final InputDocument document, final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, T model) throws FieldValueProviderException;
}
