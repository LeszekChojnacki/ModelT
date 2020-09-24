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
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexedTypeDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link SolrIndexedTypeDao}.
 */
public class DefaultSolrIndexedTypeDao extends DefaultGenericDao<SolrIndexedTypeModel> implements SolrIndexedTypeDao
{

	public DefaultSolrIndexedTypeDao()
	{
		super(SolrIndexedTypeModel._TYPECODE);
	}

	@Override
	public List<SolrIndexedTypeModel> findAllIndexedTypes()
	{
		return find();
	}

	@Override
	public SolrIndexedTypeModel findIndexedTypeByIdentifier(final String identifier)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexedTypeModel.IDENTIFIER, identifier);

		final List<SolrIndexedTypeModel> indexedTypes = find(queryParams);

		ServicesUtil.validateIfSingleResult(indexedTypes, "Indexed type not found: " + queryParams,
				"More than one indexed type found: " + queryParams);

		return indexedTypes.iterator().next();
	}
}
