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
package de.hybris.platform.solrfacetsearch.config.cache.impl;

import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.regioncache.region.impl.LRUCacheRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class FacetSearchConfigCacheRegion extends LRUCacheRegion
{
	public FacetSearchConfigCacheRegion(final String name, final int maxEntries, final boolean statsEnabled)
	{
		super(name, maxEntries, statsEnabled);
	}

	@Override
	@Required
	public void setHandledTypes(final String[] handledTypes)
	{
		this.handledTypes = Arrays.copyOf(handledTypes, handledTypes.length);
	}

	/**
	 * Method return list of the key for given config name and tenantId
	 *
	 * @param configName
	 *           - facet search configuration name
	 * @param tenantId
	 *           - tenant identifier
	 */
	public List<CacheKey> findCachedObjectKeys(final String configName, final String tenantId)
	{
		final List<CacheKey> cachedObjectKeys = new ArrayList<>();
		for (final CacheKey key : cacheMap.keySet())
		{
			if (tenantId.equals(key.getTenantId()) && configName.equals(((FacetSearchConfigCacheKey) key).getName()))
			{
				cachedObjectKeys.add(key);
			}
		}
		return cachedObjectKeys;
	}
}
