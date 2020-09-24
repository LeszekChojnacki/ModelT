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
package de.hybris.platform.store.daos.impl;

import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.daos.BaseStoreDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of <code>BaseStoreDao</code> interface.
 * 
 */
public class DefaultBaseStoreDao extends AbstractItemDao implements BaseStoreDao
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BaseStoreModel> findBaseStoresByUid(final String uid)
	{
		final String query = "SELECT {" + BaseStoreModel.PK + "} FROM {" + BaseStoreModel._TYPECODE + "} WHERE {"
				+ BaseStoreModel.UID + "} =?" + BaseStoreModel.UID;
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(BaseStoreModel.UID, uid);
		fQuery.addQueryParameters(params);
		final SearchResult<BaseStoreModel> result = search(fQuery);
		return result.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BaseStoreModel> findAllBaseStores()
	{
		final String query = "SELECT {" + BaseStoreModel.PK + "} FROM {" + BaseStoreModel._TYPECODE + "}";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		final SearchResult<BaseStoreModel> result = search(fQuery);
		return result.getResult();
	}

}
