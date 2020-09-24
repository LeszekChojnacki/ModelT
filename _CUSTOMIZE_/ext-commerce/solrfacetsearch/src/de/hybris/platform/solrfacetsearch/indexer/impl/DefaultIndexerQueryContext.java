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
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link IndexerQueryContext}.
 */
public class DefaultIndexerQueryContext implements IndexerQueryContext
{
	private FacetSearchConfig facetSearchConfig;
	private IndexedType indexedType;
	private String query;
	private Map<String, Object> queryParameters;

	private Status status;
	private final List<Exception> failureExceptions;
	private final Map<String, Object> attributes;

	public DefaultIndexerQueryContext()
	{
		attributes = new HashMap<String, Object>();
		failureExceptions = new ArrayList<Exception>();
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
	public String getQuery()
	{
		return query;
	}

	public void setQuery(final String query)
	{
		this.query = query;
	}

	@Override
	public Map<String, Object> getQueryParameters()
	{
		return queryParameters;
	}

	public void setQueryParameters(final Map<String, Object> queryParameters)
	{
		this.queryParameters = queryParameters;
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

	@Override
	public Map<String, Object> getAttributes()
	{
		return attributes;
	}
}
