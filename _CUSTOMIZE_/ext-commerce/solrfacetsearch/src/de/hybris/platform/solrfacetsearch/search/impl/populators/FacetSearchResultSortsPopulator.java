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
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;


public class FacetSearchResultSortsPopulator implements Populator<SearchResultConverterData, SolrSearchResult>
{

	@Override
	public void populate(final SearchResultConverterData source, final SolrSearchResult target)
	{
		final FacetSearchContext facetSearchContext = source.getFacetSearchContext();
		final IndexedTypeSort namedSort = facetSearchContext.getNamedSort();

		target.setCurrentNamedSort(namedSort);
		target.setAvailableNamedSorts(facetSearchContext.getAvailableNamedSorts());
	}
}
