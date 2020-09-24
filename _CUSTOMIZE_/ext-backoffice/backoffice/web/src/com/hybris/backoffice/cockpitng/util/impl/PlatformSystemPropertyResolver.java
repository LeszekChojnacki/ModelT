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
package com.hybris.backoffice.cockpitng.util.impl;

import de.hybris.platform.util.Config;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hybris.cockpitng.core.util.impl.DefaultPropertyResolverRegistry;


/**
 * Re-maps platform properties to cockpit properties, with an optional prefix. Example: There is a property
 * 'cockpitng.appTitle'. If you have no prefix defined, you can just specify it in your local.properties. If you have a
 * prefix defined, e.g. "myApp.", you must specify it like 'myApp.cockpitng.appTitle'.
 */
public class PlatformSystemPropertyResolver extends DefaultPropertyResolverRegistry
{
	private String prefix;

	@Override
	public void initProperties()
	{
		// do nothing
	}

	/**
	 * @deprecated since 6.5 - this method does nothing and should not be used
	 */
	@Deprecated
	@Override
	protected void initProperties(final String filename)
	{
		// do nothing
	}

	/**
	 * @deprecated since 6.5 - this method does nothing and should not be used
	 */
	@Deprecated
	@Override
	public void setProperties(final Map<String, String> properties)
	{
		// do nothing
	}

	@Override
	public String getProperty(final String key)
	{
		return Config.getParameter(getPrefix() + key);
	}

	@Override
	public boolean getBoolean(final String key)
	{
		return Config.getBoolean(getPrefix() + key, false);
	}

	@Override
	public boolean getBoolean(final String key, final boolean defaultValue)
	{
		return Config.getBoolean(getPrefix() + key, defaultValue);
	}

	@Override
	public int getInteger(final String key)
	{
		return Config.getInt(getPrefix() + key, 0);
	}

	@Override
	public double getDouble(final String key)
	{
		return Config.getDouble(getPrefix() + key, 0d);
	}

	@Override
	public Map<String, String> getProperties()
	{
		final String propsPrefix = getPrefix();
		if (StringUtils.isNotEmpty(propsPrefix))
		{
			final int prefixLength = propsPrefix.length();
			return Config.getParametersByPattern(propsPrefix).entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().substring(prefixLength), e -> e.getValue()));
		}
		return Config.getAllParameters();
	}

	protected String getPrefix()
	{
		return prefix == null ? StringUtils.EMPTY : prefix;
	}

	public void setPrefix(final String prefix)
	{
		this.prefix = prefix;
	}
}
