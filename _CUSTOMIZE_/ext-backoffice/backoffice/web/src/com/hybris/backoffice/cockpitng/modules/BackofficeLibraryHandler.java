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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hybris.cockpitng.core.CockpitApplicationException;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultCockpitConfigurationService;
import com.hybris.cockpitng.core.config.impl.jaxb.Config;
import com.hybris.cockpitng.core.config.impl.jaxb.Context;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.core.spring.CockpitApplicationContext;
import com.hybris.cockpitng.core.util.CockpitProperties;
import com.hybris.cockpitng.core.util.jaxb.SchemaValidationStatus;
import com.hybris.cockpitng.modules.CockpitModuleConnector;
import com.hybris.cockpitng.modules.CockpitModuleDeploymentException;
import com.hybris.cockpitng.modules.LibraryHandler;
import com.hybris.cockpitng.modules.server.ws.jaxb.CockpitModuleInfo;


/**
 * Implementation of {@link LibraryHandler} for hybris platform, responsible for getting the backoffice module extension
 * libs.
 */
public class BackofficeLibraryHandler extends BackofficeLibraryFetcher implements LibraryHandler<Object>, ApplicationContextAware
{

	public static final String CONFIG_CONTEXT_MODULE = "module";

	private static final Logger LOG = Logger.getLogger(BackofficeLibraryHandler.class);

	private static final String VALIDATE_COCKPIT_CONFIG_ON_STARTUP_PROPERTY = "cockpitng.validate.cockpitConfig.onstartup";

	private DefaultCockpitConfigurationService cockpitConfigurationService;

	private CockpitProperties cockpitProperties;

	private CockpitApplicationContext applicationContext;

