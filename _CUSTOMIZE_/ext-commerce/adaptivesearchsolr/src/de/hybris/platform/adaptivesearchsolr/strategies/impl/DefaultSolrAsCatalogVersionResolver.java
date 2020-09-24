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
package de.hybris.platform.adaptivesearchsolr.strategies.impl;

import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsCatalogVersionResolver;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;


public class DefaultSolrAsCatalogVersionResolver implements SolrAsCatalogVersionResolver
{
	@Override
	public List<CatalogVersionModel> resolveCatalogVersions(final SearchQuery searchQuery)
	{
		if (CollectionUtils.isEmpty(searchQuery.getCatalogVersions()))
		{
			return Collections.emptyList();
		}

		return searchQuery.getCatalogVersions().stream().filter(this::isSupportedCatalogVersion).collect(Collectors.toList());
	}

	protected boolean isSupportedCatalogVersion(final CatalogVersionModel catalogVersion)
	{
		return catalogVersion != null && !(catalogVersion instanceof ClassificationSystemVersionModel);
	}
}
