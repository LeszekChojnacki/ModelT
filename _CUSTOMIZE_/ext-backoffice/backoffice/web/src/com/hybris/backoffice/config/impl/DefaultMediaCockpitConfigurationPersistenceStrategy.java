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
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.tx.Transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultCockpitConfigurationPersistenceStrategy;


public class DefaultMediaCockpitConfigurationPersistenceStrategy extends DefaultCockpitConfigurationPersistenceStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMediaCockpitConfigurationPersistenceStrategy.class);

	private static final String COCKPITNG_CONFIG = "cockpitng-config";

	private static final String MIME_TYPE = "text/xml";

	private MediaService mediaService;

	private SessionService sessionService;

	private UserService userService;

	private BackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper;

	@Override
	public InputStream getConfigurationInputStream()
	{
		InputStream inputStream;
		try
		{
			final MediaModel media = getCockpitNGConfig();
			inputStream = getInputStreamForMedia(media);
			if (inputStream == null)
			{
				inputStream = getSessionService().executeInLocalView(new SessionExecutionBody()
				{
					@Override
					public Object execute()
					{
						final byte[] data = new byte[0];
						getMediaService().setDataForMedia(media, data);
						return getMediaService().getStreamFromMedia(media);
					}
				}, getUserService().getAdminUser());
			}
		}
		catch (final CockpitConfigurationException ex)
		{
			LOG.error("Error while enquiring cockpit-config media", ex);
			inputStream = new ByteArrayInputStream(new byte[0]);
		}
		return inputStream;
	}



	@Override
	public OutputStream getConfigurationOutputStream()
	{
		return new ByteArrayOutputStream()
		{
			@Override
			public void close() throws IOException
			{
				super.close();
				saveConfig(this);
			}
		};
	}

	private void saveConfig(final ByteArrayOutputStream configBuffer)
	{
		getSessionService().executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				final Transaction tx = Transaction.current();

				boolean success = false;
				try
				{
					tx.begin();
					final MediaModel cockpitNGConfig = getCockpitNGConfig();
					getMediaService().setDataForMedia(cockpitNGConfig, configBuffer.toByteArray());
					success = true;
				}
				catch (final ModelSavingException e)
				{
					LOG.error("Could not save configuration", e);
				}
				catch (final CockpitConfigurationException e)
				{
					LOG.error("Error while enquiring config media", e);
				}
				finally
				{
					if (success)
					{
						tx.commit();
					}
					else
					{
						tx.rollback();
					}
				}
			}
		}, getUserService().getAdminUser());
	}

	protected InputStream getInputStreamForMedia(final MediaModel media)
	{
		try
		{
			return getSessionService().executeInLocalView(new SessionExecutionBody()
			{
				@Override
				public Object execute()
				{
					return getMediaService().getStreamFromMedia(media);
				}
			}, getUserService().getAdminUser());

		}
		catch (final NoDataAvailableException e)
		{
			LOG.info("No data for media: {}", media.getCode());

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Error getting data for media: " + media.getCode(), e);
			}
		}
		return null;
	}

	/*
	 * Get the media by code. If there is no media createConfigFile() will be called.
	 */
	protected MediaModel getCockpitNGConfig() throws CockpitConfigurationException
	{
		final MediaModel config = getBackofficeConfigurationMediaHelper().findOrCreateWidgetsConfigMedia(COCKPITNG_CONFIG,
				MIME_TYPE);

		if (config == null)
		{
			throw new CockpitConfigurationException();
		}

		return config;
	}

	@Override
	public long getLastModification()
	{
		final MediaModel mediaModel;
		try
		{
			mediaModel = getCockpitNGConfig();

			if (mediaModel == null || mediaModel.getModifiedtime() == null)
			{
				return 0L;
			}
			else
			{
				return mediaModel.getModifiedtime().getTime();
			}
		}
		catch (final CockpitConfigurationException e)
		{
			LOG.error("Error while enquiring config media", e);

			return 0;
		}
	}

	public MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public BackofficeConfigurationMediaHelper getBackofficeConfigurationMediaHelper()
	{
		return backofficeConfigurationMediaHelper;
	}

	@Required
	public void setBackofficeConfigurationMediaHelper(final BackofficeConfigurationMediaHelper backofficeConfigurationMediaHelper)
	{
		this.backofficeConfigurationMediaHelper = backofficeConfigurationMediaHelper;
	}
}
