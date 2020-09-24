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
package com.hybris.backoffice;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.platform.util.Utilities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.backoffice.constants.BackofficeModules;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.modules.ModulesEnumeration;
import com.hybris.cockpitng.modules.server.ws.jaxb.CockpitModuleInfo;


/**
 * Platform extensions with backoffice nature exposed as cockpit modules enumeration
 */
public class BackofficeModulesEnumeration implements ModulesEnumeration
{

	private static final PlatformConfig PLATFORM_CONFIG = ConfigUtil.getPlatformConfig(Utilities.class);

	private static final String URI_SCHEME_EXTENSION = "extension";

	private static final String FILE_POSTFIX_BOF = "_bof.jar";

	private static final String DEFAULT_MODULE_ICON = "/cng/img/MMC.png";

	private Iterator<CockpitModuleInfo> modules;

	@Override
	public boolean hasMoreElements()
	{
		return getModulesIterator().hasNext();
	}

	@Override
	public ModuleInfo nextElement()
	{
		return getModulesIterator().next();
	}

	@Override
	public void reset()
	{
		modules = null;
	}

	protected Iterator<CockpitModuleInfo> getModulesIterator()
	{
		if (modules == null)
		{
			modules = createModulesIterator();
		}

		return modules;
	}

	protected Iterator<CockpitModuleInfo> createModulesIterator()
	{
		return BackofficeModules.getBackofficeModules().stream().map(ExtensionInfo::getName).map(this::buildModuleInfo).iterator();
	}

	protected CockpitModuleInfo buildModuleInfo(final String moduleName)
	{
		final CockpitModuleInfo ret = new CockpitModuleInfo();
		ret.setLocationUrl(getExtensionModuleUrl(moduleName).toString());
		ret.setId(moduleName);
		ret.setIconUrl(getModuleIcon(moduleName));
		ret.setWidgetsPackage(getModulePackage(moduleName));
		ret.setParentModulesLocationUrls(resolveParentModules(moduleName));
		return ret;
	}

	protected String getModuleIcon(final String moduleName)
	{
		final String configuredUrl = PLATFORM_CONFIG.getExtensionInfo(moduleName)
				.getMeta(BackofficeConstants.BACKOFFICE_MODULE_ICON_META_KEY);
		return StringUtils.isNoneBlank(configuredUrl) ? configuredUrl : DEFAULT_MODULE_ICON;
	}

	protected String getModulePackage(final String moduleName)
	{
		return moduleName + FILE_POSTFIX_BOF;
	}

	protected Collection<String> resolveParentModules(final String moduleName)
	{
		final ExtensionInfo info = PLATFORM_CONFIG.getExtensionInfo(moduleName);
		return info.getAllRequiredExtensionInfos().stream()
				.filter(dependency -> Boolean.parseBoolean(dependency.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
				.map(dependency -> getExtensionModuleUrl(dependency.getName()).toString()).collect(Collectors.toList());
	}

	protected URI getExtensionModuleUrl(final String moduleName)
	{
		try
		{
			return new URI(URI_SCHEME_EXTENSION, moduleName, null, null);
		}
		catch (final URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	public static boolean isExtensionModuleURI(final URI moduleSource)
	{
		return moduleSource != null && URI_SCHEME_EXTENSION.equals(moduleSource.getScheme());
	}

}
