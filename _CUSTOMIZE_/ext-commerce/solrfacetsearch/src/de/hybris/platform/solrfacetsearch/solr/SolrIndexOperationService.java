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
package de.hybris.platform.solrfacetsearch.solr;

import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.SolrIndexOperationModel;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexOperationNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Date;


/**
 * Service for managing index operations.
 */
public interface SolrIndexOperationService
{
	/**
	 * finds an index operation by id.
	 *
	 * @param id
	 *           unique identifier
	 *
	 * @return the index operation
	 *
	 * @throws SolrIndexOperationNotFoundException
	 *            if the index operation cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexOperationModel getOperationForId(long id) throws SolrServiceException;

	/**
	 * Starts an index operation.
	 *
	 * @param index
	 *           the index
	 * @param id
	 *           the operation id
	 * @param operation
	 *           the type of operation
	 * @param external
	 *           indicates if the index operation is external
	 *
	 * @return newly created running index operation
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexOperationModel startOperation(SolrIndexModel index, long id, IndexOperation operation, boolean external)
			throws SolrServiceException;

	/**
	 * End the index operation with statuses SUCCESS or FAILED depending on the indexError.
	 *
	 * @param id
	 *           unique identifier
	 * @param indexError
	 *           true/false
	 *
	 * @return the finished index operation
	 *
	 * @throws SolrIndexOperationNotFoundException
	 *            if the index operation cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexOperationModel endOperation(long id, boolean indexError) throws SolrServiceException;

	/**
	 * Cancels the index operation.
	 *
	 * @param id
	 *           unique identifier
	 *
	 * @return newly canceled index operation
	 *
	 * @throws SolrIndexOperationNotFoundException
	 *            if the index operation cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexOperationModel cancelOperation(long id) throws SolrServiceException;

	/**
	 * Get end time of the last successful FULL or UPDATE indexing operation for the given index. If no operation was
	 * found, <code>new Date(0L)</code> is returned.
	 *
	 * @param index
	 *           the index
	 *
	 * @return time of the last operation
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	Date getLastIndexOperationTime(SolrIndexModel index) throws SolrServiceException;
}
