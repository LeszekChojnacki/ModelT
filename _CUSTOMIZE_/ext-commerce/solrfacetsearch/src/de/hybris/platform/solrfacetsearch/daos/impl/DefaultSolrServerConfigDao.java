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
import de.hybris.platform.solrfacetsearch.daos.SolrServerConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link SolrServerConfigDao}
 */
public class DefaultSolrServerConfigDao extends DefaultGenericDao<SolrServerConfigModel> implements SolrServerConfigDao
{
	/**
	 * Default constructor.
	 */
	public DefaultSolrServerConfigDao()
	{
		super(SolrServerConfigModel._TYPECODE);
	}

	@Override
	public List<SolrServerConfigModel> findAllSolrServerConfigs()
	{
		return find();
	}

	@Override
	public SolrServerConfigModel findSolrServerConfigByName(final String name)
	{
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(SolrServerConfigModel.NAME, name);

		final Collection<SolrServerConfigModel> solrServerConfigs = find(queryParams);

		ServicesUtil.validateIfSingleResult(solrServerConfigs, "Solr server config not found: " + queryParams.toString(),
				"More than one Solr server config found: " + queryParams.toString());

		return solrServerConfigs.iterator().next();
	}
}