	private CockpitModuleConnector cockpitModuleConnector;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = CockpitApplicationContext.getCockpitApplicationContext(applicationContext);
	}

	protected CockpitApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void fetchLibrary(final CockpitModuleInfo moduleInfo, final File archiveFile) throws CockpitModuleDeploymentException
	{
		try
		{
			fetchLibrary((ModuleInfo) moduleInfo, archiveFile);
		}
		catch (final CockpitModuleDeploymentException e)
		{
			throw e;
		}
		catch (final CockpitApplicationException e)
		{
			throw new CockpitModuleDeploymentException(e);
		}
	}

	@Override
	public Object prepare(final ModuleInfo moduleInfo)
	{
		final String extensionName = moduleInfo.getId();
		if (extensionName == null)
		{
			throw new IllegalArgumentException("Module info needs to define its identity");
		}
		final ClassLoader classLoader = getApplicationContext().getClassLoader();

		// set up spring configuration file location
		final String resourceFileName = BackofficeFileConventionUtils.getModuleSpringDefinitionsFile(extensionName);
		// NOTE: using getResourceAsStream to check if the resource exists - instead of getResource() -
		// because the cockpit widget loader does not handle it yet.
		final InputStream inputStream = classLoader.getResourceAsStream(resourceFileName);
		if (inputStream != null)
		{
			IOUtils.closeQuietly(inputStream);
			getCockpitModuleConnector().updateApplicationContextUri(moduleInfo, "classpath:" + resourceFileName);
		}

		// load widgets xml
		String widgetsString = "";
		final String widgetsFileName = BackofficeFileConventionUtils.getModuleWidgetsXmlFile(extensionName);

		InputStream resourceAsStream = null;
		try
		{
			resourceAsStream = classLoader.getResourceAsStream(widgetsFileName);
			if (resourceAsStream != null)
			{
				try
				{
					widgetsString = IOUtils.toString(resourceAsStream);
				}
				catch (final IOException e)
				{
					LOG.error("Could not read widget config for extension " + extensionName + ", reason: ", e);
				}

				getCockpitModuleConnector().updateWidgetsExtension(moduleInfo, widgetsString);
			}
		}
		finally
		{
			IOUtils.closeQuietly(resourceAsStream);
		}

		return null;
	}

	@Override
	public void initialize(final ModuleInfo moduleInfo, final Object o)
	{
		// add default config snippet
		final String extensionName = moduleInfo.getId();
		if (extensionName == null)
		{
			return;
		}
		final ClassLoader classLoader = getApplicationContext().getClassLoader();
		if (classLoader == null)
		{
			return;
		}

		final String configResourceFileName = BackofficeFileConventionUtils.getModuleConfigXmlFile(extensionName);

		if (shouldValidateCockpitConfigOnStartup())
		{

			final SchemaValidationStatus validationStatus = validateCockpitConfiguration(classLoader, configResourceFileName);
			if (validationStatus.isError())
			{
				LOG.error(
						configResourceFileName + " could not be validated and may not be merged. Check previous messages for details.");
				return;

			}
			else if (validationStatus.isWarning())
			{
				LOG.warn("Validation of " + configResourceFileName + " returned warnings. Check previous messages for details.");
			}
		}

		try
		{

			InputStream stream = null;
			try
			{
				stream = classLoader.getResourceAsStream(configResourceFileName);
				if (stream != null)
				{
					final Config rootConfig = this.cockpitConfigurationService.loadRootConfiguration(stream);

					if (rootConfig != null)
					{
						addModuleContext(moduleInfo.getId(), rootConfig);
						final Config mainRootConfig = this.cockpitConfigurationService.loadRootConfiguration();
						final boolean updated = updateMainConfig(mainRootConfig, rootConfig);
						if (updated)
						{
							this.cockpitConfigurationService.storeRootConfig(mainRootConfig);
						}
					}
				}
			}
			finally
			{
				IOUtils.closeQuietly(stream);
			}
		}
		catch (final CockpitConfigurationException e)
		{
			// ok
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Exception thrown: ", e);
			}
		}
	}

	protected SchemaValidationStatus validateCockpitConfiguration(final ClassLoader classLoader, final String configuration)
	{
		InputStream stream = null;
		try
		{
			stream = classLoader.getResourceAsStream(configuration);
			if (stream != null)
			{
				return this.cockpitConfigurationService.validate(stream);
			}
			else
			{
				return SchemaValidationStatus.error();
			}
		}
		finally
		{
			IOUtils.closeQuietly(stream);
		}
	}

	private boolean shouldValidateCockpitConfigOnStartup()
	{
		return cockpitProperties != null && cockpitProperties.getBoolean(VALIDATE_COCKPIT_CONFIG_ON_STARTUP_PROPERTY);
	}

	private void addModuleContext(final String moduleName, final Config rootConfig)
	{
		for (final Context context : rootConfig.getContext())
		{
			addModuleContext(moduleName, context);
		}
	}

	private void addModuleContext(final String moduleName, final Context context)
	{
		final Map<String, String> ctx = this.cockpitConfigurationService.getContext(context);
		ctx.put(CONFIG_CONTEXT_MODULE, moduleName);
		this.cockpitConfigurationService.setContext(context, ctx);

		for (final Context child : context.getContext())
		{
			addModuleContext(moduleName, child);
		}
	}

	private boolean updateMainConfig(final Config mainRootConfig, final Config rootConfig)
	{
		boolean updated = false;
		for (final Context context : rootConfig.getContext())
		{
			updated |= updateMainConfig(mainRootConfig, context);
		}
		return updated;
	}

	private boolean updateMainConfig(final Config mainRootConfig, final Context context)
	{
		boolean updated = false;
		final Object element = context.getAny();
		if (element != null)
		{
			final Map<String, String> ctx = this.cockpitConfigurationService.getContext(context);

			Context mainContext = null;
			final List<Context> mainContextList = findContext(mainRootConfig, ctx);
			if (mainContextList == null || mainContextList.isEmpty())
			{
				mainContext = new Context();
				mainContext.setMergeBy(context.getMergeBy());
				mainContext.setParent("auto".equals(context.getParent()) ? null : context.getParent());
				this.cockpitConfigurationService.setContext(mainContext, ctx);
				mainRootConfig.getContext().add(mainContext);
			}
			else
			{
				final Context lastOne = mainContextList.get(mainContextList.size() - 1);
				if (lastOne.getAny() == null)
				{
					mainContext = lastOne;
				}
			}

			if (mainContext != null && ObjectUtils.notEqual(mainContext.getAny(), element))
			{
				mainContext.setAny(element);
				updated = true;
			}
		}

		for (final Context child : context.getContext())
		{
			updated |= updateMainConfig(mainRootConfig, child);
		}

		return updated;
	}

	private List<Context> findContext(final Config mainRootConfig, final Map<String, String> ctx)
	{
		final List<Context> mainContextList = this.cockpitConfigurationService.findContext(mainRootConfig, ctx, false, true);
		if (CollectionUtils.isNotEmpty(mainContextList))
		{
			final List<Context> result = new ArrayList<>(mainContextList);
			for (final Context context : mainContextList)
			{
				final Map<String, String> ctx2 = this.cockpitConfigurationService.getContext(context);
				if (!ctx.equals(ctx2))
				{
					result.remove(context);
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	protected DefaultCockpitConfigurationService getCockpitConfigurationService()
	{
		return cockpitConfigurationService;
	}

	@Required
	public void setCockpitConfigurationService(final DefaultCockpitConfigurationService cockpitConfigurationService)
	{
		this.cockpitConfigurationService = cockpitConfigurationService;
	}

	protected CockpitProperties getCockpitProperties()
	{
		return cockpitProperties;
	}

	@Required
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

	protected CockpitModuleConnector getCockpitModuleConnector()
	{
		return cockpitModuleConnector;
	}

	@Required
	public void setCockpitModuleConnector(final CockpitModuleConnector cockpitModuleConnector)
	{
		this.cockpitModuleConnector = cockpitModuleConnector;
	}

	@Override
	public void afterDeploy(final CockpitModuleInfo moduleInfo, final String libDir)
	{
		prepare(moduleInfo);
	}

	@Override
	public void afterDeployReverseOrder(final CockpitModuleInfo moduleInfo, final String libDir)
	{
		initialize(moduleInfo, null);
	}

}
