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
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;

import java.util.List;


/**
 * The {@link SolrServerConfigModel} DAO.
 */
public interface SolrServerConfigDao extends GenericDao<SolrServerConfigModel>
{
	/**
	 * Returns all Solr server configurations.
	 *
	 * @return the Solr server configurations.
	 */
	List<SolrServerConfigModel> findAllSolrServerConfigs();

	/**
	 * Finds a Solr server configuration by name.
	 *
	 * @param name
	 *           - the name
	 *
	 * @return the Solr server configuration
	 */
	SolrServerConfigModel findSolrServerConfigByName(final String name);
}
