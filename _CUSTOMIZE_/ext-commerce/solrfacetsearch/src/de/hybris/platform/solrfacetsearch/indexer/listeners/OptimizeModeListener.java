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
package de.hybris.platform.solrfacetsearch.indexer.listeners;

import static de.hybris.platform.solrfacetsearch.config.IndexOperation.FULL;
import static de.hybris.platform.solrfacetsearch.config.OptimizeMode.AFTER_FULL_INDEX;
import static de.hybris.platform.solrfacetsearch.config.OptimizeMode.AFTER_INDEX;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.OptimizeMode;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Listener that applies the optimize mode.
 */
public class OptimizeModeListener implements IndexerListener
{
	private static final Logger LOG = Logger.getLogger(OptimizeModeListener.class);

	protected static final OptimizeMode DEFAULT_OPTIMIZE_MODE = OptimizeMode.NEVER;
	protected static final String OPTIMIZE_MODE_HINT = "optimizeMode";

	private SolrSearchProviderFactory solrSearchProviderFactory;

	@Override
	public void beforeIndex(final IndexerContext context) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterIndex(final IndexerContext context) throws IndexerException
	{
		// skip execution if there is nothing to index/delete
		if (CollectionUtils.isEmpty(context.getPks()))
		{
			return;
		}

		final OptimizeMode optimizeMode = resolveOptimizeMode(context.getFacetSearchConfig(), context.getIndexerHints());

		if (optimizeMode == AFTER_INDEX || (optimizeMode == AFTER_FULL_INDEX && context.getIndexOperation() == FULL))
		{
			optimize(context.getFacetSearchConfig(), context.getIndexedType(), context.getIndex());
		}
	}

	@Override
	public void afterIndexError(final IndexerContext context) throws IndexerException
	{
		// NOOP
	}

	protected OptimizeMode resolveOptimizeMode(final FacetSearchConfig facetSearchConfig, final Map<String, String> indexerHints)
	{
		final String optimizeModeHint = indexerHints.get(OPTIMIZE_MODE_HINT);
		if (StringUtils.isNotBlank(optimizeModeHint))
		{
			try
			{
				return OptimizeMode.valueOf(optimizeModeHint);
			}
			catch (final IllegalArgumentException e)
			{
				LOG.error("Invalid optimizeMode indexer hint" + optimizeModeHint, e);
			}
		}

		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		final OptimizeMode optimizeMode = indexConfig.getOptimizeMode();

		if (optimizeMode != null)
		{
			return optimizeMode;
		}

		return DEFAULT_OPTIMIZE_MODE;
	}

	protected void optimize(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final Index index)
			throws IndexerException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Performing optimize on " + index.getName() + " (" + facetSearchConfig.getName() + "/"
					+ indexedType.getUniqueIndexedTypeCode() + ")");
		}

		try
		{
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			solrSearchProvider.optimize(index);
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}
}
