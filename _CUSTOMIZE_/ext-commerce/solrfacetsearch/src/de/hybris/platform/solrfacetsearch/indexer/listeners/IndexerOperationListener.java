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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.enums.IndexMode;
import de.hybris.platform.solrfacetsearch.indexer.ExtendedIndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexOperationService;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import org.springframework.beans.factory.annotation.Required;


/**
 * Responsible for creating and maintaining of IndexData and Index core/collection.
 */
public class IndexerOperationListener implements ExtendedIndexerListener
{
	public static final String DEFAULT_QUALIFIER = "default";
	public static final String FLIP_QUALIFIER = "flip";
	public static final String FLOP_QUALIFIER = "flop";

	private SolrIndexService solrIndexService;
	private SolrIndexOperationService solrIndexOperationService;
	private SolrSearchProviderFactory solrSearchProviderFactory;


	@Override
	public void afterPrepareContext(final IndexerContext context) throws IndexerException
	{
		try
		{
			final FacetSearchConfig facetSearchConfig = context.getFacetSearchConfig();
			final IndexedType indexedType = context.getIndexedType();
			final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();

			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);

			final IndexOperation indexOperation = context.getIndexOperation();

			if (indexOperation == IndexOperation.FULL)
			{
				String newQualifier = DEFAULT_QUALIFIER;

				if (indexConfig.getIndexMode() == IndexMode.TWO_PHASE)
				{
					newQualifier = resolveStagedQualifier(context.getIndex());
				}

				final Index newIndex = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, newQualifier);
				context.setIndex(newIndex);
			}

			final Index index = context.getIndex();

			// Create index
			final SolrIndexModel solrIndex = solrIndexService.getOrCreateIndex(facetSearchConfig.getName(),
					indexedType.getIdentifier(), index.getQualifier());

			// Start operation
			solrIndexOperationService.startOperation(solrIndex, context.getIndexOperationId(), indexOperation,
					context.isExternalIndexOperation());

			if (indexOperation == IndexOperation.FULL)
			{
				// Create index in Solr if it does not exist & export the configuration
				solrSearchProvider.createIndex(context.getIndex());
				solrSearchProvider.exportConfig(context.getIndex());

				if (indexConfig.getIndexMode() == IndexMode.TWO_PHASE)
				{
					solrSearchProvider.deleteAllDocuments(index);
				}
			}
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	@Override
	public void beforeIndex(final IndexerContext context) throws IndexerException
	{
		// No implementation
	}

	@Override
	public void afterIndex(final IndexerContext context) throws IndexerException
	{
		try
		{
			final FacetSearchConfig facetSearchConfig = context.getFacetSearchConfig();
			final IndexedType indexedType = context.getIndexedType();
			final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
			final Index index = context.getIndex();

			final IndexOperation indexOperation = context.getIndexOperation();

			if (indexOperation == IndexOperation.FULL && indexConfig.getIndexMode() == IndexMode.DIRECT)
			{
				final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
						indexedType);
				solrSearchProvider.deleteOldDocuments(index, context.getIndexOperationId());
			}

			solrIndexService.activateIndex(facetSearchConfig.getName(), indexedType.getIdentifier(), index.getQualifier());

			// End operation
			solrIndexOperationService.endOperation(context.getIndexOperationId(), false);
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	@Override
	public void afterIndexError(final IndexerContext context) throws IndexerException
	{
		try
		{
			// End operation with error
			solrIndexOperationService.endOperation(context.getIndexOperationId(), true);
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	protected String resolveStagedQualifier(final Index index)
	{
		if (index != null && FLIP_QUALIFIER.equals(index.getQualifier()))
		{
			return FLOP_QUALIFIER;
		}

		return FLIP_QUALIFIER;
	}

	public SolrIndexOperationService getSolrIndexOperationService()
	{
		return solrIndexOperationService;
	}

	@Required
	public void setSolrIndexOperationService(final SolrIndexOperationService solrIndexOperationService)
	{
		this.solrIndexOperationService = solrIndexOperationService;
	}

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
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
