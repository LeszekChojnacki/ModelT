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
import de.hybris.platform.regioncache.key.CacheUnitValueType;


public class FacetSearchConfigCacheKey implements CacheKey
{
	private static final String CACHED_TYPE = "__FACET_SEARCH_CONFIG__";

	private final String configName;
	private final String language;
	private final String tenantId;

	public FacetSearchConfigCacheKey(final String configName, final String language, final String tenantId)
	{
		this.configName = configName;
		this.language = language;
		this.tenantId = tenantId;
	}

	@Override
	public CacheUnitValueType getCacheValueType()
	{
		return CacheUnitValueType.NON_SERIALIZABLE;
	}

	@Override
	public Object getTypeCode()
	{
		return CACHED_TYPE;
	}

	@Override
	public String getTenantId()
	{
		return tenantId;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final FacetSearchConfigCacheKey that = (FacetSearchConfigCacheKey) o;

		if (!configName.equals(that.configName))
		{
			return false;
		}
		if (!language.equals(that.language))
		{
			return false;
		}

		return tenantId.equals(that.tenantId);
	}

	@Override
	public int hashCode()
	{
		int result = configName.hashCode();
		result = 31 * result + language.hashCode();
		result = 31 * result + tenantId.hashCode();
		return result;
	}


	@Override
	public String toString()
	{
		return "FacetSearchConfigCacheKey[ configName = " + configName + " , language=" + language + " , tenantId=" + tenantId
				+ " ]";
	}

	public String getName()
	{
		return configName;
	}

	public String getLanguage()
	{
		return language;
	}
}
