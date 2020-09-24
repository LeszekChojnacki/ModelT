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
package com.hybris.backoffice.cockpitng.dataaccess.facades.common;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


public class PlatformFacadeStrategyHandleCache
{
	public static final CacheEntry NOT_HANDLED_TYPE = new CacheEntry(false);

	private static final Logger LOG = Logger.getLogger(PlatformFacadeStrategyHandleCache.class);

	private final Map<String, CacheEntry> handleCache = new ConcurrentHashMap<>();
	private static final String TYPE_NOT_FOUND_EXCEPTION_MESSAGE = "Type not found in platform: ";
	private TypeService typeService;

	public synchronized boolean canHandle(final String typeCode)
	{
		if (typeCode == null)
		{
			return false;
		}

		final CacheEntry canHandleCached = handleCache.get(typeCode);
		if (canHandleCached == null)
		{
			return canHandleNonCachedTypeCode(typeCode);
		}
		else if (NOT_HANDLED_TYPE == canHandleCached)
		{
			return false;
		}
		else if (canHandleCached.isViewType())
		{
			return canHandleCachedViewType(typeCode);
		}
		return true;
	}

	private boolean canHandleNonCachedTypeCode(final String typeCode)
	{
		boolean canHandle = false;
		boolean viewType = false;

		final Class<?> classFromTypeCode = getClassFromTypeCode(typeCode);
		if (classFromTypeCode != null)
		{
			canHandle = isAssignableFromItemModelOrHybrisEnumValue(classFromTypeCode);
		}

		if (!canHandle)
		{
			canHandle = canGetTypeModelFromTypeCode(typeCode);
			if (canHandle)
			{
				viewType = typeService.isAssignableFrom(typeCode, ViewTypeModel._TYPECODE);
			}
		}
		addToCache(typeCode, canHandle, viewType);
		return canHandle;
	}

	private void addToCache(final String typeCode, final boolean canHandle, final boolean viewType)
	{
		if (canHandle)
		{
			handleCache.put(typeCode, new CacheEntry(viewType));
		}
		else
		{
			handleCache.put(typeCode, NOT_HANDLED_TYPE);
		}
	}

	private Class<?> getClassFromTypeCode(final String typeCode)
	{
		try
		{
			return Class.forName(typeCode, true, getClass().getClassLoader());
		}
		catch (final ClassNotFoundException exception)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(TYPE_NOT_FOUND_EXCEPTION_MESSAGE + typeCode, exception);
			}
		}
		return null;
	}

	private boolean isAssignableFromItemModelOrHybrisEnumValue(final Class<?> classFromTypeCode)
	{
		return ItemModel.class.isAssignableFrom(classFromTypeCode) || HybrisEnumValue.class.isAssignableFrom(classFromTypeCode);
	}

	private boolean canGetTypeModelFromTypeCode(final String typeCode)
	{
		try
		{
			final TypeModel type = getTypeService().getTypeForCode(typeCode);
			return type != null;
		}
		catch (final UnknownIdentifierException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(TYPE_NOT_FOUND_EXCEPTION_MESSAGE + typeCode);
			}
		}
		return false;
	}

	private boolean canHandleCachedViewType(final String typeCode)
	{
		try
		{
			getTypeService().getTypeForCode(typeCode);
		}
		catch (final UnknownIdentifierException exception)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(TYPE_NOT_FOUND_EXCEPTION_MESSAGE + typeCode);
			}
			handleCache.remove(typeCode);
			return false;
		}
		return true;
	}

	public synchronized void invalidate()
	{
		handleCache.clear();
	}

	protected Map<String, CacheEntry> getHandleCache()
	{
		return Collections.unmodifiableMap(handleCache);
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected static class CacheEntry
	{

		private boolean viewType = false;

		public CacheEntry(final boolean viewType)
		{
			this.viewType = viewType;
		}

		public boolean isViewType()
		{
			return viewType;
		}
	}
}
