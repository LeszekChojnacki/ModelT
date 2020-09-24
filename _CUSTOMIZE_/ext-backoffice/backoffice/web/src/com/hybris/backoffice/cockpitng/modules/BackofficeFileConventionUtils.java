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
package com.hybris.backoffice.cockpitng.modules;

import de.hybris.bootstrap.config.ExtensionInfo;

import java.io.File;

import com.hybris.backoffice.constants.BackofficeModules;


/**
 * Utility methods and constants dealing with filename conventions in backoffice.
 */
public class BackofficeFileConventionUtils
{

	/**
	 * @deprecated since 1808, use {@link BackofficeModules#getSpringDefinitionsFile(ExtensionInfo)} instead
	 */
	@Deprecated
	public static final String SPRING_XML_SUFFIX = "-spring.xml";
	/**
	 * @deprecated since 1808, use {@link BackofficeModules#getWidgetsXmlFile(ExtensionInfo)} instead
	 */
	@Deprecated
	public static final String WIDGETS_XML_SUFFIX = "-widgets.xml";
	/**
	 * @deprecated since 1808, use {@link BackofficeModules#getConfigXmlFile(ExtensionInfo)} instead
	 */
	@Deprecated
	public static final String CONFIG_XML_SUFFIX = "-config.xml";
	/**
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	public static final String SEPARATOR = "-";

	/**
	 * @deprecated since 1808, responsibility moved to {@link com.hybris.cockpitng.core.spring.CockpitApplicationContext}
	 */
	@Deprecated
	public static final String EXTENSION_PROTOCOL_PREFIX = "extension://";

	private BackofficeFileConventionUtils()
	{
		throw new AssertionError("creating instances of this class is prohibited");
	}

	public static String buildConventionFileName(final String extensionName, final String suffix)
	{
		return BackofficeModules.getModuleFileName(extensionName, suffix);
	}

	public static String getModuleSpringDefinitionsFile(final String extensionName)
	{
		return BackofficeModules.getBackofficeModule(extensionName).map(BackofficeModules::getSpringDefinitionsFile)
				.map(File::getName).orElse(null);
	}

	public static String getModuleConfigXmlFile(final String extensionName)
	{
		return BackofficeModules.getBackofficeModule(extensionName).map(BackofficeModules::getConfigXmlFile).map(File::getName)
				.orElse(null);
	}

	public static String getModuleWidgetsXmlFile(final String extensionName)
	{
		return BackofficeModules.getBackofficeModule(extensionName).map(BackofficeModules::getWidgetsXmlFile).map(File::getName)
				.orElse(null);
	}

}
