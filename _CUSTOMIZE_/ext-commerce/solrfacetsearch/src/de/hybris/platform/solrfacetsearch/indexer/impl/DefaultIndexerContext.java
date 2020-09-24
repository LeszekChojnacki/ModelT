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
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.solr.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link IndexerContext}.
 */
public class DefaultIndexerContext implements IndexerContext
{
	private long indexOperationId;
	private IndexOperation indexOperation;
	private boolean externalIndexOperation;
	private FacetSearchConfig facetSearchConfig;
	private IndexedType indexedType;
	private Collection<IndexedProperty> indexedProperties;

	private List<PK> pks;
	private Index index;
	private final Map<String, String> indexerHints;

	private final Map<String, Object> attributes;
	private Status status;
	private final List<Exception> failureExceptions;

	public DefaultIndexerContext()
	{
		indexerHints = new HashMap<>();
		attributes = new HashMap<>();
		failureExceptions = new ArrayList<>();
	}

	@Override
	public long getIndexOperationId()
	{
		return indexOperationId;
	}

	public void setIndexOperationId(final long indexOperationId)
	{
		this.indexOperationId = indexOperationId;
	}

	@Override
	public IndexOperation getIndexOperation()
	{
		return indexOperation;
	}

	public void setIndexOperation(final IndexOperation indexOperation)
	{
		this.indexOperation = indexOperation;
	}

	@Override
	public boolean isExternalIndexOperation()
	{
		return externalIndexOperation;
	}

	public void setExternalIndexOperation(final boolean externalIndexOperation)
	{
		this.externalIndexOperation = externalIndexOperation;
	}

	@Override
	public FacetSearchConfig getFacetSearchConfig()
	{
		return facetSearchConfig;
	}

	public void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig)
	{
		this.facetSearchConfig = facetSearchConfig;
	}

	@Override
	public IndexedType getIndexedType()
	{
		return indexedType;
	}

	public void setIndexedType(final IndexedType indexedType)
	{
		this.indexedType = indexedType;
	}

	@Override
	public Collection<IndexedProperty> getIndexedProperties()
	{
		return indexedProperties;
	}

	public void setIndexedProperties(final Collection<IndexedProperty> indexedProperties)
	{
		this.indexedProperties = indexedProperties;
	}

	@Override
	public List<PK> getPks()
	{
		return Collections.unmodifiableList(pks);
	}

	@Override
	public void setPks(final List<PK> pks)
	{
		if (Status.CREATED != status && Status.STARTING != status)
		{
			throw new IllegalStateException("expecting status CREATED or STARTING but it was " + status);
		}

		this.pks = pks;
	}

	@Override
	public Index getIndex()
	{
		return index;
	}

	@Override
	public void setIndex(final Index index)
	{
		this.index = index;
	}

	@Override
	public Map<String, String> getIndexerHints()
	{
		return indexerHints;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(final Status status)
	{
		this.status = status;
	}

	public void addFailureException(final Exception exception)
	{
		failureExceptions.add(exception);
	}

	@Override
	public List<Exception> getFailureExceptions()
	{
		return Collections.unmodifiableList(failureExceptions);
	}
}
