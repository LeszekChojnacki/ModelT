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
package de.hybris.platform.solrfacetsearch.indexer.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfAnyResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueriesExecutor;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.spi.Indexer;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategy;
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link IndexerBatchStrategy}
 */
public class DefaultIndexerBatchStrategy implements IndexerBatchStrategy
{
	// dependencies
	private IndexerQueriesExecutor indexerQueriesExecutor;
	private IndexerBatchContextFactory<?> indexerBatchContextFactory;
	private Indexer indexer;

	// state
	private long indexOperationId;
	private IndexOperation indexOperation;
	private boolean externalIndexOperation;
	private FacetSearchConfig facetSearchConfig;
	private IndexedType indexedType;
	private Collection<IndexedProperty> indexedProperties;
	private Index index;
	private Map<String, String> indexerHints;
	private List<PK> pks;

	public Indexer getIndexer()
	{
		return indexer;
	}

	@Required
	public void setIndexer(final Indexer indexer)
	{
		this.indexer = indexer;
	}

	public IndexerBatchContextFactory getIndexerBatchContextFactory()
	{
		return indexerBatchContextFactory;
	}

	@Required
	public void setIndexerBatchContextFactory(final IndexerBatchContextFactory<?> indexerBatchContextFactory)
	{
		this.indexerBatchContextFactory = indexerBatchContextFactory;
	}

	public IndexerQueriesExecutor getIndexerQueriesExecutor()
	{
		return indexerQueriesExecutor;
	}

	@Required
	public void setIndexerQueriesExecutor(final IndexerQueriesExecutor indexerQueriesExecutor)
	{
		this.indexerQueriesExecutor = indexerQueriesExecutor;
	}

	public boolean isExternalIndexOperation()
	{
		return externalIndexOperation;
	}

	@Override
	public void setExternalIndexOperation(final boolean externalIndexOperation)
	{
		this.externalIndexOperation = externalIndexOperation;
	}

	public FacetSearchConfig getFacetSearchConfig()
	{
		return facetSearchConfig;
	}

	@Override
	public void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig)
	{
		this.facetSearchConfig = facetSearchConfig;
	}

	public Index getIndex()
	{
		return index;
	}

	@Override
	public void setIndex(final Index index)
	{
		this.index = index;
	}

	public Collection<IndexedProperty> getIndexedProperties()
	{
		return indexedProperties;
	}

	@Override
	public void setIndexedProperties(final Collection<IndexedProperty> indexedProperties)
	{
		this.indexedProperties = indexedProperties;
	}

	public IndexedType getIndexedType()
	{
		return indexedType;
	}

	@Override
	public void setIndexedType(final IndexedType indexedType)
	{
		this.indexedType = indexedType;
	}

	public Map<String, String> getIndexerHints()
	{
		return indexerHints;
	}

	@Override
	public void setIndexerHints(final Map<String, String> indexerHints)
	{
		this.indexerHints = indexerHints;
	}

	public IndexOperation getIndexOperation()
	{
		return indexOperation;
	}

	@Override
	public void setIndexOperation(final IndexOperation indexOperation)
	{
		this.indexOperation = indexOperation;
	}

	public long getIndexOperationId()
	{
		return indexOperationId;
	}

	@Override
	public void setIndexOperationId(final long indexOperationId)
	{
		this.indexOperationId = indexOperationId;
	}

	public List<PK> getPks()
	{
		return pks;
	}

	@Override
	public void setPks(final List<PK> pks)
	{
		this.pks = pks;
	}

	@Override
	public void execute() throws InterruptedException, IndexerException
	{
		validateRequiredFields();

		try
		{
			final IndexerBatchContext batchContext = indexerBatchContextFactory.createContext(indexOperationId, indexOperation,
					externalIndexOperation, facetSearchConfig, indexedType, indexedProperties);
			batchContext.getIndexerHints().putAll(indexerHints);
			batchContext.setIndex(index);

			indexerBatchContextFactory.prepareContext();

			if (batchContext.getIndexOperation() == IndexOperation.DELETE)
			{
				batchContext.setPks(pks);
				batchContext.setItems(Collections.emptyList());
			}
			else
			{
				final List<ItemModel> items = executeIndexerQuery(facetSearchConfig, indexedType, pks);
				batchContext.setItems(items);
			}

			indexerBatchContextFactory.initializeContext();
			executeIndexerOperation(batchContext);
			indexerBatchContextFactory.destroyContext();
		}
		catch (final IndexerException | InterruptedException | RuntimeException e)
		{
			indexerBatchContextFactory.destroyContext(e);
			throw e;
		}
	}

	protected void validateRequiredFields()
	{
		validateParameterNotNull(indexOperation, "indexOperation field not set");
		validateParameterNotNull(facetSearchConfig, "facetSearchConfig field not set");
		validateParameterNotNull(indexedType, "indexedType field not set");
		validateParameterNotNull(indexedProperties, "indexedProperties field not set");
		validateParameterNotNull(index, "index field not set");
		validateParameterNotNull(indexerHints, "indexerHints field not set");
		validateParameterNotNull(pks, "pks field not set");
		validateIfAnyResult(pks, "pks field not set");
	}

	protected List<ItemModel> executeIndexerQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final List<PK> pks) throws IndexerException
	{
		return indexerQueriesExecutor.getItems(facetSearchConfig, indexedType, pks);
	}


	protected void executeIndexerOperation(final IndexerBatchContext batchContext) throws IndexerException, InterruptedException
	{
		switch (batchContext.getIndexOperation())
		{
			case FULL:
			case UPDATE:
				indexer.indexItems(batchContext.getItems(), batchContext.getFacetSearchConfig(), batchContext.getIndexedType());
				break;

			case PARTIAL_UPDATE:
				indexer.indexItems(batchContext.getItems(), batchContext.getFacetSearchConfig(), batchContext.getIndexedType(),
						batchContext.getIndexedProperties());
				break;

			case DELETE:
				indexer.removeItemsByPk(batchContext.getPks(), batchContext.getFacetSearchConfig(), batchContext.getIndexedType(),
						batchContext.getIndex());
				break;

			default:
				throw new IndexerException("Unsupported index operation: " + batchContext.getIndexOperation());
		}
	}

}
