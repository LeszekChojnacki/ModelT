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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.regioncache.CacheValueLoadException;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.cache.FacetSearchConfigCacheService;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import ma.glasnost.orika.impl.ConfigurableMapper;


/**
 * Default implementation of {@link FacetSearchConfigService} which uses cache
 */
public class DefaultFacetSearchConfigService implements FacetSearchConfigService
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSearchConfigService.class);

	private FacetSearchConfigCacheService facetSearchConfigCacheService;
	private ConfigurableMapper facetSearchConfigMapper;

	@Override
	public FacetSearchConfig getConfiguration(final String name) throws FacetConfigServiceException
	{
		try
		{
			final FacetSearchConfig facetSearchConfig = facetSearchConfigCacheService.putOrGetFromCache(name);
			final FacetSearchConfig copyOfFacetSearchConfig = facetSearchConfigMapper
					.map(facetSearchConfig, FacetSearchConfig.class);
			return copyOfFacetSearchConfig;
		}
		catch (final CacheValueLoadException e)
		{
			throw new FacetConfigServiceException(name, "Get configuration error", e);
		}
	}

	@Override
	public FacetSearchConfig getConfiguration(final CatalogVersionModel catalogVersion) throws FacetConfigServiceException
	{
		final List<SolrFacetSearchConfigModel> configs = catalogVersion.getFacetSearchConfigs();
		if (configs.isEmpty())
		{
			LOG.warn("no solr configuration can be found for catalog version [" + catalogVersion.getCatalog().getId()
					+ catalogVersion.getVersion() + "]");
			return null;
		}
		else if (configs.size() > 1)
		{
			LOG.warn("more than one solr configuration can be found for catalog version [" + catalogVersion.getCatalog().getId()
					+ catalogVersion.getVersion() + "], and the first one [" + configs.get(0).getName() + "] is taken.");
		}
		return getConfiguration(configs.get(0).getName());
	}

	@Override
	public IndexedType resolveIndexedType(final FacetSearchConfig facetSearchConfig, final String indexedTypeName)
			throws FacetConfigServiceException
	{
		IndexedType indexedType = null;

		final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
		if (indexConfig.getIndexedTypes() != null)
		{
			indexedType = indexConfig.getIndexedTypes().get(indexedTypeName);
		}

		if (indexedType == null)
		{
			throw new FacetConfigServiceException("Indexed type \"" + indexedTypeName + "\" not found");
		}

		return indexedType;
	}

	@Override
	public List<IndexedProperty> resolveIndexedProperties(final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType, final Collection<String> indexedPropertiesIds) throws FacetConfigServiceException
	{
		final List<IndexedProperty> indexedProperties = new ArrayList<IndexedProperty>();

		for (final String indexedPropertyId : indexedPropertiesIds)
		{
			IndexedProperty indexedProperty = null;

			if (indexedType.getIndexedProperties() != null)
			{
				indexedProperty = indexedType.getIndexedProperties().get(indexedPropertyId);
			}

			if (indexedProperty == null)
			{
				throw new FacetConfigServiceException("Indexed property \"" + indexedPropertyId + "\" not found");
			}

			indexedProperties.add(indexedProperty);
		}

		return indexedProperties;
	}

	public FacetSearchConfigCacheService getFacetSearchConfigCacheService()
	{
		return facetSearchConfigCacheService;
	}

	@Required
	public void setFacetSearchConfigCacheService(final FacetSearchConfigCacheService facetSearchConfigCacheService)
	{
		this.facetSearchConfigCacheService = facetSearchConfigCacheService;
	}

	@Required
	public void setFacetSearchConfigMapper(final ConfigurableMapper facetSearchConfigMapper)
	{
		this.facetSearchConfigMapper = facetSearchConfigMapper;
	}
}
