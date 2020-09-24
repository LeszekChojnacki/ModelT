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

import de.hybris.platform.regioncache.CacheValueLoadException;
import de.hybris.platform.regioncache.CacheValueLoader;
import de.hybris.platform.regioncache.key.CacheKey;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


public class DefaultFacetSearchConfigCacheValueLoader implements CacheValueLoader<FacetSearchConfig>
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSearchConfigCacheValueLoader.class);

	private SolrFacetSearchConfigDao solrFacetSearchConfigDao;
	private Converter<SolrFacetSearchConfigModel, FacetSearchConfig> solrFacetSearchConfigConverter;

	@Override
	public FacetSearchConfig load(final CacheKey key)
	{
		if (!(key instanceof FacetSearchConfigCacheKey))
		{
			throw new IllegalArgumentException("Key value should be instance of FacetSearchConfigCacheKey class");
		}

		final FacetSearchConfigCacheKey facetSearchConfigKey = (FacetSearchConfigCacheKey) key;

		try
		{

			final SolrFacetSearchConfigModel configModel = solrFacetSearchConfigDao
					.findFacetSearchConfigByName(facetSearchConfigKey.getName());

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Loading FacetSearchConfig for key : " + key);
			}

			return solrFacetSearchConfigConverter.convert(configModel);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new CacheValueLoadException("No such configuration: " + facetSearchConfigKey.getName(), e);
		}
	}

	public SolrFacetSearchConfigDao getSolrFacetSearchConfigDao()
	{
		return solrFacetSearchConfigDao;
	}

	@Required
	public void setSolrFacetSearchConfigDao(final SolrFacetSearchConfigDao solrFacetSearchConfigDao)
	{
		this.solrFacetSearchConfigDao = solrFacetSearchConfigDao;
	}

	public Converter<SolrFacetSearchConfigModel, FacetSearchConfig> getSolrFacetSearchConfigConverter()
	{
		return solrFacetSearchConfigConverter;
	}

	@Required
	public void setSolrFacetSearchConfigConverter(
			final Converter<SolrFacetSearchConfigModel, FacetSearchConfig> solrFacetSearchConfigConverter)
	{
		this.solrFacetSearchConfigConverter = solrFacetSearchConfigConverter;
	}
}
