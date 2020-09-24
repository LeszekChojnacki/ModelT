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

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexOperationDao;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationStatus;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.SolrIndexOperationModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Default implementation of {@link SolrIndexOperationDao}.
 */
public class DefaultSolrIndexOperationDao extends DefaultGenericDao<SolrIndexOperationModel> implements SolrIndexOperationDao
{
	protected static final String OPERATIONS_PARAM = "operations";

	protected static final String LAST_SUCCESSFUL_OPERATION_QUERY = "SELECT {pk} FROM {" + SolrIndexOperationModel._TYPECODE
			+ "} WHERE {" + SolrIndexOperationModel.INDEX + "} = ?index AND {" + SolrIndexOperationModel.OPERATION
			+ "} IN (?operations) AND {" + SolrIndexOperationModel.STATUS + "} = ?status AND {" + SolrIndexOperationModel.EXTERNAL
			+ "} = ?external ORDER BY {" + SolrIndexOperationModel.STARTTIME + "} DESC";

	/**
	 * Default constructor.
	 */
	public DefaultSolrIndexOperationDao()
	{
		super(SolrIndexOperationModel._TYPECODE);
	}

	@Override
	public SolrIndexOperationModel findIndexOperationById(final long id)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexOperationModel.ID, id);

		final Collection<SolrIndexOperationModel> operations = find(queryParams);

		ServicesUtil.validateIfSingleResult(operations, "operation not found: " + queryParams.toString(),
				"more than one operation was found: " + queryParams.toString());

		return operations.iterator().next();
	}

	@Override
	public Optional<SolrIndexOperationModel> findLastSuccesfulIndexOperation(final SolrIndexModel index)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexOperationModel.INDEX, index);
		queryParams.put(OPERATIONS_PARAM, Arrays.asList(IndexerOperationValues.FULL, IndexerOperationValues.UPDATE));
		queryParams.put(SolrIndexOperationModel.STATUS, IndexerOperationStatus.SUCCESS);
		queryParams.put(SolrIndexOperationModel.EXTERNAL, false);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(LAST_SUCCESSFUL_OPERATION_QUERY, queryParams);
		searchQuery.setCount(1);
		searchQuery.setNeedTotal(false);

		final List<SolrIndexOperationModel> operations = getFlexibleSearchService().<SolrIndexOperationModel> search(searchQuery)
				.getResult();

		if (operations.isEmpty())
		{
			return Optional.empty();
		}
		else
		{
			return Optional.of(operations.get(0));
		}
	}
}
