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
package com.hybris.backoffice.solrsearch.converters;

import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.Breadcrumb;
import de.hybris.platform.solrfacetsearch.search.Facet;

import java.util.Collection;
import java.util.List;

import com.hybris.cockpitng.search.data.facet.FacetData;


/**
 * Full text search data converter. Converts solrfacetsearch pojo objects to cockpitng representation.
 */
public interface FullTextSearchDataConverter
{
	/**
	 * Converts facets from {@link Facet} to {@link FacetData}
	 * 
	 * @param facets available facets facets.
	 * @param breadcrumbs breadcrumbs which represent selected facets.
	 * @param indexedType indexed type which is being searched.
	 * @return facet data - available facets merged with breadcrumbs.
	 */
	Collection<FacetData> convertFacets(Collection<Facet> facets, final List<Breadcrumb> breadcrumbs, final IndexedType indexedType);
}
