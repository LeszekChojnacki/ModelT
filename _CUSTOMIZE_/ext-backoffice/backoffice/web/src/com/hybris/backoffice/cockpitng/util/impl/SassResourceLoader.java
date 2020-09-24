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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import com.hybris.cockpitng.core.persistence.packaging.WidgetJarLibInfo;
import com.hybris.cockpitng.core.persistence.packaging.impl.AbstractCockpitResourceReader;
import com.hybris.cockpitng.core.persistence.packaging.impl.DefaultCockpitResourceLoader;
import com.hybris.cockpitng.core.util.CockpitProperties;


public class SassResourceLoader extends AbstractCockpitResourceReader implements Ordered
{

	private static final int ORDER = DefaultCockpitResourceLoader.ORDER - 1;
	private static final String SASS_ENABLED_PROPERTY_KEY = "sass.enabled";
	private static final String CSS_EXTENSION = "css";
	private static final String CSS_MAP_EXTENSION = "css.map";
	private static final String BACKOFFICE_EXTENSION_NAME = "backoffice";
	private static final String BACKOFFICE_GENERATED_CSS_FILES_PATH = "/resources/backoffice/generated";
	private CockpitProperties cockpitProperties;
	private Boolean enabledSass = null;

	@Override
	public InputStream getResourceAsStream(final File moduleJar, final String path)
	{
		return getResourceAsStream(path);
	}

	@Override
	public InputStream getResourceAsStream(final String path)
	{
		if (isSupported(path))
		{
			try
			{
				final File resolvedFile = resolveFile(path);
				return resolvedFile == null ? null : FileUtils.openInputStream(resolvedFile);
			}
			catch (final IOException e)
			{
				LOG.error("Could not open file " + path, e);
			}
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final WidgetJarLibInfo jarLibInfo, final String filename)
	{
		if (jarLibInfo != null && isSupported(filename))
		{
			final String prefix = jarLibInfo.getPrefix();
			final String fullFilename = filename.startsWith("/") ? filename : ("/" + prefix + "/" + filename);
			try
			{
				final File resolvedFile = resolveFile(fullFilename);
				return resolvedFile == null ? null : FileUtils.openInputStream(resolvedFile);
			}
			catch (final IOException e)
			{
				LOG.error("Could not open file " + fullFilename, e);
			}
		}
		return null;
	}

	@Override
	public boolean hasResource(final String path)
	{
		return resolveFile(path) != null;
	}

	@Override
	public boolean hasResource(final WidgetJarLibInfo jarLibInfo, final String path)
	{
		final String prefix = jarLibInfo.getPrefix();
		final String fullFilename = path.startsWith("/") ? path : ("/" + prefix + "/" + path);
		return resolveFile(fullFilename) != null;
	}

	@Override
	public boolean hasResource(final File moduleJar, final String path)
	{
		return resolveFile(path) != null;
	}

	private File resolveFile(final String path)
	{
		if (isSupported(path))
		{
			final ExtensionInfo extensionInfo = Utilities.getExtensionInfo(BACKOFFICE_EXTENSION_NAME);
			if (extensionInfo != null)
			{
				final File extensionDirectory = extensionInfo.getExtensionDirectory();
				final String adjustedPath = String.format("%s%s%s", extensionDirectory, BACKOFFICE_GENERATED_CSS_FILES_PATH, path);
				final File resourceFile = new File(adjustedPath);

				if (resourceFile.exists())
				{
					return resourceFile;
				}
			}
		}
		return null;
	}

	protected boolean isSupported(final String path)
	{
		return isEnabled() && StringUtils.isNotBlank(path)
				&& (StringUtils.endsWith(path, CSS_EXTENSION) || StringUtils.endsWith(path, CSS_MAP_EXTENSION));
	}

	protected boolean isEnabled()
	{
		if (enabledSass == null)
		{
			final boolean enabled = cockpitProperties != null
					&& Boolean.parseBoolean(cockpitProperties.getProperty(SASS_ENABLED_PROPERTY_KEY));
			enabledSass = Boolean.valueOf(enabled);
		}
		return Boolean.TRUE.equals(enabledSass);
	}

	public void setCockpitProperties(final CockpitProperties cockpitProperties)
	{
		this.cockpitProperties = cockpitProperties;
	}

	@Override
	public int getOrder()
	{
		return ORDER;
	}
}
