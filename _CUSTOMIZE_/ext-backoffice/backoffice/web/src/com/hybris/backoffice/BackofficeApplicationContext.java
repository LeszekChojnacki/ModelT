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

import de.hybris.platform.spring.ctx.TenantIgnoreXmlWebApplicationContext;
import de.hybris.platform.util.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContextException;

import com.hybris.backoffice.cockpitng.modules.BackofficeLibraryFetcher;
import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.cockpitng.core.CockpitApplicationException;
import com.hybris.cockpitng.core.modules.LibraryFetcher;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.core.persistence.packaging.WidgetClassLoader;
import com.hybris.cockpitng.core.persistence.packaging.impl.DefaultWidgetLibUtils;
import com.hybris.cockpitng.core.spring.CockpitApplicationContext;
import com.hybris.cockpitng.core.spring.ModuleContentProvider;
import com.hybris.cockpitng.modules.ModulesEnumeration;


/**
 * Application context for Backoffice.
 */
public class BackofficeApplicationContext extends TenantIgnoreXmlWebApplicationContext implements CockpitApplicationContext
{

	private static final Logger LOGGER = LoggerFactory.getLogger(BackofficeApplicationContext.class);

	private static final String CONFIG_LIBRARY_FETCHER = "backoffice.library.fetcher.class";
	private static final String CONFIG_MODULES_ENUMERATION = "backoffice.modules.enum.class";

	private static final String DEFAULT_LIBRARY_FETCHER = BackofficeLibraryFetcher.class.getName();
	private static final String DEFAULT_MODULES_ENUMERATION = BackofficeModulesEnumeration.class.getName();

	private BackofficeModulesManager modulesManager;

	public BackofficeApplicationContext(final String tenantId, final String ctxPath)
	{
		super(tenantId, ctxPath);
	}

	public BackofficeApplicationContext()
	{
		super();
	}

	@Override
	public WidgetClassLoader getClassLoader()
	{
		return (WidgetClassLoader) super.getClassLoader();
	}

	@Override
	public void setClassLoader(final ClassLoader classLoader)
	{
		revertWidgetClassLoader();
		super.setClassLoader(classLoader);
		initializeWidgetClassLoader();
	}

	@Override
	public boolean isReady()
	{
		return ApplicationUtils.isPlatformReady();
	}

	@Override
	protected void prepareRefresh()
	{
		revertWidgetClassLoader();
		getModulesManager().assureCorrectExistingModulesStructure();
		initializeWidgetClassLoader();

		try
		{
			getModulesManager().refreshAndFetch();
		}
		catch (final CockpitApplicationException ex)
		{
			throw new BeanDefinitionValidationException(ex.getLocalizedMessage(), ex);
		}

		super.prepareRefresh();
	}

	protected void revertWidgetClassLoader()
	{
		final ClassLoader currentClassLoader = super.getClassLoader();
		if (currentClassLoader instanceof WidgetClassLoader)
		{
			try
			{
				((WidgetClassLoader) currentClassLoader).close();
				super.setClassLoader(currentClassLoader.getParent());
			}
			catch (final IOException ex)
			{
				LOGGER.error("Failed to close modules class loader", ex);
			}

		}
	}

	protected void initializeWidgetClassLoader()
	{
		final ClassLoader currentClassLoader = super.getClassLoader();
		if (!(currentClassLoader instanceof WidgetClassLoader))
		{
			super.setClassLoader(createWidgetClassLoader(currentClassLoader));
		}
	}


	protected WidgetClassLoader createWidgetClassLoader(final ClassLoader parent)
	{
		final PrivilegedAction<WidgetClassLoader> action = () -> new WidgetClassLoader(parent,
				getModulesManager().getModuleJarsRootDir().getAbsolutePath(), isResourceCacheEnabled());
		return AccessController.doPrivileged(action);
	}

	protected BackofficeModulesManager getModulesManager()
	{
		if (modulesManager == null)
		{
			modulesManager = createModulesManager();
		}
		return modulesManager;
	}

	protected boolean isResourceCacheEnabled()
	{
		return Config.getBoolean(
				BackofficeConstants.EXTENSIONNAME + "." + DefaultWidgetLibUtils.COCKPITNG_WIDGETCLASSLOADER_RESOURCECACHE_ENABLED,
				true);
	}

