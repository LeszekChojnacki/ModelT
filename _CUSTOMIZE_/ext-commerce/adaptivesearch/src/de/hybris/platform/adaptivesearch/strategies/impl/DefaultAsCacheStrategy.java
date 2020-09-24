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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.strategies.AsCacheKey;
import de.hybris.platform.adaptivesearch.strategies.AsCacheScope;
import de.hybris.platform.adaptivesearch.strategies.AsCacheStrategy;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.regioncache.key.CacheUnitValueType;
import de.hybris.platform.regioncache.region.CacheRegion;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.tenant.TenantService;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsCacheStrategy} that uses the region cache from the platform.
 */
public class DefaultAsCacheStrategy implements AsCacheStrategy, InitializingBean
{
	protected static final String AS_CACHE_ENABLED_KEY = "adaptivesearch.cache.enabled";
	protected static final String AS_LOAD_CACHE_ENABLED_KEY = "adaptivesearch.cache.load.enabled";
	protected static final String AS_CALCULATION_CACHE_ENABLED_KEY = "adaptivesearch.cache.calculation.enabled";
	protected static final String AS_MERGE_CACHE_ENABLED_KEY = "adaptivesearch.cache.merge.enabled";

	private TenantService tenantService;
	private ConfigurationService configurationService;
	private CacheRegion cacheRegion;

	private String tenantId;
	private boolean cacheEnabled;
	private boolean loadCacheEnabled;
	private boolean calculationCacheEnabled;

	@Override
	public void afterPropertiesSet()
	{
		this.tenantId = tenantService.getCurrentTenantId();
		loadCacheSettings();
	}

	protected void loadCacheSettings()
	{
		this.cacheEnabled = configurationService.getConfiguration().getBoolean(AS_CACHE_ENABLED_KEY, true);
		this.loadCacheEnabled = configurationService.getConfiguration().getBoolean(AS_LOAD_CACHE_ENABLED_KEY, true);
		this.calculationCacheEnabled = configurationService.getConfiguration().getBoolean(AS_CALCULATION_CACHE_ENABLED_KEY, true);
	}

	@Override
	public boolean isEnabled(final AsCacheScope cacheScope)
	{
		if (!cacheEnabled)
		{
			return false;
		}

		switch (cacheScope)
		{
			case LOAD:
				return loadCacheEnabled;

			case CALCULATION:
				return calculationCacheEnabled;

			default:
				return false;
		}
	}

	@Override
	public <V> V getWithLoader(final AsCacheKey cacheKey, final Function<AsCacheKey, V> valueLoader)
	{
		if (isEnabled(cacheKey.getScope()))
		{
			return (V) cacheRegion.getWithLoader(new HybrisAsCacheKey(tenantId, cacheKey), key -> valueLoader.apply(cacheKey));
		}

		return valueLoader.apply(cacheKey);
	}

	@Override
	public void clear()
	{
		cacheRegion.clearCache();
	}

	@Override
	public long getSize()
	{
		return cacheRegion.getMaxReachedSize();
	}

	@Override
	public long getHits()
	{
		return cacheRegion.getCacheRegionStatistics().getHits();
	}

	@Override
	public long getMisses()
	{
		return cacheRegion.getCacheRegionStatistics().getMisses();
	}

	protected String getTenantId()
	{
		return tenantId;
	}

	public TenantService getTenantService()
	{
		return tenantService;
	}

	@Required
	public void setTenantService(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public CacheRegion getCacheRegion()
	{
		return cacheRegion;
	}

	@Required
	public void setCacheRegion(final CacheRegion cacheRegion)
	{
		this.cacheRegion = cacheRegion;
	}

	protected static class HybrisAsCacheKey implements CacheKey
	{
		private static final String ADAPTIVE_SEARCH_CACHE_UNIT_CODE = "__ADAPTIVE_SEARCH_CACHE__";

		private final String tenantId;
		private final AsCacheKey cacheKey;

		public HybrisAsCacheKey(final String tenantId, final AsCacheKey cacheKey)
		{
			this.tenantId = tenantId;
			this.cacheKey = cacheKey;
		}

		@Override
		public CacheUnitValueType getCacheValueType()
		{
			return CacheUnitValueType.SERIALIZABLE;
		}

		@Override
		public Object getTypeCode()
		{
			return ADAPTIVE_SEARCH_CACHE_UNIT_CODE;
		}

		@Override
		public String getTenantId()
		{
			return tenantId;
		}

		@Override
		public String toString()
		{
			return "HybrisAsCacheKey [tenantId=" + tenantId + " ,cacheKey=" + cacheKey + "]";
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj == null || this.getClass() != obj.getClass())
			{
				return false;
			}

			final HybrisAsCacheKey that = (HybrisAsCacheKey) obj;
			return new EqualsBuilder().append(this.tenantId, that.tenantId).append(this.cacheKey, that.cacheKey).isEquals();
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(tenantId, cacheKey);
		}
	}
}
