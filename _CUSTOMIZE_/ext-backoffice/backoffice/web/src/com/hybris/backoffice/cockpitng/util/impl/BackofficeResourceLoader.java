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

import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.platform.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.modules.BackofficeFileConventionUtils;
import com.hybris.cockpitng.core.persistence.packaging.WidgetJarLibInfo;
import com.hybris.cockpitng.core.persistence.packaging.WidgetLibConstants;
import com.hybris.cockpitng.core.persistence.packaging.impl.DefaultCockpitResourceLoader;
import com.hybris.cockpitng.core.util.CockpitProperties;
import com.hybris.cockpitng.modules.CockpitModuleConnector;


/**
 * Resource loader for faster development. Don't use it in a production environment. Gets widget resources from hybris
 * extensions directly, so changes in widget view files, css, images, etc. are visible after a browser refresh. Must be
 * enabled by property "[PREFIX]cockpitng.additionalResourceLoader.enabled=true".
 */
public class BackofficeResourceLoader extends DefaultCockpitResourceLoader
{

	public static final int ORDER = DefaultCockpitResourceLoader.ORDER - 1;

	private static final String PROPERTY_KEY = DefaultCockpitResourceLoader.ENABLED_KEY;
	private static final Logger LOGGER = LoggerFactory.getLogger(BackofficeResourceLoader.class);
	private final Map<String, File> fileCache = new HashMap<String, File>();
	private CockpitProperties cockpitProperties;
	private CockpitModuleConnector cockpitModuleConnector;
	private Boolean enabledCached = null;

	@Override
	public InputStream getViewResourceAsStream(final String path)
	{
		return getResourceAsStream(path);
	}

	@Override
	public String getViewResourceAsString(final WidgetJarLibInfo jarLibInfo, final String filename)
	{
		try (final InputStream stream = getResourceAsStream(jarLibInfo, filename))
		{
			if (stream != null)
			{
				return IOUtils.toString(stream);
			}
		}
		catch (final IOException e)
		{
			LOGGER.error("Error reading " + filename, e);
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final File moduleJar, final String path)
	{
		return getResourceAsStream(path,
				BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX + FilenameUtils.getBaseName(moduleJar.getName()));
	}

	@Override
	public InputStream getResourceAsStream(final String path)
	{
		return getResourceAsStream(path, null);
	}

	public InputStream getResourceAsStream(final String path, final String module)
	{
		if (!isEnabled())
		{
			return null;
		}
		return fetchResource(path, module);
	}

	protected InputStream fetchResource(final String path, final String module)
	{
		final String cacheKey = (module != null) ? (module + "/" + path) : path;
		File file = fileCache.get(cacheKey);
		if (file == null)
		{
			file = module == null ? resolveFile(path) : resolveFileInModule(path, module);
		}
		if (file != null)
		{
			fileCache.put(cacheKey, file);
			try
			{
				return FileUtils.openInputStream(file);
			}
			catch (final IOException e)
			{
				LOGGER.error("Could not open file " + file, e);
			}
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final WidgetJarLibInfo jarLibInfo, final String filename)
	{
		if (!isEnabled())
		{
			return null;
		}
		if (jarLibInfo != null)
		{
			final String externalLocation = jarLibInfo.getExternalLocation();
			final String prefix = jarLibInfo.getPrefix();
			final String fullFilename = filename.startsWith("/") ? filename : ("/" + prefix + "/" + filename);
			final InputStream resourceAsStream = getResourceAsStream(fullFilename, externalLocation);
			if (resourceAsStream != null)
			{
				return resourceAsStream;
			}
		}
		return null;
	}

	@Override
	public boolean hasResource(final String path)
	{
		if (!isEnabled())
		{
			return false;
		}
		return resolveFile(path) != null;
	}

	@Override
	public boolean hasResource(final WidgetJarLibInfo jarLibInfo, final String path)
	{
		if (!isEnabled())
		{
			return false;
		}
		final String externalLocation = jarLibInfo.getExternalLocation();
		final String prefix = jarLibInfo.getPrefix();
		final String fullFilename = path.startsWith("/") ? path : ("/" + prefix + "/" + path);
		return (externalLocation != null ? resolveFileInModule(fullFilename, externalLocation) : resolveFile(fullFilename)) != null;
	}

	@Override
	public boolean hasResource(final File moduleJar, final String path)
	{
		if (!isEnabled())
		{
			return false;
		}
		return resolveFileInModule(path,
				BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX + FilenameUtils.getBaseName(moduleJar.getName())) != null;
	}

	private static File resolveFileInModule(final String path, final String module)
	{
		if (module.startsWith(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX))
		{
			final String extensionName = module.replaceFirst(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX, "");
			final ExtensionInfo extensionInfo = Utilities.getExtensionInfo(extensionName);
			if (extensionInfo != null)
			{
				final File extensionDirectory = extensionInfo.getExtensionDirectory();
				final String adjustedPath = path.contains(WidgetLibConstants.RESOURCES_SUBFOLDER)
						? path.replaceFirst(WidgetLibConstants.RESOURCES_SUBFOLDER, "/backoffice/resources/")
						: ("/backoffice/resources/" + path);
				final File resourceFile = new File(extensionDirectory, adjustedPath);

				if (resourceFile.exists())
				{
					return resourceFile;
				}
			}
		}
		return null;
	}

	@Override
	protected File resolveFile(final String path)
	{
		for (final String url : cockpitModuleConnector.getCockpitModuleUrls())
		{
			final File file = resolveFileInModule(path, url);
			if (file != null && file.exists())
			{
				return file;
			}
		}
		return super.resolveFile(path);
	}

	@Override
	protected boolean isEnabled()
	{
		if (enabledCached == null)
		{
			enabledCached = Boolean
					.valueOf(cockpitProperties != null && Boolean.parseBoolean(cockpitProperties.getProperty(PROPERTY_KEY)));
		}
		return Boolean.TRUE.equals(enabledCached);
	}

	@Override
	public CockpitProperties getCockpitProperties()
	{
		return cockpitProperties;
	}

	@Override
	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

	@Required
	public void setCockpitModuleConnector(final CockpitModuleConnector cockpitModuleConnector)
	{
		this.cockpitModuleConnector = cockpitModuleConnector;
	}

	@Override
	public int getOrder()
	{
		return ORDER;
	}
}
