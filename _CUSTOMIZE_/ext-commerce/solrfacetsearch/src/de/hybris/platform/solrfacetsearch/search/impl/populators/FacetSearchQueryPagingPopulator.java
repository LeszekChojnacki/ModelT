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

import org.apache.solr.client.solrj.SolrQuery;


public class FacetSearchQueryPagingPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final int start = searchQuery.getOffset() * searchQuery.getPageSize();
		target.setStart(start);
		target.setRows(searchQuery.getPageSize());
	}
}
