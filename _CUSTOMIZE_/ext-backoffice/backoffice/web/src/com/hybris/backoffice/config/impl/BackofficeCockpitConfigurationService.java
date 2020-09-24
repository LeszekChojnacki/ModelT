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

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.type.ViewTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.CockpitConfigurationPersistenceStrategy;
import com.hybris.cockpitng.core.config.ConfigContext;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.util.ClassLoaderUtils;
import com.hybris.cockpitng.modules.config.impl.ModuleAwareCockpitConfigurationService;


/**
 * Backoffice extension of the {@link ModuleAwareCockpitConfigurationService}. The implementation uses
 * {@link MediaModel} to persist the xml content.
 */
public class BackofficeCockpitConfigurationService extends ModuleAwareCockpitConfigurationService
{
	/**
	 * @deprecated since 1811, the code will be removed
	 */
	@Deprecated
	public static final String COCKPITNG_CONFIG = "cockpitng-config";

	/**
	 * @deprecated since 1811, the code will be removed
	 */
	@Deprecated
	public static final String MIME_TYPE = "text/xml";

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeCockpitConfigurationService.class);
	private MediaService mediaService;
	private ModelService modelService;
	private TypeService typeService;
	private SessionService sessionService;
	private UserService userService;
	private TimeService timeService;
	private BackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper;

	/**
	 * @deprecated since 1811, the code will be removed
	 */
	@Deprecated
	protected InputStream getInputStreamForMedia(final MediaModel media)
	{
		return getRequiredPersistenceStrategy().getInputStreamForMedia(media);
	}

	/**
	 * @deprecated since 1811, the code will be removed
	 */
	@Deprecated
	@Override
	protected ByteArrayOutputStream getConfigFileOutputStream()
	{
		return (ByteArrayOutputStream) getRequiredPersistenceStrategy().getConfigurationOutputStream();
	}

	private DefaultMediaCockpitConfigurationPersistenceStrategy getRequiredPersistenceStrategy()
	{
		final CockpitConfigurationPersistenceStrategy strategy = getPersistenceStrategy();
		if (strategy instanceof DefaultMediaCockpitConfigurationPersistenceStrategy)
		{
			return (DefaultMediaCockpitConfigurationPersistenceStrategy) strategy;
		}
		throw new IllegalStateException(
				"Use " + DefaultMediaCockpitConfigurationPersistenceStrategy.class.getName() + " instead of " + strategy);
	}

	/**
	 * Get the media by code. If there is no media createConfigFile() will be called.
	 *
	 * @deprecated since 1811, the code will be removed
	 */
	@Deprecated
	protected MediaModel getCockpitNGConfig() throws CockpitConfigurationException
	{
		return getRequiredPersistenceStrategy().getCockpitNGConfig();
	}

	/**
	 * New media file will be created. The code will be set to 'cockpitmg-config', catalogversion will be set to
	 * 'default-staged', mime-type will be set to 'text/xml' and the data attribute will be set to a null-byte array. Get
	 * the media by code. If there is no media createConfigFile() will be called.
	 *
	 * @see DefaultMediaCockpitConfigurationPersistenceStrategy#getCockpitNGConfig()
	 * @deprecated since 1811, the code will be removed, use
	 *             {@link DefaultMediaCockpitConfigurationPersistenceStrategy#getCockpitNGConfig()}
	 */
	@Deprecated
	protected MediaModel createConfigFile()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetToDefaults()
	{
		InputStream resourceAsStream = null;
		try
		{
			resourceAsStream = getDefaultCockpitConfigAsStream();
			if (resourceAsStream != null)
			{
				final String defaultCockpitConfig = convertConfigToString(resourceAsStream);
				setConfigAsString(defaultCockpitConfig);
			}
		}
		finally
		{
			IOUtils.closeQuietly(resourceAsStream);
		}

		super.resetToDefaults();
	}

	private InputStream getDefaultCockpitConfigAsStream()
	{
		return ClassLoaderUtils.getCurrentClassLoader(this.getClass()).getResourceAsStream("impex/backoffice-cockpit-config.xml");
	}

	@Override
	protected <C> void putConfigToCache(final ConfigContext context, final Class<C> configType, final C config)
	{
		final String type = context.getAttribute(DefaultConfigContext.CONTEXT_TYPE);
		if (StringUtils.isNotBlank(type))
		{
			try
			{
				if (getTypeService().getTypeForCode(type) instanceof ViewTypeModel)
				{
					return;
				}
			}
			catch (final UnknownIdentifierException uie)
			{
				LOG.debug(uie.getMessage(), uie);
			}
		}
		super.putConfigToCache(context, configType, config);
	}

	@Override
	protected long getCurrentTimeInMillis()
	{
		return getTimeService().getCurrentTime().getTime();
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected MediaService getMediaService()
	{
		return this.mediaService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Required
	@Deprecated
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected ModelService getModelService()
	{
		return this.modelService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected TypeService getTypeService()
	{
		return this.typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected SessionService getSessionService()
	{
		return this.sessionService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected UserService getUserService()
	{
		return this.userService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected TimeService getTimeService()
	{
		return this.timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	protected BackofficeConfigurationMediaHelper getBackofficeConfigurationMediaHelper()
	{
		return backofficeConfigurationMediaHelper;
	}

	/**
	 * @deprecated since 1811
	 */
	@Deprecated
	@Required
	public void setBackofficeConfigurationMediaHelper(final BackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper)
	{
		this.backofficeConfigurationMediaHelper = backofficeConfigurationMediaHelper;
	}
}
