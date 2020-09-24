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

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueriesExecutor;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;


/**
 * Default implementation of {@link IndexerQueriesExecutor}.
 */
public class DefaultIndexerQueriesExecutor implements IndexerQueriesExecutor
{
	private static final Logger LOG = Logger.getLogger(DefaultIndexerQueriesExecutor.class);

	private FlexibleSearchService flexibleSearchService;
	private IndexerQueryContextFactory<IndexerQueryContext> indexerQueryContextFactory;

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	public IndexerQueryContextFactory<IndexerQueryContext> getIndexerQueryContextFactory()
	{
		return indexerQueryContextFactory;
	}

	@Required
	public void setIndexerQueryContextFactory(final IndexerQueryContextFactory<IndexerQueryContext> indexerQueryContextFactory)
	{
		this.indexerQueryContextFactory = indexerQueryContextFactory;
	}

	@Override
	public List<PK> getPks(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final String query,
			final Map<String, Object> queryParameters) throws IndexerException
	{
		try
		{
			indexerQueryContextFactory.createContext(facetSearchConfig, indexedType, query, queryParameters);
			indexerQueryContextFactory.initializeContext();

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Query: " + query);
			}

			final StopWatch timer = new StopWatch();
			timer.start();

			final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query, queryParameters);
			fsQuery.setResultClassList(Arrays.asList(PK.class));
			final SearchResult<PK> fsResult = flexibleSearchService.search(fsQuery);

			timer.stop();

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Number of pks: " + fsResult.getTotalCount() + ", query time: " + timer.getTotalTimeSeconds() + "s.");
			}

			indexerQueryContextFactory.destroyContext();
			return fsResult.getResult();
		}
		catch (final IndexerException | RuntimeException e)
		{
			indexerQueryContextFactory.destroyContext(e);
			throw e;
		}
	}

	@Override
	public List<ItemModel> getItems(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<PK> pks) throws IndexerException
	{
		final String query = "SELECT {pk} FROM {" + indexedType.getCode() + "} where {pk} in (?pks)";
		final Map<String, Object> queryParameters = Collections.<String, Object> singletonMap("pks", pks);

		try
		{
			indexerQueryContextFactory.createContext(facetSearchConfig, indexedType, query, queryParameters);
			indexerQueryContextFactory.initializeContext();

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Query: " + query);
			}

			final StopWatch timer = new StopWatch();
			timer.start();

			final FlexibleSearchQuery fsQuery = new FlexibleSearchQuery(query, queryParameters);
			final SearchResult<ItemModel> fsResult = flexibleSearchService.search(fsQuery);

			timer.stop();

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Number of items: " + fsResult.getTotalCount() + ", query time: " + timer.getTotalTimeSeconds() + "s.");
			}

			indexerQueryContextFactory.destroyContext();
			return fsResult.getResult();
		}
		catch (final IndexerException | RuntimeException e)
		{
			indexerQueryContextFactory.destroyContext(e);
			throw e;
		}
	}
}
