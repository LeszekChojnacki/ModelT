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
package de.hybris.platform.solrfacetsearch.common;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.util.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ConfigurationUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

	protected static final String MESSAGE_TEMPLATE = "Existing '{}.{}'[{}] value will be overridden by configuration property '{}'[{}]";

	private ConfigurationUtils()
	{
		// utility class
	}

	public static Object getObject(final ItemModel item, final String property, final String template, final Object... args)
	{
		final String key = String.format(template, args);
		final String value = Config.getString(key, null);

		if (value != null)
		{
			LOG.debug(MESSAGE_TEMPLATE, item.getItemtype(), property, item.getProperty(property), key, value);
			return value;
		}

		return item.getProperty(property);
	}

	public static String getString(final ItemModel item, final String property, final String template, final Object... args)
	{
		final String key = String.format(template, args);
		final String value = Config.getString(key, null);

		if (value != null)
		{
			LOG.debug(MESSAGE_TEMPLATE, item.getItemtype(), property, item.getProperty(property), key, value);
			return value;
		}

		return item.getProperty(property);
	}

	public static Integer getInteger(final ItemModel item, final String property, final String template, final Object... args)
	{
		final String key = String.format(template, args);
		final String value = Config.getString(key, null);

		if (value != null)
		{
			LOG.debug(MESSAGE_TEMPLATE, item.getItemtype(), property, item.getProperty(property), key, value);
			return Integer.valueOf(value);
		}

		return item.getProperty(property);
	}
}
