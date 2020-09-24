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
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;


public class FacetSearchQueryParamsPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final Map<String, String[]> rawParams = searchQuery.getRawParams();

		for (final Entry<String, String[]> entry : rawParams.entrySet())
		{
			target.add(entry.getKey(), entry.getValue());
		}
	}
}
