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

import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.bootstrap.config.PlatformConfig;
import de.hybris.platform.util.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Streams;
import com.hybris.backoffice.constants.BackofficeConstants;
import com.hybris.backoffice.constants.BackofficeModules;
import com.hybris.cockpitng.core.modules.ModuleInfo;
import com.hybris.cockpitng.core.spring.CockpitApplicationContext;
import com.hybris.cockpitng.modules.ModuleEntry;
import com.hybris.cockpitng.modules.impl.AbstractCockpitModuleConnector;
import com.hybris.cockpitng.modules.server.ws.jaxb.CockpitModuleInfo;


/**
 * Extended version of {@link AbstractCockpitModuleConnector}, can load cockpit modules from hybris backoffice-module
 * extensions.
 */
public class BackofficeModuleConnector extends AbstractCockpitModuleConnector implements ApplicationContextAware
{
	private static final Logger LOG = LoggerFactory.getLogger(BackofficeModuleConnector.class);
	private final PlatformConfig platformConfig = ConfigUtil.getPlatformConfig(Utilities.class);

	private CockpitApplicationContext applicationContext;

	/**
	 * @deprecated since 1808, use {@link BackofficeModules#getBackofficeModules()}
	 */
	@Deprecated
	public List<ModuleEntry> getHybrisModulesFromExtensions()
	{
		final List<ModuleEntry> ret = new ArrayList<>();

		for (final ExtensionInfo extensionInfo : platformConfig.getExtensionInfosInBuildOrder())
		{
			final String extensionName = extensionInfo.getName();
			createModuleEntry(extensionName).ifPresent(ret::add);
		}
		return ret;
	}

	protected Optional<ModuleEntry> createModuleEntry(final String extensionName)
	{
		final ExtensionInfo extensionInfo = Utilities.getPlatformConfig().getExtensionInfo(extensionName);
		if (extensionInfo != null && Boolean.parseBoolean(extensionInfo.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
		{
			final ModuleEntry men = new ModuleEntry();
			men.setLabel("Extension: " + extensionName);
			men.setEnabled(true);
			men.setParentModuleUrls(resolveParentModules(extensionInfo));
			men.setUrl(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX + extensionName);
			return Optional.of(men);
		}
		else
		{
			return Optional.empty();
		}
	}

	protected Collection<String> resolveParentModules(final ExtensionInfo info)
	{
		final List<String> urls = new ArrayList<>();
		for (final ExtensionInfo dependency : info.getAllRequiredExtensionInfos())
		{
			if (dependency != null && Boolean.parseBoolean(dependency.getMeta(BackofficeConstants.BACKOFFICE_MODULE_META_KEY)))
			{
				urls.add(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX + dependency.getName());
			}
		}
		return urls;
	}

	@Override
	public void setDefaultModules(final List<ModuleEntry> hybrisModules)
	{
		final List<ModuleEntry> hybrisModulesFromExtensions = getHybrisModulesFromExtensions();
		final Stream<ModuleEntry> modulesFromContext = getApplicationContext().getLoadedModulesNames().stream()
				.map(this::createModuleEntry).filter(Optional::isPresent).map(Optional::get);
		hybrisModulesFromExtensions.addAll(hybrisModules);
		final Collection<ModuleEntry> moduleEntries = Streams
				.concat(hybrisModulesFromExtensions.stream(), modulesFromContext, hybrisModules.stream())
				.collect(Collectors.toMap(ModuleEntry::getUrl, Function.identity(), (e1, e2) -> e2, LinkedHashMap::new)).values();
		super.setDefaultModules(new ArrayList<>(moduleEntries));
	}

	@Override
	public List<ModuleEntry> getDefaultModules()
	{
		if (super.getDefaultModules() == null)
		{
			setDefaultModules(Collections.emptyList());
		}
		return super.getDefaultModules();
	}

	@Override
	public CockpitModuleInfo getModuleInfo(final String moduleUri, final boolean cached)
	{
		if (moduleUri.startsWith(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX))
		{
			if (cached)
			{
				final CockpitModuleInfo cachedCockpitModuleInfo = moduleCache.get(moduleUri);
				if (cachedCockpitModuleInfo != null)
				{
					return cachedCockpitModuleInfo;
				}
				else
				{
					LOG.debug("Could not find module info in cache for '{}', creating new one.", moduleUri);
				}
			}

			final String extName = moduleUri.substring(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX.length());
			final CockpitModuleInfo cockpitModuleInfo = getApplicationContext().getModuleInfo(extName)
					.map(this::buildCockpitModuleInfo).orElse(buildCockpitModuleInfo(moduleUri));
			moduleCache.put(moduleUri, cockpitModuleInfo);
			return cockpitModuleInfo;
		}
		else
		{
			return null;
		}
	}

	protected CockpitModuleInfo buildCockpitModuleInfo(final ModuleInfo moduleInfo)
	{
		if (moduleInfo instanceof CockpitModuleInfo)
		{
			return (CockpitModuleInfo) moduleInfo;
		}
		else
		{
			final CockpitModuleInfo ret = new CockpitModuleInfo();
			ret.setLocationUrl(moduleInfo.getLocationUrl());
			ret.setIconUrl(moduleInfo.getIconUrl());
			ret.setId(moduleInfo.getId());
			ret.setParentModulesLocationUrls(moduleInfo.getParentModulesLocationUrls());
			ret.setWidgetsPackage(moduleInfo.getWidgetsPackage());
			return ret;
		}
	}

	protected CockpitModuleInfo buildCockpitModuleInfo(final String moduleUri)
	{
		final String extName = moduleUri.substring(BackofficeFileConventionUtils.EXTENSION_PROTOCOL_PREFIX.length());
		final Optional<ModuleEntry> module = getDefaultModules().stream().filter(x -> StringUtils.equals(x.getUrl(), moduleUri))
				.findFirst();
		final CockpitModuleInfo ret = new CockpitModuleInfo();
		ret.setLocationUrl(moduleUri);
		ret.setIconUrl("/cng/img/MMC.png");
		ret.setId(extName);
		module.ifPresent(moduleEntry -> ret.setParentModulesLocationUrls(moduleEntry.getParentModuleUrls()));
		ret.setWidgetsPackage(extName + "_bof.jar");
		return ret;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = CockpitApplicationContext.getCockpitApplicationContext(applicationContext);
	}

	protected CockpitApplicationContext getApplicationContext()
	{
		return applicationContext;
	}
}
