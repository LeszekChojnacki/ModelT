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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.solr.client.solrj.response.QueryResponse;


public class SearchResultConverterData
{
	private FacetSearchContext facetSearchContext;
	private QueryResponse queryResponse;
	private final Map<String, Object> attributes;

	public SearchResultConverterData()
	{
		attributes = new HashMap<>();
	}

	public FacetSearchContext getFacetSearchContext()
	{
		return facetSearchContext;
	}

	public void setFacetSearchContext(final FacetSearchContext facetSearchContext)
	{
		this.facetSearchContext = facetSearchContext;
	}

	public QueryResponse getQueryResponse()
	{
		return queryResponse;
	}

	public void setQueryResponse(final QueryResponse queryResponse)
	{
		this.queryResponse = queryResponse;
	}

	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final SearchResultConverterData that = (SearchResultConverterData) obj;
		return new EqualsBuilder()
				.append(this.facetSearchContext, that.facetSearchContext)
				.append(this.queryResponse, that.queryResponse)
				.append(this.attributes, that.attributes)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.facetSearchContext, this.queryResponse, this.attributes);
	}
}
