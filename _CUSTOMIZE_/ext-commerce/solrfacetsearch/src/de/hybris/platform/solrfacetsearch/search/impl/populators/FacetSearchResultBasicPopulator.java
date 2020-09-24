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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverters;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;

import org.springframework.beans.factory.annotation.Required;


public class FacetSearchResultBasicPopulator implements Populator<SearchResultConverterData, SolrSearchResult>
{
	private SearchResultConverters searchResultConverters;

	public SearchResultConverters getSearchResultConverters()
	{
		return searchResultConverters;
	}

	@Required
	public void setSearchResultConverters(final SearchResultConverters searchResultConverters)
	{
		this.searchResultConverters = searchResultConverters;
	}

	@Override
	public void populate(final SearchResultConverterData source, final SolrSearchResult target)
	{
		final SearchQuery searchQuery = source.getFacetSearchContext().getSearchQuery();

		if (searchResultConverters != null)
		{
			final IndexedType indexedType = searchQuery.getIndexedType();
			target.setConvertersMapping(searchResultConverters.getConverterMapping(indexedType.getCode()));
		}

		target.setSearchQuery(searchQuery);
		target.setQueryResponse(source.getQueryResponse());

		target.setBreadcrumbs(searchQuery.getBreadcrumbs());
	}
}
