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
package com.hybris.backoffice.cockpitng.util.labels;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.util.labels.ResourcesLabelLocator;


public class BackofficeModulesLabelLocatorInit
{
	private static final Logger LOG = Logger.getLogger(BackofficeModulesLabelLocatorInit.class);
	private final PlatformConfig platformConfig = ConfigUtil.getPlatformConfig(Utilities.class);
	private CockpitLocaleService localeService;
	private String location;
	private String name;

	/**
	 * Registers label locators for all backoffice module extensions. Should be specified as init-method if this will be
	 * registered as a spring bean.
	 */
	public void init()
	{
		for (final String extensionName : getAllBackofficeExtensionNames())
		{
			final String normalizedLocation = getNormalizedLocation(location);
			final String resourceName = StringUtils.replace(name, "{extName}", extensionName);
			final String resourcePath = normalizedLocation + '/' + resourceName;

			final ClassLoader classLoader = getClass().getClassLoader();
			if (classLoader != null)
			{
				final ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
				final boolean hasLocalizedProperties = Stream.of(Locale.getAvailableLocales())
						.map(locale -> control.getCandidateLocales(resourcePath, locale)).flatMap(Collection::stream)
						.map(locale -> control.toBundleName(resourcePath, locale))
						.map(bundleName -> control.toResourceName(bundleName, "properties")).distinct().map(classLoader::getResource)
						.anyMatch(Objects::nonNull);
				if (hasLocalizedProperties)
				{
					final ResourcesLabelLocator extensionLocator = createResourcesLabelLocator(resourceName);
					extensionLocator.init();
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Registering backoffice module label locator for extension '" + extensionName + "'");
					}
				}
			}
		}
	}


	/**
	 * Creates a {@link ResourcesLabelLocator} with the given resource name and the current location.
	 */
	protected ResourcesLabelLocator createResourcesLabelLocator(final String resourceName)
	{
		final ResourcesLabelLocator locator = new ResourcesLabelLocator();
		locator.setLocation(location);
		locator.setName(resourceName);
		return locator;
	}


	/**
	 * Returns all backoffice module extension names in build order.
	 */
	protected List<String> getAllBackofficeExtensionNames()
	{
		final List<String> ret = new ArrayList<>();
		for (final ExtensionInfo extensionInfo : platformConfig.getExtensionInfosInBuildOrder())
		{
			final String extensionName = extensionInfo.getName();
			final ExtensionInfo extensionInfoObject = Utilities.getPlatformConfig().getExtensionInfo(extensionName);

			if (extensionInfoObject != null
					&& Boolean.parseBoolean(extensionInfoObject.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
			{
				ret.add(extensionName);
			}
		}

		return ret;
	}


	/**
	 * Removes a possible leading or trailing '/'.
	 */
	protected String getNormalizedLocation(final String location)
	{
		String ret = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(location))
		{
			if (location.startsWith("/"))
			{
				ret = location.substring(1);
			}
			if (StringUtils.isNotBlank(ret) && ret.charAt(ret.length() - 1) == '/')
			{
				return ret.substring(0, ret.length() - 2);
			}
		}
		return ret;
	}

	/**
	 * Sets the location of the localization files in resources. It must point to the package which contain the files.
	 * Example: '/my/path/in/resources/labels'.
	 *
	 * @param location
	 *           location of the localization files within resources (a package)
	 */
	@Required
	public void setLocation(final String location)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("location", location);
		this.location = location;
	}

	/**
	 * Sets the prefix of the label files. Will be used to locate the labels files in the specified location (@see
	 * {@link #setLocation(String)}). For example: setting the name to 'labels' will look for files with naming scheme
	 * labels_<LOCALE_CODE>.properties where <LOCALE_CODE> is the code of the current locale. (The default fallback file
	 * will be labels.properties in this case.)
	 *
	 * @param name
	 *           prefix of the label files
	 */
	@Required
	public void setName(final String name)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("name", name);
		this.name = name;
	}

	protected CockpitLocaleService getLocaleService()
	{
		return localeService;
	}

	@Required
	public void setLocaleService(final CockpitLocaleService localeService)
	{
		this.localeService = localeService;
	}
}
