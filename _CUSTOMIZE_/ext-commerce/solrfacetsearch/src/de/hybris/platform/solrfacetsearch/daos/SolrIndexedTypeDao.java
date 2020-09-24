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
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.List;


/**
 * The {@link SolrIndexedTypeModel} DAO.
 */
public interface SolrIndexedTypeDao extends GenericDao<SolrIndexedTypeModel>
{
	/**
	 * Returns all indexed types.
	 *
	 * @return the indexed types
	 */
	List<SolrIndexedTypeModel> findAllIndexedTypes();

	/**
	 * Finds an indexed type by identifier.
	 *
	 * @param identifier
	 *           - the indexed type identifier
	 *
	 * @return the indexed type
	 */
	SolrIndexedTypeModel findIndexedTypeByIdentifier(String identifier);
}
