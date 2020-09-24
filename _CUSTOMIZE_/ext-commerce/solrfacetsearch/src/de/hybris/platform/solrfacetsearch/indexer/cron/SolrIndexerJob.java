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

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;

import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Job that performs indexing
 */
public class SolrIndexerJob extends AbstractIndexerJob
{
	private static final Logger LOG = Logger.getLogger(SolrIndexerJob.class);

	@Override
	public PerformResult performIndexingJob(final CronJobModel cronJob)
	{
		LOG.info("Started indexer cronjob.");

		if (!(cronJob instanceof SolrIndexerCronJobModel))
		{
			LOG.warn("Unexpected cronjob type: " + cronJob);
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
		}

		final SolrIndexerCronJobModel solrIndexerCronJob = (SolrIndexerCronJobModel) cronJob;

		final SolrFacetSearchConfigModel facetSearchConfigModel = solrIndexerCronJob.getFacetSearchConfig();
		final FacetSearchConfig facetSearchConfig = getFacetSearchConfig(facetSearchConfigModel);
		if (facetSearchConfig == null)
		{
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		try
		{
			indexItems(solrIndexerCronJob, facetSearchConfig);
		}
		catch (final IndexerException e)
		{
			LOG.warn("Error during indexer call: " + facetSearchConfigModel.getName(), e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		LOG.info("Finished indexer cronjob.");
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	protected void indexItems(final SolrIndexerCronJobModel solrIndexerCronJob, final FacetSearchConfig facetSearchConfig)
			throws IndexerException
	{
		final Map<String, String> indexerHints = solrIndexerCronJob.getIndexerHints();
		final IndexerOperationValues indexerOperation = solrIndexerCronJob.getIndexerOperation();

		switch (indexerOperation)
		{
			case FULL:
				indexerService.performFullIndex(facetSearchConfig, indexerHints);
				break;
			case UPDATE:
				indexerService.updateIndex(facetSearchConfig, indexerHints);
				break;
			case DELETE:
				indexerService.deleteFromIndex(facetSearchConfig, indexerHints);
				break;
			default:
				throw new IndexerException("Unsupported indexer operation: " + indexerOperation);
		}
	}
}
