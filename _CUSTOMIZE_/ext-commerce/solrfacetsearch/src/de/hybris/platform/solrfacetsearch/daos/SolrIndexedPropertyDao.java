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
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.List;


/**
 * The {@link SolrIndexedPropertyModel} DAO.
 */
public interface SolrIndexedPropertyDao extends GenericDao<SolrIndexedPropertyModel>
{
	/**
	 * Returns all indexed properties for specific indexed type.
	 *
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @return the indexed properties
	 */
	List<SolrIndexedPropertyModel> findIndexedPropertiesByIndexedType(SolrIndexedTypeModel indexedType);

	/**
	 * Finds an indexed property by name.
	 *
	 * @param indexedType
	 *           - the indexed type
	 * @param name
	 *           - the name
	 *
	 * @return the indexed property
	 */
	SolrIndexedPropertyModel findIndexedPropertyByName(SolrIndexedTypeModel indexedType, String name);
}
