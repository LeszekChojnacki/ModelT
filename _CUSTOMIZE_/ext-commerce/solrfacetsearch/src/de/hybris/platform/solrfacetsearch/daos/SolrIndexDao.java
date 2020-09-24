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

import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.List;


/**
 * The {@link SolrIndexModel} DAO.
 */
public interface SolrIndexDao extends GenericDao<SolrIndexModel>
{
	/**
	 * Returns all indexes.
	 *
	 * @return the indexes
	 */
	List<SolrIndexModel> findAllIndexes();

	/**
	 * Finds indexes by facet search configuration and indexed type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @return the indexes
	 */
	List<SolrIndexModel> findIndexesByConfigAndType(SolrFacetSearchConfigModel facetSearchConfig,
			SolrIndexedTypeModel indexedType);

	/**
	 * Finds an index by facet search configuration, indexed type and qualifier.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the indexes
	 */
	SolrIndexModel findIndexByConfigAndTypeAndQualifier(SolrFacetSearchConfigModel facetSearchConfig,
			SolrIndexedTypeModel indexedType, String qualifier);

	/**
	 * Finds the active index for a specific facet search configuration and indexed type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @return the active index
	 */
	SolrIndexModel findActiveIndexByConfigAndType(SolrFacetSearchConfigModel facetSearchConfig, SolrIndexedTypeModel indexedType);
}
