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
package com.hybris.backoffice.solrsearch.indexer.impl;

import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.impl.DefaultIndexerStrategy;
import de.hybris.platform.solrfacetsearch.solr.Index;

import org.apache.log4j.Logger;


public class BackofficeIndexerStrategy extends DefaultIndexerStrategy
{
	private final static Logger LOG = Logger.getLogger(BackofficeIndexerStrategy.class);

	/**
	 * Creates local view with admin privileges and calls {@link #doExecuteAsAdmin}
	 */
	@Override
	protected void doExecute(final Index resolvedIndex, final long indexOperationId, final boolean isExternalIndexOperation)
			throws IndexerException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Executing indexer worker as an admin user.");
		}

		getSessionService().executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				try
				{
					doExecuteAsAdmin(resolvedIndex, indexOperationId, isExternalIndexOperation);
				}
				catch (final IndexerException e)
				{
					LOG.error("Executing indexer worker as an admin user failed: ", e);
				}
			}
		}, getUserService().getAdminUser());
	}

	/**
	 * This method contains real logic being executed by {@link BackofficeIndexerStrategy#doExecute} in admin's local
	 * view. Default implementation delegates to {@link DefaultIndexerStrategy#doExecute}
	 */
	protected void doExecuteAsAdmin(final Index resolvedIndex, final long indexOperationId, final boolean isExternalIndexOperation)
			throws IndexerException
	{
		super.doExecute(resolvedIndex, indexOperationId, isExternalIndexOperation);
	}
}
