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
package de.hybris.platform.solrfacetsearch.search.impl.populators;


import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Required;


public class FacetSearchQueryCatalogVersionsFilterPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	public static final String CATALOG_ID_FIELD = "catalogId";
	public static final String CATALOG_VERSION_FIELD = "catalogVersion";

	private CatalogTypeService catalogTypeService;

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final IndexedType indexedType = searchQuery.getIndexedType();
		final ComposedTypeModel composedType = indexedType.getComposedType();

		if (catalogTypeService.isCatalogVersionAwareType(composedType)
				&& CollectionUtils.isNotEmpty(searchQuery.getCatalogVersions()))
		{
			final StringBuilder rawQuery = new StringBuilder();
			boolean isFirst = true;

			for (final CatalogVersionModel catalogVersion : searchQuery.getCatalogVersions())
			{
				final CatalogModel catalog = catalogVersion.getCatalog();

				if (isFirst)
				{
					isFirst = false;
				}
				else
				{
					rawQuery.append(" OR ");
				}

				rawQuery.append('(');
				rawQuery.append(CATALOG_ID_FIELD).append(":\"").append(ClientUtils.escapeQueryChars(catalog.getId())).append('\"');
				rawQuery.append(" AND ");
				rawQuery.append(CATALOG_VERSION_FIELD).append(":\"").append(ClientUtils.escapeQueryChars(catalogVersion.getVersion()))
						.append('\"');
				rawQuery.append(')');
			}

			target.addFilterQuery(rawQuery.toString());
		}
	}

	public CatalogTypeService getCatalogTypeService()
	{
		return catalogTypeService;
	}

	@Required
	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}
}
