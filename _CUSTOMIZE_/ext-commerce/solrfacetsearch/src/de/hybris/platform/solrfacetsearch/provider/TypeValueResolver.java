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

import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;


/**
 * <p>
 * Implementations of this interface are responsible for resolving the values to be indexed. This interface works at the
 * type level.
 * </p>
 *
 * @see ValueResolver
 */
public interface TypeValueResolver<T>
{
	/**
	 * Resolves the values to be indexed.
	 *
	 * @param document
	 *           - document that will be indexed, all the resolved values should be added as fields to this document
	 * @param batchContext
	 *           - the current indexer batch context
	 * @param model
	 *           - the values should be resolved for this model instance
	 *
	 * @throws FieldValueProviderException
	 */
	void resolve(final InputDocument document, IndexerBatchContext batchContext, T model) throws FieldValueProviderException;
}
