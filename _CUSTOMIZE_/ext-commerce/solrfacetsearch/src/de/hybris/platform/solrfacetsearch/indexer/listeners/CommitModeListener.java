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

import de.hybris.platform.solrfacetsearch.config.CommitMode;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider.CommitType;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Listener that applies the commit mode.
 */
public class CommitModeListener implements IndexerListener, IndexerBatchListener
{
	private static final Logger LOG = Logger.getLogger(CommitModeListener.class);

	protected static final CommitMode DEFAULT_COMMIT_MODE = CommitMode.AFTER_INDEX;
	protected static final String COMMIT_MODE_HINT = "commitMode";

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

		final CommitMode commitMode = resolveCommitMode(context.getFacetSearchConfig(), context.getIndexerHints());

		switch (commitMode)
		{
			case AFTER_INDEX:
			case MIXED:
				commit(context.getFacetSearchConfig(), context.getIndexedType(), context.getIndex(), CommitType.HARD);
				break;

			default:
		}
	}

	@Override
	public void afterIndexError(final IndexerContext context) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void beforeBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		// skip execution if there is nothing to index/delete
		if (CollectionUtils.isEmpty(batchContext.getItems()))
		{
			return;
		}

		final CommitMode commitMode = resolveCommitMode(batchContext.getFacetSearchConfig(), batchContext.getIndexerHints());

		switch (commitMode)
		{
			case AFTER_BATCH:
				commit(batchContext.getFacetSearchConfig(), batchContext.getIndexedType(), batchContext.getIndex(), CommitType.HARD);
				break;

			case MIXED:
				commit(batchContext.getFacetSearchConfig(), batchContext.getIndexedType(), batchContext.getIndex(), CommitType.SOFT);
				break;

			default:
		}
	}

	@Override
	public void afterBatchError(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	protected CommitMode resolveCommitMode(final FacetSearchConfig facetSearchConfig, final Map<String, String> indexerHints)
	{
		final String commitModeHint = indexerHints.get(COMMIT_MODE_HINT);
		if (StringUtils.isNotBlank(commitModeHint))
		{

			try
			{
				return CommitMode.valueOf(commitModeHint);
			}
			catch (final IllegalArgumentException e)
			{
				LOG.error("Invalid commitMode indexer hint " + commitModeHint, e);
			}
		}

		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		final CommitMode commitMode = indexConfig.getCommitMode();

		if (commitMode != null)
		{
			return commitMode;
		}

		return DEFAULT_COMMIT_MODE;
	}

	protected void commit(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final Index index,
			final CommitType commitType) throws IndexerException
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Performing " + commitType + " commit on " + index.getName() + " (" + facetSearchConfig.getName() + "/"
					+ indexedType.getUniqueIndexedTypeCode() + ")");
		}

		try
		{
			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			solrSearchProvider.commit(index, commitType);
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
