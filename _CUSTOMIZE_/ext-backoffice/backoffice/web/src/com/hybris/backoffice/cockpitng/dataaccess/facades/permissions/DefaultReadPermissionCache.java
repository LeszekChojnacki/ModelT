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
package com.hybris.backoffice.cockpitng.dataaccess.facades.permissions;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultReadPermissionCache implements ReadPermissionCache
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultReadPermissionCache.class);
	private static final String COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_MESSAGE = "Could not find attribute descriptor for given: ";
	private static final String COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_FORMAT = "%s.%s";
	private final Map<String, Boolean> typeReadPermissionMap;
	private final Map<String, Map<String, Boolean>> typeAttributeReadPermissionMap;
	private final PermissionCRUDService permissionCRUDService;

	public DefaultReadPermissionCache(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
		this.typeReadPermissionMap = new ConcurrentHashMap<>();
		this.typeAttributeReadPermissionMap = new ConcurrentHashMap<>();
	}

	@Override
	public boolean canReadType(final String typeCode)
	{
		try
		{
			synchronized (typeReadPermissionMap)
			{
				return typeReadPermissionMap.computeIfAbsent(typeCode, getPermissionCRUDService()::canReadType);
			}
		}
		catch (final UnknownIdentifierException uie)
		{
			LOG.warn(uie.getMessage(), uie);
		}
		return false;
	}

	@Override
	public boolean canReadAttribute(final String typeCode, final String attribute)
	{
		try
		{
			synchronized (typeAttributeReadPermissionMap)
			{
				final Map<String, Boolean> attributeReadPermissionMap = typeAttributeReadPermissionMap.computeIfAbsent(typeCode,
						key -> new HashMap<>());

				return attributeReadPermissionMap.computeIfAbsent(attribute,
						key -> permissionCRUDService.canReadAttribute(typeCode, key));
			}
		}
		catch (final UnknownIdentifierException uie)
		{
			LOG.debug(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_MESSAGE
					+ String.format(COULD_NOT_FIND_ATTRIBUTE_EXCEPTION_FORMAT, typeCode, attribute), uie);
			return true;
		}
	}

	protected PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}
}
