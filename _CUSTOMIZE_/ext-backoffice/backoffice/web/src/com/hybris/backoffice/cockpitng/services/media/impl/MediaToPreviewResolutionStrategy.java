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
package com.hybris.backoffice.cockpitng.services.media.impl;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.servicelayer.media.MediaService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.services.media.impl.AbstractPreviewResolutionStrategy;


public class MediaToPreviewResolutionStrategy extends AbstractPreviewResolutionStrategy<MediaModel>
{

	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	private MediaStorageConfigService mediaStorageConfigService;

	private MediaService mediaService;

	@Override
	public String resolvePreviewUrl(final MediaModel target)
	{
		final String url = getMediaService().getUrlForMedia(target);
		if (StringUtils.isNotBlank(url))
		{
			return getMediaURL(target);
		}
		return null;
	}

	@Override
	public String resolveMimeType(final MediaModel target)
	{
		return StringUtils.defaultIfBlank(target.getMime(), StringUtils.EMPTY);
	}

	private String getMediaURL(final MediaModel mediaModel)
	{
		return mediaModel.getURL();
	}

	/**
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	@Required
	public void setMediaStorageConfigService(final MediaStorageConfigService mediaStorageConfigService)
	{
		this.mediaStorageConfigService = mediaStorageConfigService;
	}

	/**
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	protected MediaStorageConfigService getMediaStorageConfigService()
	{
		return mediaStorageConfigService;
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
}
