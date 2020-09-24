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
package com.hybris.backoffice.solrsearch.cache.impl;

import de.hybris.platform.cache.AbstractCacheUnit;
import de.hybris.platform.cache.Cache;
import de.hybris.platform.cache.InvalidationListener;
import de.hybris.platform.cache.InvalidationManager;
import de.hybris.platform.cache.InvalidationTarget;
import de.hybris.platform.cache.InvalidationTopic;
import de.hybris.platform.cache.RemoteInvalidationSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.solrsearch.cache.BackofficeFacetSearchConfigCache;
import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;


/**
 * Thread-safe, invalidating implementation of {@link BackofficeFacetSearchConfigCache}. <br>
 * Thread safety based on {@link ReentrantReadWriteLock}. <br>
 * Invalidation triggered by creation, modification and removal of items of types defined in
 * {@link #invalidatingTypecodes} collection.
 */
public class DefaultBackofficeFacetSearchConfigCache implements BackofficeFacetSearchConfigCache, InvalidationListener
{

	protected final Map<String, BackofficeIndexedTypeToSolrFacetSearchConfigModel> cache = new HashMap<>();
	protected final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
	protected Set<String> invalidatingTypecodes = new HashSet<>();

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeFacetSearchConfigCache.class);


	public void initialize()
	{
		final InvalidationTopic topic = InvalidationManager.getInstance().getInvalidationTopic(new String[]
		{ Cache.CACHEKEY_HJMP, Cache.CACHEKEY_ENTITY });
		topic.addInvalidationListener(this);
	}

	@Override
	public boolean containsSearchConfigForTypeCode(final String typeCode)
	{
		final Lock readLock = cacheLock.readLock();
		readLock.lock();
		try
		{
			return cache.containsKey(typeCode);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public BackofficeIndexedTypeToSolrFacetSearchConfigModel getSearchConfigForTypeCode(final String typeCode)
	{
		final Lock readLock = cacheLock.readLock();
		readLock.lock();
		try
		{
			return cache.get(typeCode);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public void putSearchConfigForTypeCode(final String typeCode,
			final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig)
	{
		final Lock writeLock = cacheLock.writeLock();
		writeLock.lock();
		try
		{
			cache.put(typeCode, searchConfig);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void invalidateCache()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Invalidating cache");
		}
		final Lock writeLock = cacheLock.writeLock();
		writeLock.lock();
		try
		{
			cache.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void setInvalidatingTypecodes(final Set<String> invalidatingTypecodes)
	{
		this.invalidatingTypecodes = invalidatingTypecodes;
	}

	@Override
	public void keyInvalidated(final Object[] key, final int invalidationType, final InvalidationTarget target,
			final RemoteInvalidationSource remoteSource)
	{
		if (target instanceof Cache && isOperationInvalidating(invalidationType) && isTypeInvalidating(key))
		{
			invalidateCache();
		}
	}

	protected boolean isOperationInvalidating(final int invalidationType)
	{
		return AbstractCacheUnit.INVALIDATIONTYPE_CREATED == invalidationType
				|| AbstractCacheUnit.INVALIDATIONTYPE_REMOVED == invalidationType
				|| AbstractCacheUnit.INVALIDATIONTYPE_MODIFIED == invalidationType;
	}

	protected boolean isTypeInvalidating(final Object[] key)
	{
		return invalidatingTypecodes.contains(key[2]);
	}

}
