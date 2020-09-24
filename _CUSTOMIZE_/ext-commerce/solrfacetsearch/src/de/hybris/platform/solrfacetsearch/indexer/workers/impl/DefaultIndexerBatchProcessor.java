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
package de.hybris.platform.solrfacetsearch.indexer.workers.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.processing.distributed.simple.SimpleBatchProcessor;
import de.hybris.platform.processing.model.SimpleBatchModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerRuntimeException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategyFactory;
import de.hybris.platform.solrfacetsearch.model.SolrIndexerDistributedProcessModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


public class DefaultIndexerBatchProcessor implements SimpleBatchProcessor
{
	private FacetSearchConfigService facetSearchConfigService;
	private IndexerBatchStrategyFactory indexerBatchStrategyFactory;
	private SolrSearchProviderFactory solrSearchProviderFactory;

	@Override
	public void process(final SimpleBatchModel inputBatch)
	{
		try
		{
			final SolrIndexerDistributedProcessModel distributedProcessModel = (SolrIndexerDistributedProcessModel) inputBatch
					.getProcess();

			final FacetSearchConfig facetSearchConfig = facetSearchConfigService
					.getConfiguration(distributedProcessModel.getFacetSearchConfig());
			final IndexedType indexedType = facetSearchConfigService.resolveIndexedType(facetSearchConfig,
					distributedProcessModel.getIndexedType());
			final List<IndexedProperty> indexedProperties = facetSearchConfigService.resolveIndexedProperties(facetSearchConfig,
					indexedType, distributedProcessModel.getIndexedProperties());
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			final Index index = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, distributedProcessModel.getIndex());
			final List<PK> pks = asList(inputBatch.getContext());

			final IndexerBatchStrategy indexerBatchStrategy = indexerBatchStrategyFactory
					.createIndexerBatchStrategy(facetSearchConfig);

			indexerBatchStrategy.setIndexOperationId(distributedProcessModel.getIndexOperationId());
			indexerBatchStrategy
					.setIndexOperation(
							IndexOperation.valueOf(distributedProcessModel.getIndexOperation().getCode().toUpperCase(Locale.ROOT)));
			indexerBatchStrategy.setExternalIndexOperation(distributedProcessModel.isExternalIndexOperation());
			indexerBatchStrategy.setFacetSearchConfig(facetSearchConfig);
			indexerBatchStrategy.setIndexedType(indexedType);
			indexerBatchStrategy.setIndexedProperties(indexedProperties);
			indexerBatchStrategy.setIndex(index);
			indexerBatchStrategy.setIndexerHints(distributedProcessModel.getIndexerHints());
			indexerBatchStrategy.setPks(pks);

			indexerBatchStrategy.execute();
		}
		catch (final IndexerException | FacetConfigServiceException | SolrServiceException e)
		{
			throw new IndexerRuntimeException(e);
		}
		catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	protected List<PK> asList(final Object ctx)
	{
		Preconditions.checkState(ctx instanceof List, "ctx must be instance of List");

		return (List<PK>) ctx;
	}

	public FacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	public IndexerBatchStrategyFactory getIndexerBatchStrategyFactory()
	{
		return indexerBatchStrategyFactory;
	}

	@Required
	public void setIndexerBatchStrategyFactory(final IndexerBatchStrategyFactory indexerBatchStrategyFactory)
	{
		this.indexerBatchStrategyFactory = indexerBatchStrategyFactory;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}
}
