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
 * Interface for receiving notifications about indexer queries execution.
 */
public interface IndexerQueryListener
{
	/**
	 * Handles a notification that an indexer query is about to begin execution.
	 *
	 * @param queryContext
	 *           - the {@link IndexerQueryContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void beforeQuery(IndexerQueryContext queryContext) throws IndexerException;

	/**
	 * Handles a notification that an indexer query has just completed.
	 *
	 * @param queryContext
	 *           - the {@link IndexerQueryContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterQuery(IndexerQueryContext queryContext) throws IndexerException;

	/**
	 * Handles a notification that an indexer query failed (this may also be due to listeners failing).
	 *
	 * @param queryContext
	 *           - the {@link IndexerQueryContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterQueryError(IndexerQueryContext queryContext) throws IndexerException;
}
