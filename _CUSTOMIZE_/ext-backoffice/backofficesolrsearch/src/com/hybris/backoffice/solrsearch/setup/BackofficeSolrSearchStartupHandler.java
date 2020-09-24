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
package com.hybris.backoffice.solrsearch.setup;

import de.hybris.platform.servicelayer.event.events.AfterInitializationEndEvent;
import de.hybris.platform.servicelayer.event.events.AfterInitializationStartEvent;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.events.ExternalEventCallback;
import com.hybris.backoffice.solrsearch.events.AfterInitializationEndBackofficeSearchListener;
import com.hybris.backoffice.solrsearch.events.AfterInitializationStartBackofficeSearchListener;
import com.hybris.backoffice.solrsearch.services.SolrIndexerJobsService;


/**
 * Class responsible for handling {@link AfterInitializationEndEvent} and {@link AfterInitializationStartEvent} in
 * Backofficesolrsearch extension.
 */
public class BackofficeSolrSearchStartupHandler
{

	private AfterInitializationEndBackofficeSearchListener afterInitializationEndBackofficeListener;
	private SolrIndexerJobsService solrIndexerJobsService;

	private ExternalEventCallback<AfterInitializationEndEvent> enableSolrJobsCallback;


	public void initialize()
	{
		if (enableSolrJobsCallback == null)
		{
			enableSolrJobsCallback = createAfterInitializationEndCallback();
		}
		if (enableSolrJobsCallback != null)
		{
			registerAfterInitializationEndCallback(enableSolrJobsCallback);
		}
	}

	protected ExternalEventCallback<AfterInitializationEndEvent> createAfterInitializationEndCallback()
	{
		return event -> solrIndexerJobsService.enableBackofficeSolrSearchIndexerJobs();
	}

	private void registerAfterInitializationEndCallback(final ExternalEventCallback<AfterInitializationEndEvent> callback)
	{
		if (!afterInitializationEndBackofficeListener.isCallbackRegistered(callback))
		{
			afterInitializationEndBackofficeListener.registerCallback(callback);
		}
	}

	public void destroy()
	{
		afterInitializationEndBackofficeListener.unregisterCallback(enableSolrJobsCallback);
	}

	@Required
	public void setAfterInitializationEndBackofficeListener(
			final AfterInitializationEndBackofficeSearchListener afterInitializationEndBackofficeListener)
	{
		this.afterInitializationEndBackofficeListener = afterInitializationEndBackofficeListener;
	}

	/**
	 * @deprecated since 1811, not used anymore
	 */
	@Deprecated
	public void setAfterInitializationStartBackofficeListener(
			final AfterInitializationStartBackofficeSearchListener afterInitializationStartBackofficeListener)
	{
		throw new UnsupportedOperationException();
	}

	@Required
	public void setSolrIndexerJobsService(final SolrIndexerJobsService solrIndexerJobsService)
	{
		this.solrIndexerJobsService = solrIndexerJobsService;
	}


}
