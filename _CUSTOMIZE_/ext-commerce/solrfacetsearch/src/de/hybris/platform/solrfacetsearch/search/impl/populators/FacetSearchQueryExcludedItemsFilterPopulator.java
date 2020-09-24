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

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;


public class FacetSearchQueryExcludedItemsFilterPopulator extends AbstractFacetSearchQueryPopulator
{
	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final List<PK> excludedItems = searchQuery.getExcludedItems();

		if (CollectionUtils.isNotEmpty(excludedItems))
		{
			final String filterQuery = '-' + SolrfacetsearchConstants.PK_FIELD
					+ excludedItems.stream().map(PK::getLongValueAsString).collect(Collectors.joining(" OR ", ":(", ")"));

			target.addFilterQuery(filterQuery);
		}
	}
}
