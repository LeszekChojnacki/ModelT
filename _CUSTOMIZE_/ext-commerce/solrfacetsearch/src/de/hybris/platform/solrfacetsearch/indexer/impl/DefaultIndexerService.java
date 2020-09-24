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
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategyFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link IndexerService}.
 */
public class DefaultIndexerService implements IndexerService
{
	private IndexerStrategyFactory indexerStrategyFactory;

	public IndexerStrategyFactory getIndexerStrategyFactory()
	{
		return indexerStrategyFactory;
	}

	@Required
	public void setIndexerStrategyFactory(final IndexerStrategyFactory indexerStrategyFactory)
	{
		this.indexerStrategyFactory = indexerStrategyFactory;
	}

	@Override
	public void performFullIndex(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		performFullIndex(facetSearchConfig, Collections.<String, String> emptyMap());
	}

	@Override
	public void performFullIndex(final FacetSearchConfig facetSearchConfig, final Map<String, String> indexerHints)
			throws IndexerException
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		for (final IndexedType indexedType : indexConfig.getIndexedTypes().values())
		{
			final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
			indexerStrategy.setIndexOperation(IndexOperation.FULL);
			indexerStrategy.setFacetSearchConfig(facetSearchConfig);
			indexerStrategy.setIndexedType(indexedType);
			indexerStrategy.setIndexerHints(indexerHints);
			indexerStrategy.execute();
		}
	}

	@Override
	public void updateIndex(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		updateIndex(facetSearchConfig, Collections.<String, String> emptyMap());
	}

	@Override
	public void updateIndex(final FacetSearchConfig facetSearchConfig, final Map<String, String> indexerHints)
			throws IndexerException
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		for (final IndexedType indexedType : indexConfig.getIndexedTypes().values())
		{
			final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
			indexerStrategy.setIndexOperation(IndexOperation.UPDATE);
			indexerStrategy.setFacetSearchConfig(facetSearchConfig);
			indexerStrategy.setIndexedType(indexedType);
			indexerStrategy.setIndexerHints(indexerHints);
			indexerStrategy.execute();
		}
	}

	@Override
	public void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType) throws IndexerException
	{
		updateTypeIndex(facetSearchConfig, indexedType, Collections.<String, String> emptyMap());
	}

	@Override
	public void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Map<String, String> indexerHints) throws IndexerException
	{
		final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
		indexerStrategy.setIndexOperation(IndexOperation.UPDATE);
		indexerStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerStrategy.setIndexedType(indexedType);
		indexerStrategy.setIndexerHints(indexerHints);
		indexerStrategy.execute();
	}

	@Override
	public void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final List<PK> pks)
			throws IndexerException
	{
		updateTypeIndex(facetSearchConfig, indexedType, pks, Collections.<String, String> emptyMap());
	}

	@Override
	public void updateTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final List<PK> pks,
			final Map<String, String> indexerHints) throws IndexerException
	{
		final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
		indexerStrategy.setIndexOperation(IndexOperation.UPDATE);
		indexerStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerStrategy.setIndexedType(indexedType);
		indexerStrategy.setPks(pks);
		indexerStrategy.setIndexerHints(indexerHints);
		indexerStrategy.execute();
	}

	@Override
	public void updatePartialTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties, final List<PK> pks) throws IndexerException
	{
		updatePartialTypeIndex(facetSearchConfig, indexedType, indexedProperties, pks, Collections.<String, String> emptyMap());
	}

	@Override
	public void updatePartialTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties, final List<PK> pks, final Map<String, String> indexerHints)
			throws IndexerException
	{
		final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
		indexerStrategy.setIndexOperation(IndexOperation.PARTIAL_UPDATE);
		indexerStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerStrategy.setIndexedType(indexedType);
		indexerStrategy.setIndexedProperties(indexedProperties);
		indexerStrategy.setPks(pks);
		indexerStrategy.setIndexerHints(indexerHints);
		indexerStrategy.execute();
	}

	@Override
	public void deleteFromIndex(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		deleteFromIndex(facetSearchConfig, Collections.<String, String> emptyMap());
	}

	@Override
	public void deleteFromIndex(final FacetSearchConfig facetSearchConfig, final Map<String, String> indexerHints)
			throws IndexerException
	{
		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		for (final IndexedType indexedType : indexConfig.getIndexedTypes().values())
		{
			final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
			indexerStrategy.setIndexOperation(IndexOperation.DELETE);
			indexerStrategy.setFacetSearchConfig(facetSearchConfig);
			indexerStrategy.setIndexedType(indexedType);
			indexerStrategy.setIndexerHints(indexerHints);
			indexerStrategy.execute();
		}
	}

	@Override
	public void deleteTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType) throws IndexerException
	{
		deleteTypeIndex(facetSearchConfig, indexedType, Collections.<String, String> emptyMap());
	}

	@Override
	public void deleteTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Map<String, String> indexerHints) throws IndexerException
	{
		final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
		indexerStrategy.setIndexOperation(IndexOperation.DELETE);
		indexerStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerStrategy.setIndexedType(indexedType);
		indexerStrategy.setIndexerHints(indexerHints);
		indexerStrategy.execute();
	}

	@Override
	public void deleteTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final List<PK> pks)
			throws IndexerException
	{
		deleteTypeIndex(facetSearchConfig, indexedType, pks, Collections.<String, String> emptyMap());
	}

	@Override
	public void deleteTypeIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final List<PK> pks,
			final Map<String, String> indexerHints) throws IndexerException
	{
		final IndexerStrategy indexerStrategy = createIndexerStrategy(facetSearchConfig);
		indexerStrategy.setIndexOperation(IndexOperation.DELETE);
		indexerStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerStrategy.setIndexedType(indexedType);
		indexerStrategy.setPks(pks);
		indexerStrategy.setIndexerHints(indexerHints);
		indexerStrategy.execute();
	}

	protected IndexerStrategy createIndexerStrategy(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		return indexerStrategyFactory.createIndexerStrategy(facetSearchConfig);
	}
}
