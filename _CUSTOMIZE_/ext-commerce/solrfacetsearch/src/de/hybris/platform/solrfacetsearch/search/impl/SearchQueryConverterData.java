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

import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;


public class SearchQueryConverterData
{
	private FacetSearchContext facetSearchContext;
	private SearchQuery searchQuery;
	private Map<String, Object> attributes;

	public SearchQueryConverterData()
	{
		attributes = new HashMap<String, Object>();
	}

	public FacetSearchContext getFacetSearchContext()
	{
		return facetSearchContext;
	}

	public void setFacetSearchContext(final FacetSearchContext facetSearchContext)
	{
		this.facetSearchContext = facetSearchContext;
	}

	public SearchQuery getSearchQuery()
	{
		return searchQuery;
	}

	public void setSearchQuery(final SearchQuery searchQuery)
	{
		this.searchQuery = searchQuery;
	}

	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(final Map<String, Object> attributes)
	{
		this.attributes = attributes;
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

		final SearchQueryConverterData that = (SearchQueryConverterData) obj;
		return new EqualsBuilder()
				.append(this.facetSearchContext, that.facetSearchContext)
				.append(this.searchQuery, that.searchQuery)
				.append(this.attributes, that.attributes)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.facetSearchContext, this.searchQuery, this.attributes);
	}
}
