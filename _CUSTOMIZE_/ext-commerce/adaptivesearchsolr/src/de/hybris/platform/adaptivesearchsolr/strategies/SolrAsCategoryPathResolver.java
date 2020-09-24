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
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;


/**
 * Implementations of this interface should build the category path.
 */
@FunctionalInterface
public interface SolrAsCategoryPathResolver
{
	/**
	 * Returns the category path.
	 *
	 * @param searchQuery
	 *           - the search query
	 * @param catalogVersions
	 *           - the catalog versions
	 *
	 * @return the category path
	 */
	List<CategoryModel> resolveCategoryPath(SearchQuery searchQuery, List<CatalogVersionModel> catalogVersions);
}
