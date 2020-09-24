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

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerHotUpdateCronJobModel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;



/**
 * Job that performs indexer hot-update
 */
public class SolrIndexerHotUpdateJob extends AbstractIndexerJob
{
	private static final Logger LOG = Logger.getLogger(SolrIndexerHotUpdateJob.class);

	@Override
	public PerformResult performIndexingJob(final CronJobModel cronJob)
	{

		LOG.info("Started indexer hot-update cronjob.");

		if (!(cronJob instanceof SolrIndexerHotUpdateCronJobModel))
		{
			LOG.warn("Unexpected cronjob type: " + cronJob);
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
		}

		final SolrIndexerHotUpdateCronJobModel indexerCronJob = (SolrIndexerHotUpdateCronJobModel) cronJob;
		final Collection<ItemModel> items = indexerCronJob.getItems();
		if (CollectionUtils.isEmpty(items))
		{
			LOG.info("Nothing to index");
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}

		final SolrFacetSearchConfigModel facetSearchConfigModel = indexerCronJob.getFacetSearchConfig();
		final FacetSearchConfig facetSearchConfig = getFacetSearchConfig(facetSearchConfigModel);
		if (facetSearchConfig == null)
		{
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		try
		{
			indexItems(indexerCronJob, facetSearchConfig, items);
		}
		catch (final IndexerException e)
		{
			LOG.warn("Error during indexer call: " + facetSearchConfigModel.getName() + " \n" + e);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		LOG.info("Finished indexer hot-update cronjob.");

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	protected void indexItems(final SolrIndexerHotUpdateCronJobModel indexerCronJob, final FacetSearchConfig facetSearchConfig,
			final Collection<ItemModel> items) throws IndexerException
	{
		final IndexerOperationValues indexerOperation = indexerCronJob.getIndexerOperation();
		final IndexedType indexedType = facetSearchConfig.getIndexConfig().getIndexedTypes().get(indexerCronJob.getIndexTypeName());
		final List<PK> pks = items.stream().map(item -> item.getPk()).collect(Collectors.toList());

		switch (indexerOperation)
		{
			case UPDATE:
				indexerService.updateTypeIndex(facetSearchConfig, indexedType, pks);
				break;
			case DELETE:
				indexerService.deleteTypeIndex(facetSearchConfig, indexedType, pks);
				break;
			default:
				throw new IndexerException("Unsupported indexer operation: " + indexerOperation);
		}
	}
}
