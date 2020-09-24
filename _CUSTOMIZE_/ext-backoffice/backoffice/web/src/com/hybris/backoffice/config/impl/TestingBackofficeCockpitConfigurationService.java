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
package com.hybris.backoffice.config.impl;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ResourceLoader;

import com.hybris.backoffice.cockpitng.modules.BackofficeLibraryHandler;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.jaxb.Config;
import com.hybris.cockpitng.core.config.impl.jaxb.Context;


/**
 * Configuration service used for testing purposes. Allows injection of custom configuration snippets.
 */
public class TestingBackofficeCockpitConfigurationService extends BackofficeCockpitConfigurationService
{
	private static final String MESSAGE_UNABLE_TO_APPLY_NULL_CUSTOMIZATION = "Unable to apply null customization. Please use #clearCustomConfiguration to remove customizations.";
	private ResourceLoader resourceLoader;
	private Config customConfiguration;


	@Override
	protected void cacheRootConfiguration(final Config rootConfig, final long cacheTimestamp)
	{
		getCustomConfiguration().ifPresent(customConfig -> updateMainConfig(rootConfig, customConfig));
		super.cacheRootConfiguration(rootConfig, cacheTimestamp);
	}

	protected void updateMainConfig(final Config mainRootConfig, final Config rootConfig)
	{
		for (final Context context : rootConfig.getContext())
		{
			updateMainConfig(mainRootConfig, context);
		}
	}

	protected void updateMainConfig(final Config mainRootConfig, final Context context)
	{
		final Map<String, String> ctx = getContext(context);
		ctx.remove(BackofficeLibraryHandler.CONFIG_CONTEXT_MODULE);
		setContext(context, ctx);
		mainRootConfig.getContext().add(context);
	}

	public Optional<Config> getCustomConfiguration()
	{
		return Optional.ofNullable(customConfiguration);
	}

	/**
	 * Removes any customization to configuration that has been applied earlier.
	 *
	 * @see #setCustomConfiguration(Config, String)
	 */
	public void clearCustomConfiguration()
	{
		this.customConfiguration = null;
		invalidateRootConfigurationCache();
	}

	/**
	 * Sets additional custom configuration that should be appended to the one loaded from system configuration
	 *
	 * @param customConfiguration
	 *           configuration to be appended
	 * @param source
	 *           name of a source for applied custom configuration
	 */
	public void setCustomConfiguration(final Config customConfiguration, final String source)
	{
		if (customConfiguration == null)
		{
			throw new IllegalArgumentException(MESSAGE_UNABLE_TO_APPLY_NULL_CUSTOMIZATION);
		}
		this.customConfiguration = customConfiguration;
		this.customConfiguration.getContext().forEach(context -> {
			final Map<String, String> ctx = getContext(context);
			ctx.put(BackofficeLibraryHandler.CONFIG_CONTEXT_MODULE, source);
			setContext(context, ctx);
		});

		invalidateRootConfigurationCache();
	}

	/**
	 * Sets additional custom configuration that should be appended to the one loaded from system configuration @param
	 * customConfiguration configuration XML to be parsed and appended @param source name of a source for applied custom
	 * configuration
	 *
	 * @param customConfiguration
	 *           custom configuration snippet to apply
	 * @param source
	 *           source of snippet
	 * @throws CockpitConfigurationException
	 *            - when cockpit configuration cannot be loaded
	 */
	public void setCustomConfiguration(final String customConfiguration, final String source) throws CockpitConfigurationException
	{
		if (customConfiguration == null)
		{
			throw new IllegalArgumentException(MESSAGE_UNABLE_TO_APPLY_NULL_CUSTOMIZATION);

		}

		final Config customConfig = loadRootConfiguration(new ByteArrayInputStream(customConfiguration.getBytes()));
		setCustomConfiguration(customConfig, source);
	}

	protected ResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	@Required
	public void setResourceLoader(final ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}
}
