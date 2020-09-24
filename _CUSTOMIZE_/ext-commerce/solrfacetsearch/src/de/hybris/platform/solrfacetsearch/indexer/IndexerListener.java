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
 * Interface for receiving notifications about {@link IndexerContext} instances.
 */
public interface IndexerListener
{
	/**
	 * Handles a notification that the indexing for a particular {@link IndexerContext} is about to begin.
	 *
	 * @param context
	 *           - the {@link IndexerContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void beforeIndex(IndexerContext context) throws IndexerException;

	/**
	 * Handles a notification that the indexing for a particular {@link IndexerContext} has just been completed.
	 *
	 * @param context
	 *           - the {@link IndexerContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterIndex(IndexerContext context) throws IndexerException;

	/**
	 * Handles a notification that the indexing for a particular {@link IndexerContext} failed.
	 *
	 * @param context
	 *           - the {@link IndexerContext}
	 *
	 * @throws IndexerException
	 *            if an error occurs
	 */
	void afterIndexError(IndexerContext context) throws IndexerException;
}
