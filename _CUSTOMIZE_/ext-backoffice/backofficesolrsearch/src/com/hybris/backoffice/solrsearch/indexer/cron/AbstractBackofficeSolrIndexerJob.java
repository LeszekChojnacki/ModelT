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
package com.hybris.backoffice.solrsearch.indexer.cron;

import de.hybris.platform.core.PK;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.cron.SolrIndexerJob;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.CollectionUtils;

import com.hybris.backoffice.solrsearch.daos.SolrModifiedItemDAO;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;

/**
 * @deprecated since 1808 Custom backoffice indexer jobs are deprecated. Standard indexing jobs are being used instead - {@link SolrIndexerJob}
 */
@Deprecated
public abstract class AbstractBackofficeSolrIndexerJob extends SolrIndexerJob
{
	private final Logger LOG = LoggerFactory.getLogger(AbstractBackofficeSolrIndexerJob.class);

	protected SolrModifiedItemDAO solrModifiedItemDAO;
	protected BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;

	@Override
	public PerformResult performIndexingJob(final CronJobModel cronJob)
	{

		final Collection<SolrModifiedItemModel> modifiedItemModels = findModifiedItems();

		if (!CollectionUtils.isEmpty(modifiedItemModels))
		{
			synchronizeIndexAndRemoveModifiedItems(modifiedItemModels);

			LOG.debug("Solr synchronization completed for {} items", Integer.valueOf(modifiedItemModels.size()));
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	protected void synchronizeIndexAndRemoveModifiedItems(final Collection<SolrModifiedItemModel> modifiedItemModels)
	{
		final Map<String, List<SolrModifiedItemModel>> itemsByTypecode = modifiedItemModels.stream()
				.collect(Collectors.groupingBy(SolrModifiedItemModel::getModifiedTypeCode));

		for (final Map.Entry<String, List<SolrModifiedItemModel>> entry : itemsByTypecode.entrySet())
		{
			try
			{
				final SolrFacetSearchConfigModel solrFacetSearchConfigModel = backofficeFacetSearchConfigService
						.getSolrFacetSearchConfigModel(entry.getKey());

				if (solrFacetSearchConfigModel != null)
				{
					synchronizeIndexForConfig(solrFacetSearchConfigModel, entry.getValue());
				}
				else
				{
					LOG.warn("Solr facet search config cannot be found for type {}", entry.getKey());
				}

				modelService.removeAll(entry.getValue());
			}
			catch (final FacetConfigServiceException e)
			{
				LOG.warn("Solr facet search config cannot be found for type: " + entry.getKey(), e);
			}
			catch (final IndexerException|SolrServiceException e)
			{
				LOG.warn("Solr synchronization failed for type: " + entry.getKey(), e);
			}
		}
	}

	protected void synchronizeIndexForConfig(final SolrFacetSearchConfigModel config, final List<SolrModifiedItemModel> items)
			throws FacetConfigServiceException, IndexerException, SolrServiceException
	{
		final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getConfiguration(config.getName());

		final Collection<IndexedType> types = facetSearchConfig.getIndexConfig().getIndexedTypes().values();

		for (final IndexedType type : types)
		{
			final List<PK> pks = items.stream().map(item -> PK.fromLong(item.getModifiedPk().longValue()))
					.collect(Collectors.toList());

			synchronizeIndexForType(facetSearchConfig, type, pks);
		}
	}

	protected abstract void synchronizeIndexForType(FacetSearchConfig facetSearchConfig, IndexedType type, Collection<PK> pks)
			throws IndexerException, SolrServiceException;

	protected abstract Collection<SolrModifiedItemModel> findModifiedItems();

	@Required
	public void setSolrModifiedItemDAO(final SolrModifiedItemDAO solrModifiedItemDAO)
	{
		this.solrModifiedItemDAO = solrModifiedItemDAO;
	}

	public SolrModifiedItemDAO getSolrModifiedItemDAO()
	{
		return solrModifiedItemDAO;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	public BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}
}
