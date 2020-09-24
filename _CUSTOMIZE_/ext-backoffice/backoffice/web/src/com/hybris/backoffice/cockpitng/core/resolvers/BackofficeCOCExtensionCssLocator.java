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
package com.hybris.backoffice.cockpitng.core.resolvers;

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.platform.util.Utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.cockpitng.core.persistence.packaging.CockpitResourceLoader;
import com.hybris.cockpitng.core.persistence.packaging.WidgetLibConstants;
import com.hybris.cockpitng.core.persistence.packaging.WidgetResourceLocator;


public class BackofficeCOCExtensionCssLocator implements WidgetResourceLocator
{

	public static final String CSS_CACHE_ENABLED = "cssCacheEnabled";

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeCOCExtensionCssLocator.class);

	private CockpitResourceLoader resourceLoader;

	private byte[] cachedResult;
	private List<String> cocResources;
	private String prefix;
	private String urlSuffix;
	private List<String> extNames;
	private String strippedPath;

	@Override
	public boolean isApplicableTo(final HttpServletRequest req, final ServletConfig servletConfig)
	{
		return StringUtils.endsWith(req.getRequestURI(), urlSuffix);
	}

	@Override
	public boolean apply(final HttpServletRequest req, final HttpServletResponse resp, final ServletConfig servletConfig)
	{
		try
		{
			if (isCssCached(servletConfig) && cachedResult != null)
			{
				resp.getOutputStream().write(cachedResult);
				return true;
			}
			synchronized (this)
			{
				if (cachedResult == null)
				{
					final StringBuilder builder = new StringBuilder();
					for (final String extName : getAllBackofficeExtensionNames())
					{
						for (final String res : cocResources)
						{
							final String path = res.replace(prefix, extName);
							if (StringUtils.isNotBlank(path) && !path.equals(WidgetLibConstants.RESOURCES_SUBFOLDER)
									&& getResourceLoader().hasResource(path))
							{
								strippedPath = path.replaceFirst(WidgetLibConstants.RESOURCES_SUBFOLDER, "");
								if (strippedPath.charAt(0) != '/')
								{
									strippedPath = "/".concat(strippedPath);
								}
								builder.append(String.format("@import url('../../%s%s');%n",
										WidgetLibConstants.CLASSPATH_RESOURCES_PATH_PREFIX, strippedPath));
							}
						}
					}
					cachedResult = builder.toString().getBytes(Charset.defaultCharset());
				}
			}
			resp.getOutputStream().write(cachedResult);
			return true;
		}
		catch (final IOException exc)
		{
			cachedResult = new byte[0];
			LOG.warn("Could not resolve resource", exc);
		}
		return false;
	}

	protected synchronized List<String> getAllBackofficeExtensionNames()
	{
		if (extNames == null)
		{
			extNames = new ArrayList<>();
			for (final ExtensionInfo extensionInfo : ConfigUtil.getPlatformConfig(Utilities.class).getExtensionInfosInBuildOrder())
			{
				final String extensionName = extensionInfo.getName();
				final ExtensionInfo extensionInfoObject = Utilities.getPlatformConfig().getExtensionInfo(extensionName);

				if (extensionInfoObject != null
						&& Boolean.parseBoolean(extensionInfoObject.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY))
						&& !extensionInfoObject.isExtgenTemplate())
				{
					extNames.add(extensionName);
				}
			}
		}
		return extNames;
	}

	protected boolean isCssCached(final ServletConfig servletConfig)
	{
		return Boolean.parseBoolean(servletConfig.getInitParameter(CSS_CACHE_ENABLED));
	}

	public void setUrlSuffix(final String urlSuffix)
	{
		this.urlSuffix = urlSuffix;
	}

	public void setCocResources(final List<String> cocResources)
	{
		this.cocResources = cocResources;
	}

	public void setPrefix(final String prefix)
	{
		this.prefix = prefix;
	}

	protected CockpitResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	@Required
	public void setResourceLoader(final CockpitResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

}
