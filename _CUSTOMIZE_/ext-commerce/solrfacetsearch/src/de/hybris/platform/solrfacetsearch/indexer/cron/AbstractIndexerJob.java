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
package de.hybris.platform.solrfacetsearch.indexer.cron;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.spi.Indexer;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import org.apache.log4j.Logger;


/**
 * Base implementation for indexer jobs.
 */
public abstract class AbstractIndexerJob extends AbstractJobPerformable
{
	private static final Logger LOG = Logger.getLogger(AbstractIndexerJob.class);

	protected IndexerService indexerService;
	protected FacetSearchConfigService facetSearchConfigService;
	protected Indexer indexer;

	@Override
	public PerformResult perform(final CronJobModel cronJob)
	{
		return performIndexingJob(cronJob);
	}

	public abstract PerformResult performIndexingJob(final CronJobModel cronJob);

	/**
	 * @param indexerService
	 *           the indexerService to set
	 */
	public void setIndexerService(final IndexerService indexerService)
	{
		this.indexerService = indexerService;
	}

	/**
	 * @param facetSearchConfigService
	 *           the facetSearchConfigService to set
	 */
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	/**
	 * @param facetSearchConfigModel
	 */
	protected FacetSearchConfig getFacetSearchConfig(final SolrFacetSearchConfigModel facetSearchConfigModel)
	{
		try
		{
			return facetSearchConfigService.getConfiguration(facetSearchConfigModel.getName());
		}
		catch (final FacetConfigServiceException e)
		{
			LOG.warn("Configuration service could not load configuration with name: " + facetSearchConfigModel.getName(), e);
			return null;
		}
	}

	/**
	 * @param indexer
	 *           the indexer to set
	 */
	public void setIndexer(final Indexer indexer)
	{
		this.indexer = indexer;
	}
}
