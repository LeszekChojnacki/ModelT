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
package de.hybris.platform.adaptivesearchsolr.strategies;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;


/**
 * Implementations of this interface should resolve the catalog version.
 */
@FunctionalInterface
public interface SolrAsCatalogVersionResolver
{
	/**
	 * Resolves the catalog versions from the search query.
	 *
	 * @param searchQuery
	 *           - the search query
	 *
	 * @return the catalog versions
	 */
	List<CatalogVersionModel> resolveCatalogVersions(SearchQuery searchQuery);
}
