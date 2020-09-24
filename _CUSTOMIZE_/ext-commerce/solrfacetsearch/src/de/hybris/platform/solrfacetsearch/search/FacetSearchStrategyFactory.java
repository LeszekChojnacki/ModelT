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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;


/**
 * Implementations of this interface are responsible for creating {@link FacetSearchStrategy}
 */
public interface FacetSearchStrategyFactory
{
	/**
	 * Factory method to create Facet strategy
	 *
	 * @param facetSearchConfig
	 *           Facet search configuration
	 * @param indexedType
	 *           Indexed type
	 *
	 * @return FacetSearchStrategy implementation
	 */
	FacetSearchStrategy createStrategy(FacetSearchConfig facetSearchConfig, IndexedType indexedType);
}
