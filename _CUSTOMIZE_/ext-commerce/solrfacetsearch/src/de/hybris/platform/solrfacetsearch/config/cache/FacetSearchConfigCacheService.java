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
package de.hybris.platform.solrfacetsearch.config.cache;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;


/**
 * Service for managing FacetSearchConfig cache.
 */
public interface FacetSearchConfigCacheService
{
	/**
	 * Put the facet search config in the cache.
	 *
	 * @param name to be ot used as reference.
	 *
	 * @return {@link FacetSearchConfig}
	 */
	FacetSearchConfig putOrGetFromCache(String name);

	/**
	 * Invalidate cache for a certain name.
	 *
	 * @param name to be ot used as reference.
	 */
	void invalidate(String name);
}
