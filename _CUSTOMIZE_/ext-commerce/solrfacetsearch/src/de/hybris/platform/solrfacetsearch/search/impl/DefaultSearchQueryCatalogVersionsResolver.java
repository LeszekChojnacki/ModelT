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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQueryCatalogVersionsResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the interface {@Link SearchQueryCatalogVersionsResolver}
 */
public class DefaultSearchQueryCatalogVersionsResolver implements SearchQueryCatalogVersionsResolver
{
	private CatalogVersionService catalogVersionService;

	@Override
	public List<CatalogVersionModel> resolveCatalogVersions(final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType)
	{
		ServicesUtil.validateParameterNotNull(facetSearchConfig, "FacetSearchConfig cannot be null");
		final Collection<CatalogVersionModel> configuredCatalogVersions = facetSearchConfig.getIndexConfig().getCatalogVersions();
		final List<CatalogVersionModel> result = new ArrayList<CatalogVersionModel>();

		if (configuredCatalogVersions != null && !configuredCatalogVersions.isEmpty())
		{
			for (final CatalogVersionModel catalogVersion : catalogVersionService.getSessionCatalogVersions())
			{
				if (configuredCatalogVersions.contains(catalogVersion))
				{
					result.add(catalogVersion);
				}
			}
		}
		return result;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}
}
