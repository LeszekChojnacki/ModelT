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

import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;


/**
 * Interface for receiving notifications about {@link IndexerBatchContext} instances.
 */
public interface IndexerBatchListener
{
	/**
	 * Handles a notification that the processing for a particular {@link IndexerBatchContext} is about to begin.
	 *
	 * @param batchContext
	 *           - the {@link IndexerBatchContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void beforeBatch(IndexerBatchContext batchContext) throws IndexerException;

	/**
	 * Handles a notification that the processing for a particular {@link IndexerBatchContext} has just been completed.
	 *
	 * @param batchContext
	 *           - the {@link IndexerBatchContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterBatch(IndexerBatchContext batchContext) throws IndexerException;

	/**
	 * Handles a notification that the processing for a particular {@link IndexerBatchContext} failed.
	 *
	 * @param batchContext
	 *           - the {@link IndexerBatchContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterBatchError(IndexerBatchContext batchContext) throws IndexerException;
}
