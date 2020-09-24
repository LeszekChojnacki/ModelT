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
package de.hybris.platform.solrfacetsearch.reporting.cron;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.model.cron.SolrQueryStatisticsCollectorCronJobModel;
import de.hybris.platform.solrfacetsearch.reporting.AggregatedQueryStatsService;
import de.hybris.platform.solrfacetsearch.reporting.SolrQueryStatisticsAggregator;
import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;

import java.util.List;

import org.apache.log4j.Logger;


/**
 * Job that aggregates and saves statistics for SOLR queries
 */
public class SolrQueryStatisticsCollectorJob extends AbstractJobPerformable<SolrQueryStatisticsCollectorCronJobModel>
{
	private static final Logger LOG = Logger.getLogger(SolrQueryStatisticsCollectorJob.class);
	private SolrQueryStatisticsAggregator solrQueryStatisticsAggregator;
	private AggregatedQueryStatsService aggregatedQueryStatsService;
	private boolean enableCollectingStatistics;

	@Override
	public PerformResult perform(final SolrQueryStatisticsCollectorCronJobModel cronJob)
	{
		if (enableCollectingStatistics)
		{
			try
			{
				final List<AggregatedSearchQueryInfo> aggregatedStatistics = solrQueryStatisticsAggregator.aggregate();
				aggregatedQueryStatsService.save(aggregatedStatistics);
			}
			catch (final Exception ex)
			{
				LOG.error("Error executing SolrQueryStatisticsCollectorJob", ex);
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
		}
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	public void setSolrQueryStatisticsAggregator(final SolrQueryStatisticsAggregator solrQueryStatisticsAggregator)
	{
		this.solrQueryStatisticsAggregator = solrQueryStatisticsAggregator;
	}

	public void setAggregatedQueryStatsService(final AggregatedQueryStatsService aggregatedQueryStatsService)
	{
		this.aggregatedQueryStatsService = aggregatedQueryStatsService;
	}

	public void setEnableCollectingStatistics(final boolean enableCollectingStatistics)
	{
		this.enableCollectingStatistics = enableCollectingStatistics;
	}
}
