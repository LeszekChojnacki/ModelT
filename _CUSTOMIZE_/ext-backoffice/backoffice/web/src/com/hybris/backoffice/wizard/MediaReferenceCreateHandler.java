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

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.tx.Transaction;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.expression.ExpressionResolverFactory;
import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Wizard handler which allows to crete single {@link MediaModel} for uploaded content. It accepts following parameters
 * <ul>
 * <li>mediaContentProperty - defines where in model uploaded content is stored. Value is mandatory</li>
 * <li>mediaProperty - defines property in the model where created media should be added. e.g. newProduct.picture. Value
 * is mandatory</li>
 * <li>saveParentObject - if defined parent object will be saved after the reference is set</li>
 * <li>mediaType - defines type of media to be created. It has to be assignable to {@link MediaModel}. By default
 * {@link MediaModel#_TYPECODE} is used</li>
 * <li>mediaCodeExpression - expression which will be used to create media code. It will be called on widget model. e.g.
 * newProduct.code+'_picture'. If code is already used suffix with randomly generated alphanumeric will be added at the
 * end</li>
 * <li>catalogVersionProperty - defines where in model catalog version is stored. It will be used to create media. Value
 * is mandatory for type not assignable to {@link CatalogUnawareMediaModel}</li>
 * <li>mediaFolderProperty - defines where in model {@link MediaFolderModel} is stored. It will be used to create
 * media</li>
 * <li>mediaContainerProperty - defines where in model {@link MediaContainerModel} is stored. It will be used to create
 * media</li>
 * <li>mediaFormatProperty - defines where in model {@link MediaFormatModel} is stored. It will be used to create
 * media</li>
 * </ul>
 * In case media cannot be created or assigned operation will be rollback and user will be notified that media for
 * uploaded content cannot be created.
 */
public class MediaReferenceCreateHandler implements FlowActionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(MediaContentUpdateHandler.class);
	protected static final String PARAM_MEDIA_CONTENT_PROPERTY = "mediaContentProperty";
	protected static final String PARAM_MEDIA_PROPERTY = "mediaProperty";
	protected static final String PARAM_MEDIA_CODE_EXP = "mediaCodeExpression";
	protected static final String PARAM_CATALOG_VERSION = "catalogVersionProperty";
	protected static final String PARAM_MEDIA_FOLDER = "mediaFolderProperty";
	protected static final String PARAM_MEDIA_CONTAINER = "mediaContainerProperty";
	protected static final String PARAM_MEDIA_FORMAT = "mediaFormatProperty";
	protected static final String PARAM_MEDIA_TYPE = "mediaType";
	protected static final String PARAM_SAVE_PARENT_OBJECT = "saveParentObject";
	protected static final String NOTIFICATION_SOURCE = "mediaReferenceHandler";
	protected static final String EVENT_CREATE_MEDIA_REFERENCE = "createMediaReference";

	private MediaService mediaService;
	private ObjectFacade objectFacade;
	private ExpressionResolverFactory expressionResolverFactory;
	private TypeService typeService;
	private NotificationService notificationService;
	private int retryCount;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final Optional<FileUploadResult> mediaContent = getMediaContent(adapter, params, FileUploadResult.class);
		if (!mediaContent.isPresent())
		{
			adapter.done();
			return;
		}


		final String mediaProperty = getMediaProperty(params);
		final Optional<ItemModel> referenceParent = getReferenceParent(adapter, mediaProperty);

		if (!referenceParent.isPresent())
		{
			return;
		}

		if (isSaveParentObjectEnabled(params) && getObjectFacade().isModified(referenceParent.get()))
		{
			LOG.warn("Parent object save cannot be performed on not persisted object");
			return;
		}

		try
		{
			beginTransaction();

			final Optional<MediaModel> mediaReference = createMediaReference(adapter, params);
			if (mediaReference.isPresent())
			{
				setMediaContent(mediaContent.get(), mediaReference.get());
				saveReference(referenceParent.get(), mediaProperty, mediaReference.get(), params);
			}
			else
			{
				notifyCreateMediaFailed(mediaContent.get().getName());
			}
			commitTransaction();
		}
		catch (final Exception ex)
		{
			rollbackTransaction();
			LOG.warn("Cannot create media reference:" + mediaContent.get().getName(), ex);
			notifyCreateMediaFailed(mediaContent.get().getName());
		}
		adapter.done();

	}

	protected void saveReference(final ItemModel referenceParent, final String mediaProperty, final Object reference,
			final Map<String, String> params) throws ObjectSavingException
	{
		expressionResolverFactory.createResolver().setValue(referenceParent, getReferenceProperty(mediaProperty), reference);
		if (isSaveParentObjectEnabled(params))
		{
			objectFacade.save(referenceParent);
		}
	}

	protected String getReferenceProperty(final String mediaProperty)
	{
		final ObjectValuePath root = ObjectValuePath.parse(mediaProperty);
		return root.getRelative(root.getParent()).buildPath();
	}

	protected Optional<MediaModel> createMediaReference(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		return createMediaReference(adapter, params, StringUtils.EMPTY);
	}

	protected Optional<MediaModel> createMediaReference(final FlowActionHandlerAdapter adapter, final Map<String, String> params,
			final String suffix)
	{
		final int maxAttempts = getRetryCount();
		for (int attempt = 0; attempt < maxAttempts; ++attempt)
		{
			final String code = generateMediaCode(adapter, params, suffix, attempt);
			final Optional<MediaModel> media = tryToCreateMedia(adapter, params, code);
			if (media.isPresent())
			{
				return media;
			}
		}
		return Optional.empty();
	}

	protected Optional<MediaModel> tryToCreateMedia(final FlowActionHandlerAdapter adapter, final Map<String, String> params,
			final String code)
	{
		try
		{
			final String mediaType = getMediaType(params);
			final Optional<CatalogVersionModel> catalogVersion = getCatalogVersion(adapter, params);

			if (!isCatalogVersionRequired(mediaType) || catalogVersion.isPresent())
			{
				final MediaModel media = objectFacade.create(mediaType);
				media.setCode(code);
				getFolder(adapter, params).ifPresent(media::setFolder);
				getMediaContainer(adapter, params).ifPresent(media::setMediaContainer);
				getMediaFormat(adapter, params).ifPresent(media::setMediaFormat);
				catalogVersion.ifPresent(media::setCatalogVersion);
				objectFacade.save(media);
				return Optional.of(media);
			}
		}
		catch (final Exception ex)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Cannot create media with code:" + code, ex);
			}
		}
		return Optional.empty();
	}

	protected boolean isCatalogVersionRequired(final String mediaType)
	{
		return !typeService.isAssignableFrom(CatalogUnawareMediaModel._TYPECODE, mediaType);
	}

	protected String getMediaType(final Map<String, String> params)
	{
		final String mediaType = params.get(PARAM_MEDIA_TYPE);
		if (StringUtils.isNotEmpty(mediaType))
		{
			if (typeService.isAssignableFrom(MediaModel._TYPECODE, mediaType))
			{
				return mediaType;
			}
			else
			{
				LOG.warn("Configured media type:{} does not extent Media", mediaType);
			}
		}
		return MediaModel._TYPECODE;
	}

	protected void setMediaContent(final FileUploadResult content, final MediaModel media)
	{
		media.setRealFileName(content.getName());
		media.setMime(content.getContentType());
		mediaService.setDataForMedia(media, content.getData());
	}

	protected String generateMediaCode(final FlowActionHandlerAdapter adapter, final Map<String, String> params,
			final String suffix, final long attempt)
	{
		final String code = getCode(adapter, params);
		return attempt > 0 ? String.format("%s%s_%s", code, suffix, RandomStringUtils.random(3, true, false)) : code.concat(suffix);
	}

	private String getCode(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final String mediaCodePrefix = params.get(PARAM_MEDIA_CODE_EXP);
		return getPropertyValue(adapter, mediaCodePrefix, String.class).orElse(StringUtils.EMPTY);
	}

	protected Optional<MediaFormatModel> getMediaFormat(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		return getPropertyValue(adapter, params.get(PARAM_MEDIA_FORMAT), MediaFormatModel.class);
	}

	protected Optional<MediaContainerModel> getMediaContainer(final FlowActionHandlerAdapter adapter,
			final Map<String, String> params)
	{
		return getPropertyValue(adapter, params.get(PARAM_MEDIA_CONTAINER), MediaContainerModel.class);
	}

	protected Optional<MediaFolderModel> getFolder(final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		return getPropertyValue(adapter, params.get(PARAM_MEDIA_FOLDER), MediaFolderModel.class);
	}

	protected Optional<CatalogVersionModel> getCatalogVersion(final FlowActionHandlerAdapter adapter,
			final Map<String, String> params)
	{
		final String property = params.get(PARAM_CATALOG_VERSION);
		Optional<CatalogVersionModel> catalogVersion = Optional.empty();
		if (StringUtils.isNotEmpty(property))
		{
			catalogVersion = getPropertyValue(adapter, property, CatalogVersionModel.class);
		}

		if (!catalogVersion.isPresent() && isCatalogVersionRequired(getMediaType(params)))
		{
			LOG.warn("Missing catalog version under property:'{}'", property);
		}
		return catalogVersion;
	}

	protected <T> Optional<T> getPropertyValue(final FlowActionHandlerAdapter adapter, final String property, final Class<T> clazz)
	{
		if (StringUtils.isNotBlank(property))
		{
			return Optional.ofNullable(adapter.getWidgetInstanceManager().getModel().getValue(property, clazz));
		}
		return Optional.empty();
	}

	protected <T extends ItemModel> Optional<T> getReferenceParent(final FlowActionHandlerAdapter adapter,
			final String mediaProperty)
	{
		if (mediaProperty != null)
		{
			final ObjectValuePath objectValuePath = ObjectValuePath.parse(mediaProperty);
			if (objectValuePath != null)
			{
				final ObjectValuePath parent = objectValuePath.getParent();
				return (Optional<T>) getPropertyValue(adapter, parent.buildPath(), ItemModel.class);
			}
		}
		return Optional.empty();
	}

	protected String getMediaProperty(final Map<String, String> params)
	{
		final String mediaProperty = params.get(PARAM_MEDIA_PROPERTY);
		if (StringUtils.isEmpty(mediaProperty))
		{
			LOG.warn("Missing {} param which specifies media to update", PARAM_MEDIA_PROPERTY);
		}
		return mediaProperty;
	}

	protected <T> Optional<T> getMediaContent(final FlowActionHandlerAdapter adapter, final Map<String, String> params,
			final Class<T> clazz)
	{
		final String mediaProperty = params.get(PARAM_MEDIA_CONTENT_PROPERTY);
		if (mediaProperty == null)
		{
			LOG.warn("Missing {} param which specifies media content", PARAM_MEDIA_CONTENT_PROPERTY);
		}
		return getPropertyValue(adapter, mediaProperty, clazz);
	}

	protected void notifyCreateMediaFailed(final String fileName)
	{
		getNotificationService().notifyUser(NOTIFICATION_SOURCE, EVENT_CREATE_MEDIA_REFERENCE, NotificationEvent.Level.FAILURE,
				fileName);
	}

	protected boolean isSaveParentObjectEnabled(final Map<String, String> params)
	{
		return Boolean.valueOf(params.get(PARAM_SAVE_PARENT_OBJECT));
	}

	protected void rollbackTransaction()
	{
		Transaction.current().rollback();
	}

	protected void commitTransaction()
	{
		Transaction.current().commit();
	}

	protected void beginTransaction()
	{
		Transaction.current().begin();
	}

	public ExpressionResolverFactory getExpressionResolverFactory()
	{
		return expressionResolverFactory;
	}

	@Required
	public void setExpressionResolverFactory(final ExpressionResolverFactory expressionResolverFactory)
	{
		this.expressionResolverFactory = expressionResolverFactory;
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

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public void setRetryCount(final int retryCount)
	{
		this.retryCount = retryCount;
	}

	public int getRetryCount()
	{
		return retryCount > 0 ? retryCount : 10;
	}
}
