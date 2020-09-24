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
package com.hybris.backoffice.solrsearch.events;

import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


/**
 * Direct solr index synchronization strategy
 */
public class DirectSolrIndexSynchronizationStrategy implements SolrIndexSynchronizationStrategy
{

	protected final Logger LOG = LoggerFactory.getLogger(DirectSolrIndexSynchronizationStrategy.class);

	protected IndexerService indexerService;
	protected ModelService modelService;
	protected TypeService typeService;
	protected FacetSearchConfigService facetSearchConfigService;
	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;

	@Override
	public void removeItem(final String typecode, final long pk)
	{
		removeItems(typecode, Collections.singletonList(PK.fromLong(pk)));
	}

	@Override
	public void removeItems(final String typecode, final List<PK> pkList)
	{
		try
		{
			final SolrFacetSearchConfigModel searchConfig = backofficeFacetSearchConfigService
					.getSolrFacetSearchConfigModel(typecode);

			if (searchConfig != null)
			{
				performIndexDelete(typecode, searchConfig, pkList.stream().map(PK::toString).collect(Collectors.toList()));
			}
		}
		catch (FacetConfigServiceException e)
		{
			LOG.warn("Solr facet search config cannot be found for type: " + typecode, e);
		}
	}

	@Override
	public void updateItem(final String typecode, final long pk)
	{
		updateItems(typecode, Collections.singletonList(PK.fromLong(pk)));
	}

	@Override
	public void updateItems(final String typecode, final List<PK> pkList)
	{
		try
		{
			final SolrFacetSearchConfigModel searchConfig = backofficeFacetSearchConfigService
					.getSolrFacetSearchConfigModel(typecode);

			if (searchConfig != null)
			{
				performIndexUpdate(typecode, searchConfig, pkList.stream().map(PK::toString).collect(Collectors.toList()));
			}
		}
		catch (FacetConfigServiceException e)
		{
			LOG.warn("Solr facet search config cannot be found for type: " + typecode, e);
		}
	}

	/**
	 * Remove specified items from solr index
	 *
	 * @param typecode type of items
	 * @param solrFacetSearchConfig solr config
	 * @param pks list of pks
	 * @deprecated
	 * @since 1808
	 */
	@Deprecated
	protected void performIndexDelete(final String typecode, final SolrFacetSearchConfigModel solrFacetSearchConfig,
			final List<String> pks)
	{
		performIndexDelete(solrFacetSearchConfig, pks.stream().map(PK::parse).collect(Collectors.toList()));
	}

	/**
	 * Remove specified items from solr index
	 * @param solrFacetSearchConfig solr config
	 * @param pks list of pks
	 */
	protected void performIndexDelete(final SolrFacetSearchConfigModel solrFacetSearchConfig,
									  final List<PK> pks)
	{
		final FacetSearchConfig facetSearchConfig = findFacetSearchConfig(solrFacetSearchConfig);

		if (facetSearchConfig != null)
		{
			final Collection<IndexedType> indexedTypes = facetSearchConfig.getIndexConfig().getIndexedTypes().values();

			for (final IndexedType type : indexedTypes)
			{
				try
				{
					indexerService.deleteTypeIndex(facetSearchConfig, type, pks);
				}
				catch (final IndexerException e)
				{
					LOG.warn("Error during indexer call: " + facetSearchConfig.getName(), e);
				}
			}
		}
	}

	/**
	 * Update specified items in solr index
	 *
	 * @param typecode type of items
	 * @param solrFacetSearchConfig solr config
	 * @param pks list of pks
	 * @deprecated
	 * @since 1808
	 */
	@Deprecated
	protected void performIndexUpdate(final String typecode, final SolrFacetSearchConfigModel solrFacetSearchConfig,
			final List<String> pks)
	{
		performIndexUpdate(solrFacetSearchConfig, pks.stream().map(PK::parse).collect(Collectors.toList()));
	}

	/**
	 * Update specified items in solr index
	 * @param solrFacetSearchConfig solr config
	 * @param pks list of pks
	 */
	protected void performIndexUpdate(final SolrFacetSearchConfigModel solrFacetSearchConfig,
			final List<PK> pks)
	{
		final FacetSearchConfig facetSearchConfig = findFacetSearchConfig(solrFacetSearchConfig);

		if (facetSearchConfig != null)
		{
			final Collection<IndexedType> indexedTypes = facetSearchConfig.getIndexConfig().getIndexedTypes().values();

			for (final IndexedType type : indexedTypes)
			{
				try
				{
					indexerService.updateTypeIndex(facetSearchConfig, type, pks);
				}
				catch (final IndexerException e)
				{
					LOG.warn("Error during indexer call: " + facetSearchConfig.getName(), e);
				}
			}
		}
	}

	/**
	 * Finds solr search config
	 * @param facetSearchConfigModel
	 */
	protected FacetSearchConfig findFacetSearchConfig(final SolrFacetSearchConfigModel facetSearchConfigModel)
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
	 * @param indexerService
	 */
	@Required
	public void setIndexerService(final IndexerService indexerService)
	{
		this.indexerService = indexerService;
	}

	/**
	 * @param modelService
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @param facetSearchConfigService
	 *           the facetSearchConfigService to set
	 */
	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	/**
	 * @param typeService
	 */
	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}
}
