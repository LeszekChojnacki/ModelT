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
package de.hybris.platform.solrfacetsearch.daos;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;

import java.util.List;


/**
 * The {@link SolrFacetSearchConfigModel} DAO.
 */
public interface SolrFacetSearchConfigDao extends GenericDao<SolrFacetSearchConfigModel>
{
	/**
	 * Returns all facet search configurations.
	 *
	 * @return the facet search configurations
	 */
	List<SolrFacetSearchConfigModel> findAllFacetSearchConfigs();

	/**
	 * Finds {@link SolrFacetSearchConfigModel} by name.
	 *
	 * @param name
	 *           - the facet search configuration name
	 *
	 * @return the facet search configuration
	 */
	SolrFacetSearchConfigModel findFacetSearchConfigByName(final String name);

	/**
	 * Finds facet search configurations having a relation with the given catalog version.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 *
	 * @return the facet search configurations
	 */
	List<SolrFacetSearchConfigModel> findFacetSearchConfigsByCatalogVersion(final CatalogVersionModel catalogVersion);
}
