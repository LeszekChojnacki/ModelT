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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.media.MediaModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Wizard handler which allows to crete multiple {@link MediaModel} for uploaded content collection. It uses the same
 * parameters as parent class and following one:
 * <ul>
 * <li>append - if true then created medias will be appended to current value</li>
 * </ul>
 */
public class MultiMediaReferenceCreateHandler extends MediaReferenceCreateHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(MultiMediaReferenceCreateHandler.class);
	protected static final String PARAM_APPEND = "append";

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final Optional<Collection> mediaContentValue = getMediaContent(adapter, params, Collection.class);
		if (!mediaContentValue.isPresent() || CollectionUtils.isEmpty(mediaContentValue.get()))
		{
			adapter.done();
			return;
		}

		final List<FileUploadResult> uploadedMediaList = new ArrayList<>(mediaContentValue.get());
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

			final Optional<List<MediaModel>> newMedias = createMediasForContent(uploadedMediaList, adapter, params);

			if (newMedias.isPresent())
			{
				final List<MediaModel> referenceValue = createMediaCollection(adapter, params, getMediaProperty(params));
				referenceValue.addAll(newMedias.get());
				saveReference(referenceParent.get(), mediaProperty, referenceValue, params);
				commitTransaction();
			}
			else
			{
				notifyCreateMediaFailed(toFilesNames(uploadedMediaList));
				rollbackTransaction();
			}
		}
		catch (final Exception ex)
		{
			rollbackTransaction();
			final String fileNames = toFilesNames(uploadedMediaList);
			LOG.warn("Cannot create medias for:" + fileNames, ex);
			notifyCreateMediaFailed(fileNames);
		}
		adapter.done();
	}

	protected Optional<List<MediaModel>> createMediasForContent(final List<FileUploadResult> uploadedMediaList,
			final FlowActionHandlerAdapter adapter, final Map<String, String> params)
	{
		final List<MediaModel> createdMedias = new ArrayList<>();
		for (int index = 0; index < uploadedMediaList.size(); index++)
		{
			final FileUploadResult mediaContent = uploadedMediaList.get(index);
			final String referenceNameSuffix = String.valueOf(index + 1);
			final Optional<MediaModel> mediaReference = createMediaReference(adapter, params, referenceNameSuffix);
			if (mediaReference.isPresent())
			{
				setMediaContent(mediaContent, mediaReference.get());
				createdMedias.add(mediaReference.get());
			}
			else
			{
				return Optional.empty();
			}
		}
		return Optional.of(createdMedias);
	}

	protected List<MediaModel> createMediaCollection(final FlowActionHandlerAdapter adapter, final Map<String, String> params,
			final String mediaProperty)
	{
		final List<MediaModel> createdMedias = new ArrayList<>();
		if (isAppendMode(params))
		{
			getPropertyValue(adapter, mediaProperty, Collection.class).ifPresent(createdMedias::addAll);
		}
		return createdMedias;
	}

	protected boolean isAppendMode(final Map<String, String> params)
	{
		return Boolean.valueOf(params.getOrDefault(PARAM_APPEND, Boolean.FALSE.toString()));
	}

	protected String toFilesNames(final Collection<FileUploadResult> mediasContent)
	{
		return mediasContent.stream().map(FileUploadResult::getName).collect(Collectors.joining(", "));
	}
}
