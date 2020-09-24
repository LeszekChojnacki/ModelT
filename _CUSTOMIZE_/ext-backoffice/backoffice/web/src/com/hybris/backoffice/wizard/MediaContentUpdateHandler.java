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
package com.hybris.backoffice.wizard;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class MediaContentUpdateHandler implements FlowActionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(MediaContentUpdateHandler.class);
	protected static final String MEDIA_CONTENT_PROPERTY = "mediaContentProperty";
	protected static final String MEDIA_PROPERTY = "mediaProperty";

	private MediaService mediaService;
	private ObjectFacade objectFacade;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> map)
	{
		final FileUploadResult mediaContent = getMediaContent(adapter, map);
		final MediaModel mediaToUpdate = getMediaToUpdate(adapter, map);
		if (mediaContent != null && mediaToUpdate != null)
		{
			if (objectFacade.isModified(mediaToUpdate))
			{
				return;
			}

			mediaToUpdate.setRealFileName(mediaContent.getName());
			mediaToUpdate.setMime(mediaContent.getContentType());
			mediaService.setDataForMedia(mediaToUpdate, mediaContent.getData());
		}
		adapter.done();
	}

	protected MediaModel getMediaToUpdate(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final String mediaProperty = params.get(MEDIA_PROPERTY);
		if (StringUtils.isNotEmpty(mediaProperty))
		{
			return adapter.getWidgetInstanceManager().getModel().getValue(mediaProperty, MediaModel.class);
		}
		else
		{
			LOG.warn("Missing {} param which specifies media to update", MEDIA_PROPERTY);
			return null;
		}
	}

	protected FileUploadResult getMediaContent(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final String mediaProperty = params.get(MEDIA_CONTENT_PROPERTY);
		if (StringUtils.isNotEmpty(mediaProperty))
		{
			return adapter.getWidgetInstanceManager().getModel().getValue(mediaProperty, FileUploadResult.class);
		}
		else
		{
			LOG.warn("Missing {} param which specifies media content", MEDIA_CONTENT_PROPERTY);
			return null;
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

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}
}

