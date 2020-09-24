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

import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.RawQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;


public class FacetSearchQueryFilterQueriesPopulator extends AbstractFacetSearchQueryPopulator
{
	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final List<String> filterQueries = new ArrayList<>();

		addQueryFieldQueries(searchQuery, filterQueries);
		addRawQueries(searchQuery, filterQueries);

		for (final String filterQuery : filterQueries)
		{
			target.addFilterQuery(filterQuery);
		}
	}

	protected void addQueryFieldQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		for (final QueryField filterQuery : searchQuery.getFilterQueries())
		{
			final String query = convertQueryField(searchQuery, filterQuery);
			queries.add(query);
		}
	}

	protected void addRawQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		for (final RawQuery filterRawQuery : searchQuery.getFilterRawQueries())
		{
			final String query = convertRawQuery(searchQuery, filterRawQuery);
			queries.add(query);
		}
	}
}
