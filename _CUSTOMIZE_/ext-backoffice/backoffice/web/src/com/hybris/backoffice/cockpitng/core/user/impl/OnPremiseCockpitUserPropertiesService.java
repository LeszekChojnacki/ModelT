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
package com.hybris.backoffice.cockpitng.core.user.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.user.CockpitUserPropertiesService;
import com.hybris.cockpitng.type.ObjectValueService;


/**
 * Provides cockpit user properties of an on-premise user.
 */
public class OnPremiseCockpitUserPropertiesService implements CockpitUserPropertiesService
{
	private static final Logger LOG = Logger.getLogger(OnPremiseCockpitUserPropertiesService.class);

	private UserService userService;
	private ObjectValueService objectValueService;

	private Map<String, String> propertyMap;


	@Override
	public Map<String, String> getUserProperties(final String userId)
	{
		if (MapUtils.isNotEmpty(this.propertyMap))
		{
			try
			{
				final UserModel user = this.userService.getUserForUID(userId);
				final Map<String, String> result = new HashMap<String, String>(this.propertyMap.size());
				for (final Map.Entry<String, String> entry : this.propertyMap.entrySet())
				{
					final String propertyName = entry.getKey();
					final String propertyExpression = entry.getValue();
					final Object value = this.objectValueService.getValue(propertyExpression, user);
					result.put(propertyName, valueToString(value));
				}
				return result;
			}
			catch (de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException e)
			{
				LOG.error("can not retrieve user properties for user '" + userId + "'", e);
			}
		}
		return Collections.<String, String> emptyMap();
	}

	/**
	 * Converts given value to its string representation.
	 *
	 * @param value
	 *           value to convert to string
	 * @return string representation of the given value
	 */
	protected String valueToString(final Object value)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof String)
		{
			return (String) value;
		}
		if (value instanceof Collection)
		{
			((Collection) value).toArray();
			return arrayValueToString(((Collection) value).toArray());
		}
		if (value.getClass().isArray())
		{
			return arrayValueToString((Object[]) value);
		}
		return ObjectUtils.toString(value);
	}

	private String arrayValueToString(final Object[] arrayValue)
	{
		if (arrayValue == null || arrayValue.length == 0)
		{
			return null;
		}
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < arrayValue.length; i++)
		{
			if (i > 0)
			{
				result.append(',');
			}
			result.append(valueToString(arrayValue[i]));
		}
		return result.toString();
	}

	public void setPropertyMap(final Map<String, String> propertyMap)
	{
		this.propertyMap = propertyMap;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setObjectValueService(final ObjectValueService objectValueService)
	{
		this.objectValueService = objectValueService;
	}
}
