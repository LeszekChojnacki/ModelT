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
package de.hybris.platform.solrfacetsearch.daos.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link SolrFacetSearchConfigDao}.
 */
public class DefaultSolrFacetSearchConfigDao extends DefaultGenericDao<SolrFacetSearchConfigModel>
		implements SolrFacetSearchConfigDao
{
	protected static final String CATALOG_VERSION_PARAM = "catalogVersion";

	protected static final String FIND_BY_CATALOG_VERSION_QUERY = "SELECT {f.PK} " + //
			"FROM {" + SolrFacetSearchConfigModel._TYPECODE + " AS f JOIN "
			+ CatalogVersionModel._SOLRFACETSEARCHCONFIG2CATALOGVERSIONRELATION + " as rel ON {f.PK} = {rel.source} } " + //
			"WHERE {rel.target}=?catalogVersion ";

	/**
	 * Default constructor.
	 */
	public DefaultSolrFacetSearchConfigDao()
	{
		super(SolrFacetSearchConfigModel._TYPECODE);
	}

	@Override
	public List<SolrFacetSearchConfigModel> findAllFacetSearchConfigs()
	{
		return find();
	}

	@Override
	public SolrFacetSearchConfigModel findFacetSearchConfigByName(final String name)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrFacetSearchConfigModel.NAME, name);

		final Collection<SolrFacetSearchConfigModel> configurations = find(queryParams);

		ServicesUtil.validateIfSingleResult(configurations, "Solr facet search configuration not found: " + queryParams.toString(),
				"More than one Solr facet search configuration found: " + queryParams.toString());

		return configurations.iterator().next();
	}

	@Override
	public List<SolrFacetSearchConfigModel> findFacetSearchConfigsByCatalogVersion(final CatalogVersionModel catalogVersion)
	{
		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(FIND_BY_CATALOG_VERSION_QUERY);
		searchQuery.addQueryParameter(CATALOG_VERSION_PARAM, catalogVersion);

		final SearchResult<SolrFacetSearchConfigModel> searchResult = getFlexibleSearchService().search(searchQuery);

		return searchResult.getResult();
	}
}
