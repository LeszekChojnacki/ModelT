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
import de.hybris.platform.solrfacetsearch.daos.SolrIndexedPropertyDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link SolrIndexedPropertyDao}.
 */
public class DefaultSolrIndexedPropertyDao extends DefaultGenericDao<SolrIndexedPropertyModel> implements SolrIndexedPropertyDao
{
	public DefaultSolrIndexedPropertyDao()
	{
		super(SolrIndexedPropertyModel._TYPECODE);
	}

	@Override
	public List<SolrIndexedPropertyModel> findIndexedPropertiesByIndexedType(final SolrIndexedTypeModel indexedType)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexedPropertyModel.SOLRINDEXEDTYPE, indexedType);

		return find(queryParams);
	}

	@Override
	public SolrIndexedPropertyModel findIndexedPropertyByName(final SolrIndexedTypeModel indexedType, final String name)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexedPropertyModel.SOLRINDEXEDTYPE, indexedType);
		queryParams.put(SolrIndexedPropertyModel.NAME, name);

		final List<SolrIndexedPropertyModel> indexedProperties = find(queryParams);

		ServicesUtil.validateIfSingleResult(indexedProperties, "Indexed property not found: " + name,
				"More than one indexed property found: " + name);

		return indexedProperties.iterator().next();
	}
}
