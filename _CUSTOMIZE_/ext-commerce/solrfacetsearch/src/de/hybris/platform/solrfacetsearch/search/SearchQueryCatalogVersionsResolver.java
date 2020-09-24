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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.List;


/**
 * Implementations of this interface should provide a list of CatalogVersionModel based on FacetSearchConfig and/or IndexedType
 */
public interface SearchQueryCatalogVersionsResolver
{
	/**
	 * Method to resolve the catalog versions
	 *
	 * @param facetSearchConfig
	 * 		- the facet search configuration
	 * @param indexedType
	 * 		- the indexed type
	 *
	 * @return the resolved catalog versions
	 */
	List<CatalogVersionModel> resolveCatalogVersions(FacetSearchConfig facetSearchConfig, IndexedType indexedType);
}
