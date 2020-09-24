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
package de.hybris.platform.solrfacetsearch.indexer.workers;


/**
 * Worker that should be used for performing indexing operations.
 */
public interface IndexerWorker extends Runnable
{
	/**
	 * Initializes the worker.
	 *
	 * @param workerParameters
	 *           the worker parameters
	 */
	void initialize(IndexerWorkerParameters workerParameters);

	/**
	 * Indicates if the worker is initialized and ready to run.
	 *
	 * @return {@code true} if the worker is initialized, {@code false} otherwise
	 */
	boolean isInitialized();
}
