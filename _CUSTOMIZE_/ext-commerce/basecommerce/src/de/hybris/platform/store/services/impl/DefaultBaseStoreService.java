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
package de.hybris.platform.store.services.impl;

import de.hybris.platform.basecommerce.strategies.BaseStoreSelectorStrategy;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.daos.BaseStoreDao;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of <code>BaseStoreService</code> service.
 */
public class DefaultBaseStoreService implements BaseStoreService
{
	private BaseStoreDao baseStoreDao;

	private List<BaseStoreSelectorStrategy> baseStoreSelectorStrategies;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BaseStoreModel> getAllBaseStores()
	{
		return baseStoreDao.findAllBaseStores();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseStoreModel getBaseStoreForUid(final String uid) throws AmbiguousIdentifierException, UnknownIdentifierException
	{
		final List<BaseStoreModel> result = baseStoreDao.findBaseStoresByUid(uid);
		if (result.isEmpty())
		{
			throw new UnknownIdentifierException("Base store with uid '" + uid + "' not found!");
		}
		else if (result.size() > 1)
		{
			throw new AmbiguousIdentifierException("Base store uid '" + uid + "' is not unique, " + result.size()
					+ " base stores found!");
		}
		return result.get(0);
	}

	@Override
	public BaseStoreModel getCurrentBaseStore()
	{
		BaseStoreModel result = null;
		if (!CollectionUtils.isEmpty(baseStoreSelectorStrategies))
		{
			for (final BaseStoreSelectorStrategy strategy : baseStoreSelectorStrategies)
			{
				result = strategy.getCurrentBaseStore();
				if (result != null)
				{
					break;
				}
			}
		}
		return result;
	}

	@Required
	public void setBaseStoreDao(final BaseStoreDao baseStoreDao)
	{
		this.baseStoreDao = baseStoreDao;
	}

	@Required
	public void setBaseStoreSelectorStrategies(final List<BaseStoreSelectorStrategy> baseStoreSelectorStrategies)
	{
		this.baseStoreSelectorStrategies = baseStoreSelectorStrategies;
	}

}
