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
 * Extending listener for indexer batch context.
 */
public interface ExtendedIndexerBatchListener extends IndexerBatchListener
{
	/**
	 * The implementation of this method will be invoked after the context preparation.
	 *
	 * @param context
	 * 			 - The {@link IndexerBatchContext}
	 *
	 * @throws {@link IndexerException}
	 * 				if an error occurs
	 */
	void afterPrepareContext(IndexerBatchContext context) throws IndexerException;
}
