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
package de.hybris.platform.solrfacetsearch.indexer.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.ExporterException;
import de.hybris.platform.solrfacetsearch.indexer.spi.Exporter;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link Exporter} for Solr.
 */
public class SolrServerExporter implements Exporter
{
	private static final Logger LOG = LoggerFactory.getLogger(SolrServerExporter.class);

	private IndexerBatchContextFactory<IndexerBatchContext> indexerBatchContextFactory;
	private SolrSearchProviderFactory solrSearchProviderFactory;

	public IndexerBatchContextFactory<IndexerBatchContext> getIndexerBatchContextFactory()
	{
		return indexerBatchContextFactory;
	}

	@Required
	public void setIndexerBatchContextFactory(final IndexerBatchContextFactory<IndexerBatchContext> indexerBatchContextFactory)
	{
		this.indexerBatchContextFactory = indexerBatchContextFactory;
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

	@Override
	public void exportToUpdateIndex(final Collection<SolrInputDocument> solrDocuments, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws ExporterException
	{
		if (CollectionUtils.isEmpty(solrDocuments))
		{
			LOG.warn("solrDocuments should not be empty");
			return;
		}

		SolrClient solrClient = null;

		try
		{
			final IndexerBatchContext batchContext = indexerBatchContextFactory.getContext();
			final Index index = batchContext.getIndex();
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			solrClient = solrSearchProvider.getClientForIndexing(index);

			solrClient.add(index.getName(), solrDocuments);
		}
		catch (final SolrServiceException | SolrServerException | IOException exception)
		{
			throw new ExporterException(exception.getMessage(), exception);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void exportToDeleteFromIndex(final Collection<String> idsToDelete, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws ExporterException
	{
		if (CollectionUtils.isEmpty(idsToDelete))
		{
			LOG.warn("idsToDelete should not be empty");
			return;
		}

		SolrClient solrClient = null;

		try
		{
			final IndexerBatchContext batchContext = indexerBatchContextFactory.getContext();
			final Index index = batchContext.getIndex();
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			solrClient = solrSearchProvider.getClientForIndexing(index);

			solrClient.deleteById(index.getName(), new ArrayList<String>(idsToDelete));
		}
		catch (final SolrServiceException | SolrServerException | IOException exception)
		{
			throw new ExporterException(exception.getMessage(), exception);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}
}