	protected BackofficeModulesManager createModulesManager()
	{
		final ModulesEnumeration modulesEnumeration = createModulesEnumeration();
		final LibraryFetcher fetcher = createModulesFetcher(modulesEnumeration);
		return new BackofficeModulesManager(fetcher, modulesEnumeration);
	}

	protected LibraryFetcher createModulesFetcher(final ModulesEnumeration enumeration)
	{
		final String configuredClassName = getLibraryFetcherClassName();
		if ((enumeration instanceof LibraryFetcher) && StringUtils.isBlank(configuredClassName))
		{
			return (LibraryFetcher) enumeration;
		}
		final String className = configuredClassName != null ? configuredClassName : DEFAULT_LIBRARY_FETCHER;
		try
		{
			final Class<? extends LibraryFetcher> handlerClass = (Class<? extends LibraryFetcher>) Class.forName(className, true,
					super.getClassLoader());
			return BeanUtils.instantiate(handlerClass);
		}
		catch (final ClassNotFoundException ex)
		{
			throw new ApplicationContextException("Unable to initialize Backoffice application context", ex);
		}
	}

	protected String getLibraryFetcherClassName()
	{
		return Config.getParameter(CONFIG_LIBRARY_FETCHER);
	}

	protected ModulesEnumeration createModulesEnumeration()
	{
		final String className = getModulesEnumerationClassName();
		try
		{
			final Class<? extends ModulesEnumeration> handlerClass = (Class<? extends ModulesEnumeration>) Class.forName(className,
					true, super.getClassLoader());
			return BeanUtils.instantiate(handlerClass);
		}
		catch (final ClassNotFoundException ex)
		{
			throw new ApplicationContextException("Unable to initialize Backoffice application context", ex);
		}
	}

	protected String getModulesEnumerationClassName()
	{
		return Config.getString(CONFIG_MODULES_ENUMERATION, DEFAULT_MODULES_ENUMERATION);
	}

	@Override
	public File getDataRootDir()
	{
		return getModulesManager().getDataRootDir();
	}

	@Override
	public List<String> getLoadedModulesNames()
	{
		return getModulesManager().getModules();
	}

	@Override
	public Optional<String> getModuleName(final URI moduleURI)
	{
		return getModulesManager().getModuleName(moduleURI);
	}

	@Override
	public Optional<URI> getModuleURI(final String moduleName)
	{
		return getModulesManager().getModuleSource(moduleName);
	}

	@Override
	public Optional<ModuleInfo> getModuleInfo(final String moduleName)
	{
		return getModulesManager().getModuleInfo(moduleName);
	}

	@Override
	public void registerNewModule(final String moduleName, final ModuleContentProvider contentsProvider)
			throws CockpitApplicationException
	{
		if (getModulesManager().isModuleRegistered(moduleName))
		{
			throw new CockpitApplicationException("Module already registered: " + moduleName);
		}

		OutputStream stream = null;
		try
		{
			final File moduleJar = getModulesManager().registerNewModuleJar(moduleName);
			stream = contentsProvider.prepareStream(createDefaultModuleJarStream(moduleJar));
			contentsProvider.writeContent(stream);
			contentsProvider.finalizeStream(stream);
		}
		catch (final IOException ex)
		{
			try
			{
				getModulesManager().unregisterModuleJar(moduleName);
			}
			catch (final IOException ie)
			{
				LOGGER.error(ie.getLocalizedMessage(), ie);
			}

			throw new CockpitApplicationException(ex);
		}
		finally
		{
			if (stream != null)
			{
				IOUtils.closeQuietly(stream);
			}
		}

		refresh();
	}

	protected OutputStream createDefaultModuleJarStream(final File moduleJar) throws FileNotFoundException
	{
		return new FileOutputStream(moduleJar);
	}

	@Override
	public void unregisterModule(final String moduleName) throws CockpitApplicationException
	{
		try
		{
			revertWidgetClassLoader();
			getModulesManager().unregisterModuleJar(moduleName);
		}
		catch (final IOException ex)
		{
			throw new CockpitApplicationException(ex);
		}
		finally
		{
			initializeWidgetClassLoader();
		}
	}

}
