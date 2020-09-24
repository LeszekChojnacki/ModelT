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
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.util.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.events.AfterInitializationEndBackofficeSearchListener;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


/**
 * This class executes its logic during backoffice web context initialization and after system initialization started
 * from hAC. It initializes (i.e. performs full indexing) all uninitialized backoffice solr indexes.
 */
public class BackofficeSolrSearchIndexInitializer
{

	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;
	private SolrIndexService solrIndexService;
	private IndexerService indexerService;
	private AfterInitializationEndBackofficeSearchListener afterInitializationEndBackofficeListener;

	private static final String PROPERTY_INDEX_AUTOINIT = "backoffice.solr.search.index.autoinit";

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeSolrSearchIndexInitializer.class);


	public void initialize()
	{
		initializeIndexesIfNecessary();
		registerSystemInitializationEndCallback();
	}

	protected void initializeIndexesIfNecessary()
	{
		if (shouldInitializeIndexes())
		{
			getBackofficeFacetSearchConfigService().getAllMappedFacetSearchConfigs().stream()
					.filter(searchConfig -> !isIndexInitialized(searchConfig)).forEach(this::initializeIndex);
		}
		else
		{
			LOG.info("Backoffice SOLR indices initialization disabled by property");
		}
	}

	protected boolean shouldInitializeIndexes()
	{
		return Config.getBoolean(PROPERTY_INDEX_AUTOINIT, true);
	}

	protected boolean isIndexInitialized(final FacetSearchConfig searchConfig)
	{
		try
		{
			final IndexedType indexedType = searchConfig.getIndexConfig().getIndexedTypes().values().iterator().next();
			getSolrIndexService().getActiveIndex(searchConfig.getName(), indexedType.getIdentifier());

			return true;
		}
		catch (final SolrServiceException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.info("Index for '{}' configuration not initialized", searchConfig.getName(), e);
			}
			else
			{
				LOG.info("Index for '{}' configuration not initialized", searchConfig.getName());
			}
		}
		return false;
	}

	protected void initializeIndex(final FacetSearchConfig searchConfig)
	{
		LOG.info("Performing FULL INDEX operation for '{}' configuration", searchConfig.getName());
		try
		{
			getIndexerService().performFullIndex(searchConfig);
		}
		catch (final IndexerException e)
		{
			LOG.error("Indexer error", e);
		}
	}

	private void registerSystemInitializationEndCallback()
	{
		if (!afterInitializationEndBackofficeListener.isCallbackRegistered(this::handleSystemInitializationEndEvent))
		{
			afterInitializationEndBackofficeListener.registerCallback(this::handleSystemInitializationEndEvent);
		}
	}

	protected void handleSystemInitializationEndEvent(final AfterInitializationEndEvent event)
	{
		initializeIndexesIfNecessary();
	}

	protected BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	protected SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	protected IndexerService getIndexerService()
	{
		return indexerService;
	}

	@Required
	public void setIndexerService(final IndexerService indexerService)
	{
		this.indexerService = indexerService;
	}

	@Required
	public void setAfterInitializationEndBackofficeListener(
			final AfterInitializationEndBackofficeSearchListener afterInitializationEndBackofficeListener)
	{
		this.afterInitializationEndBackofficeListener = afterInitializationEndBackofficeListener;
	}

}
