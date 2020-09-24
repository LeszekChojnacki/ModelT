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
import de.hybris.platform.solrfacetsearch.daos.SolrIndexDao;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link SolrIndexDao}.
 */
public class DefaultSolrIndexDao extends DefaultGenericDao<SolrIndexModel> implements SolrIndexDao
{
	/**
	 * Default constructor.
	 */
	public DefaultSolrIndexDao()
	{
		super(SolrIndexModel._TYPECODE);
	}

	@Override
	public List<SolrIndexModel> findAllIndexes()
	{
		return find();
	}

	@Override
	public SolrIndexModel findIndexByConfigAndTypeAndQualifier(final SolrFacetSearchConfigModel facetSearchConfig,
			final SolrIndexedTypeModel indexedType, final String qualifier)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexModel.FACETSEARCHCONFIG, facetSearchConfig);
		queryParams.put(SolrIndexModel.INDEXEDTYPE, indexedType);
		queryParams.put(SolrIndexModel.QUALIFIER, qualifier);

		final Collection<SolrIndexModel> indexes = find(queryParams);

		ServicesUtil.validateIfSingleResult(indexes, "Index not found: " + queryParams.toString(),
				"More than one index was found: " + queryParams.toString());

		return indexes.iterator().next();
	}

	@Override
	public SolrIndexModel findActiveIndexByConfigAndType(final SolrFacetSearchConfigModel facetSearchConfig,
			final SolrIndexedTypeModel indexedType)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexModel.FACETSEARCHCONFIG, facetSearchConfig);
		queryParams.put(SolrIndexModel.INDEXEDTYPE, indexedType);
		queryParams.put(SolrIndexModel.ACTIVE, true);

		final Collection<SolrIndexModel> indexes = find(queryParams);

		ServicesUtil.validateIfSingleResult(indexes, "Active index not found: " + queryParams.toString(),
				"More than one active index found: " + queryParams.toString());

		return indexes.iterator().next();
	}

	@Override
	public List<SolrIndexModel> findIndexesByConfigAndType(final SolrFacetSearchConfigModel facetSearchConfig,
			final SolrIndexedTypeModel indexedType)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrIndexModel.FACETSEARCHCONFIG, facetSearchConfig);
		queryParams.put(SolrIndexModel.INDEXEDTYPE, indexedType);

		return find(queryParams);
	}
}
