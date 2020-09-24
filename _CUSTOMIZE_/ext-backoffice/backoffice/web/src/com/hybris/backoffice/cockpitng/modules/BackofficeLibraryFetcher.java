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

import de.hybris.platform.core.MasterTenant;
import de.hybris.platform.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.BackofficeModulesEnumeration;
import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.cockpitng.core.CockpitApplicationException;
import com.hybris.cockpitng.core.modules.LibraryFetcher;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.modules.CockpitModuleDeploymentException;


/**
 * An object responsible for default fetching of available Backoffice modules. It looks for module package
 * ({@link ModuleInfo#getWidgetsPackage()} and copies it into specified module lib file.
 */
public class BackofficeLibraryFetcher extends BackofficeModulesEnumeration implements LibraryFetcher
{

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeLibraryFetcher.class);

	private static final String URI_SCHEME_SIMPLIFIED_EXTENSION = "simplified-extension";

	@Override
	protected URI getExtensionModuleUrl(final String moduleName)
	{
		if (canFetchLibrary(moduleName, getModulePackage(moduleName)))
		{
			return super.getExtensionModuleUrl(moduleName);
		}
		else
		{
			return getSimplifiedExtensionModuleURI(moduleName);
		}
	}

	@Override
	public boolean canFetchLibrary(final ModuleInfo moduleInfo)
	{
		final boolean moduleDefined = moduleInfo != null && StringUtils.isNotBlank(moduleInfo.getId());
		return moduleDefined && canFetchLibrary(moduleInfo.getId(), moduleInfo.getWidgetsPackage());
	}

	protected boolean canFetchLibrary(final String moduleName, final String modulePackage)
	{
		if (StringUtils.isNotBlank(moduleName))
		{
			return findWidgetPackage(moduleName, modulePackage) != null;
		}

		return false;
	}

	@Override
	public void fetchLibrary(final ModuleInfo moduleInfo, final File archiveFile) throws CockpitApplicationException
	{
		if (moduleInfo == null)
		{
			throw new CockpitModuleDeploymentException("Could not get module library, module info was null");
		}
		final String extensionName = moduleInfo.getId();
		if (extensionName == null)
		{
			throw new CockpitModuleDeploymentException("Could not get module library, module info id was null");
		}
		final URL resourceUrl = findWidgetPackage(extensionName, moduleInfo.getWidgetsPackage());
		if (resourceUrl != null)
		{
			copyURLToFile(moduleInfo, resourceUrl, archiveFile);
		}
		else
		{
			final Class managerClass = Utilities.getExtensionClass(MasterTenant.getInstance(), extensionName);
			if (managerClass == null)
			{
				throw new CockpitModuleDeploymentException("Could not load manager class for extension '" + extensionName + "'");
			}

			throw new CockpitModuleDeploymentException(
					"Either create a 'backoffice' folder or remove backoffice nature declaration from extension: "
							+ moduleInfo.getId());
		}
	}

	protected void copyURLToFile(final ModuleInfo moduleInfo, final URL source, final File destination)
			throws CockpitModuleDeploymentException
	{
		try
		{
			LOG.info("[{}] Copying {} from extension", moduleInfo.getId(), moduleInfo.getWidgetsPackage());
			FileUtils.copyURLToFile(source, destination);
		}
		catch (final IOException e)
		{
			throw new CockpitModuleDeploymentException("Could not get fetch module library", e);
		}
	}

	protected URL findWidgetPackage(final String moduleName, final String packageName)
	{
		final Class managerClass = Utilities.getExtensionClass(MasterTenant.getInstance(), moduleName);
		return managerClass != null ? findWidgetPackage(managerClass, packageName) : null;
	}

	protected URL findWidgetPackage(final Class<?> managerClass, final String packageName)
	{
		return managerClass.getResource("/" + BackofficeConstants.EXTENSIONNAME + "/" + packageName);
	}

	protected URI getSimplifiedExtensionModuleURI(final String moduleName)
	{
		try
		{
			return new URI(URI_SCHEME_SIMPLIFIED_EXTENSION, moduleName, null, null);
		}
		catch (final URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	public static boolean isSimplifiedExtensionModuleURI(final URI moduleSource)
	{
		return moduleSource != null && URI_SCHEME_SIMPLIFIED_EXTENSION.equals(moduleSource.getScheme());
	}

}
