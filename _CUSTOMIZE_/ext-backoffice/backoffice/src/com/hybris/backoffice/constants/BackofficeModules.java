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
package com.hybris.backoffice.constants;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.platform.util.Utilities;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Utilities class providing information about registered Backoffice modules and their configuration files.
 */
public final class BackofficeModules
{

	private static final PlatformConfig PLATFORM_CONFIG = ConfigUtil.getPlatformConfig(Utilities.class);

	private static final String SPRING_XML_SUFFIX = "spring.xml";
	private static final String WIDGETS_XML_SUFFIX = "widgets.xml";
	private static final String CONFIG_XML_SUFFIX = "config.xml";
	private static final String FILE_CONVENTION_SEPARATOR = "-";

	private BackofficeModules()
	{
		throw new AssertionError("Utilities class should not be instantiated");
	}

	/**
	 * Provides a list of extensions registered in system that defines backoffice nature
	 *
	 * @return list of extensions in build order
	 */
	public static List<ExtensionInfo> getBackofficeModules()
	{
		return PLATFORM_CONFIG.getExtensionInfosInBuildOrder().stream()
				.filter(ext -> Boolean.parseBoolean(ext.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
				.collect(Collectors.toList());
	}

	/**
	 * Provides information about an extension of specified name that defines backoffice nature
	 *
	 * @param moduleName
	 *           name of module
	 * @return information about the extension
	 */
	public static Optional<ExtensionInfo> getBackofficeModule(final String moduleName)
	{
		final ExtensionInfo extensionInfo = PLATFORM_CONFIG.getExtensionInfo(moduleName);
		if (extensionInfo != null && Boolean.parseBoolean(extensionInfo.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
		{
			return Optional.of(extensionInfo);
		}
		else
		{
			return Optional.empty();
		}
	}

	/**
	 * Gets the name of backoffice configuration file for specified module and of specified suffix (i.e.
	 * <code>modulename-backoffice-config.xml</code>)
	 *
	 * @param moduleName
	 *           name of module
	 * @param suffix
	 *           file suffix (i.e. <code>config.xml</code>
	 * @return full configuration file name
	 */
	public static String getModuleFileName(final String moduleName, final String suffix)
	{
		final StringBuilder fileName = new StringBuilder(moduleName).append(FILE_CONVENTION_SEPARATOR)
				.append(BackofficeConstants.EXTENSIONNAME);
		if (!suffix.startsWith(FILE_CONVENTION_SEPARATOR))
		{
			fileName.append(FILE_CONVENTION_SEPARATOR);
		}
		fileName.append(suffix);
		return fileName.toString();
	}

	/**
	 * Gets backoffice configuration file for specified extension and of specified suffix (i.e.
	 * <code>modulename-backoffice-config.xml</code>)
	 *
	 * @param extension
	 *           information about extension
	 * @param suffix
	 *           file suffix (i.e. <code>config.xml</code>
	 * @return configuration file
	 */
	public static File getModuleFile(final ExtensionInfo extension, final String suffix)
	{
		return new File(extension.getItemsXML().getParent(), getModuleFileName(extension.getName(), suffix));
	}

	/**
	 * Gets backoffice spring beans configuration file for specified extension
	 *
	 * @param extension
	 *           information about extension
	 * @return spring beans configuration file
	 */
	public static File getSpringDefinitionsFile(final ExtensionInfo extension)
	{
		return getModuleFile(extension, SPRING_XML_SUFFIX);
	}

	/**
	 * Gets backoffice UI configuration file for specified extension
	 *
	 * @param extension
	 *           information about extension
	 * @return backoffice UI configuration file
	 */
	public static File getConfigXmlFile(final ExtensionInfo extension)
	{
		return getModuleFile(extension, CONFIG_XML_SUFFIX);
	}

	/**
	 * Gets backoffice widgets meshup file for specified extension
	 *
	 * @param extension
	 *           information about extension
	 * @return widgets meshup file
	 */
	public static File getWidgetsXmlFile(final ExtensionInfo extension)
	{
		return getModuleFile(extension, WIDGETS_XML_SUFFIX);
	}
}

