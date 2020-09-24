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
import de.hybris.platform.solrfacetsearch.model.SolrIndexOperationModel;

import java.util.Optional;


/**
 * The {@link SolrIndexOperationModel} DAO.
 */
public interface SolrIndexOperationDao extends GenericDao<SolrIndexOperationModel>
{
	/**
	 * Finds the index operation by id.
	 *
	 * @param id
	 *           - the index operation id
	 *
	 * @return the index operation
	 */
	SolrIndexOperationModel findIndexOperationById(long id);

	/**
	 * Finds the last successfully finished FULL or UPDATE index operation for a given index.
	 *
	 * @param index
	 *           - the index
	 *
	 * @return Optional {@link SolrIndexOperationModel}
	 */
	Optional<SolrIndexOperationModel> findLastSuccesfulIndexOperation(SolrIndexModel index);
}
