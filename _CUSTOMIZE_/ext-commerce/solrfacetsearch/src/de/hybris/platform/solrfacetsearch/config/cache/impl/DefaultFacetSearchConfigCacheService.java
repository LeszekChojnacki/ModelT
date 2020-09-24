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

import de.hybris.platform.cache.Cache;
import de.hybris.platform.cache.InvalidationListener;
import de.hybris.platform.cache.InvalidationManager;
import de.hybris.platform.cache.InvalidationTarget;
import de.hybris.platform.cache.InvalidationTopic;
import de.hybris.platform.cache.RemoteInvalidationSource;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.regioncache.CacheController;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.cache.FacetSearchConfigCacheService;
import de.hybris.platform.solrfacetsearch.config.cache.FacetSearchConfigInvalidationTypeSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of service for managing FacetSearchConfig cache. It allows adding and invalidating objects
 * from cache and also have listener which invalidate cache based on information about model object changes.
 */
public class DefaultFacetSearchConfigCacheService implements FacetSearchConfigCacheService, ApplicationContextAware
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSearchConfigCacheService.class);
	private Set<String> invalidationTypes;
	private CacheController cacheController;
	private FacetSearchConfigCacheRegion facetSearchConfigCacheRegion;
	private DefaultFacetSearchConfigCacheValueLoader facetSearchConfigCacheValueLoader;
	private CommonI18NService commonI18NService;
	private String tenantId;
	private ApplicationContext applicationContext;

	private final InvalidationListener invalidationListener = new InvalidationListener()
	{
		@Override
		public void keyInvalidated(final Object[] key, final int invalidationType, final InvalidationTarget target,
				final RemoteInvalidationSource remoteSrc)
		{
			if (invalidationTypes.contains(key[2]))
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("SolrfacetSearchCache.clearCache : invalidationType=" + invalidationType);
				}

				cacheController.clearCache(facetSearchConfigCacheRegion);
			}
		}
	};

	/**
	 * Initiation method for the bean
	 */
	@PostConstruct
	public void init()
	{
		tenantId = Registry.getCurrentTenantNoFallback().getTenantID();
		final InvalidationTopic topic = InvalidationManager.getInstance().getInvalidationTopic(new String[]
		{ Cache.CACHEKEY_HJMP, Cache.CACHEKEY_ENTITY });
		topic.addInvalidationListener(invalidationListener);
		createInvalidationTypeSet();
	}

	protected void createInvalidationTypeSet()
	{
		invalidationTypes = new HashSet<>();
		final Map<String, FacetSearchConfigInvalidationTypeSet> beanMap = applicationContext
				.getBeansOfType(FacetSearchConfigInvalidationTypeSet.class);
		for (final FacetSearchConfigInvalidationTypeSet invalidationTypesBean : beanMap.values())
		{
			invalidationTypes.addAll(invalidationTypesBean.getInvalidationTypes());
		}
	}

	@Override
	public FacetSearchConfig putOrGetFromCache(final String configName)
	{
		return cacheController.getWithLoader(createCacheKey(configName), facetSearchConfigCacheValueLoader);
	}

	@Override
	public void invalidate(final String name)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Invalidating facet search config cache for : " + name);
		}

		final List<CacheKey> keyList = facetSearchConfigCacheRegion.findCachedObjectKeys(name, tenantId);
		for (final CacheKey key : keyList)
		{
			cacheController.invalidate(key);
		}
	}

	protected FacetSearchConfigCacheKey createCacheKey(final String configName)
	{
		final FacetSearchConfigCacheKey key = new FacetSearchConfigCacheKey(configName, getLanguage(), tenantId);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Created key :" + key);
		}
		return key;
	}

	protected String getLanguage()
	{
		final LanguageModel language = commonI18NService.getCurrentLanguage();
		return language == null ? null : language.getIsocode();
	}

	@Required
	public void setCacheController(final CacheController cacheController)
	{
		this.cacheController = cacheController;
	}

	public DefaultFacetSearchConfigCacheValueLoader getFacetSearchConfigCacheValueLoader()
	{
		return facetSearchConfigCacheValueLoader;
	}

	@Required
	public void setFacetSearchConfigCacheValueLoader(
			final DefaultFacetSearchConfigCacheValueLoader facetSearchConfigCacheValueLoader)
	{
		this.facetSearchConfigCacheValueLoader = facetSearchConfigCacheValueLoader;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public FacetSearchConfigCacheRegion getFacetSearchConfigCacheRegion()
	{
		return facetSearchConfigCacheRegion;
	}

	@Required
	public void setFacetSearchConfigCacheRegion(final FacetSearchConfigCacheRegion facetSearchConfigCacheRegion)
	{
		this.facetSearchConfigCacheRegion = facetSearchConfigCacheRegion;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}
}
