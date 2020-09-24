/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.quickupload;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.datahub.dto.feed.FeedData;

import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicCsvImportClient;
import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicRestClient;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zkoss.util.media.Media;

import com.google.common.collect.Maps;

public class LoadProcessingStrategy extends AbstractProcessingStrategy
{
	private static final String LOAD_IN_PROGRESS = "load.inprogress";
	private static final String IMPORT_FINISHED = "datahub.quickupload.finished.import";

	private DynamicCsvImportClient csvImportClient;

	@Override
	public Map<String, Object> processInternal(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final FeedData selectedFeed = getContextValue(ctx, QuickUploadController.FEED_MODEL_PARAMETER);
		final String selectedTYpe = getContextValue(ctx, QuickUploadController.TYPE_MODEL_PARAMETER);

		final Media uploadedMedia = controller.getValue(QuickUploadController.UPLOADED_MEDIA_MODEL_PARAMETER, Media.class);
		boolean operationStatus = true;
		if (uploadedMedia != null)
		{
			if (uploadedMedia.isBinary())
			{
				operationStatus = csvImportClient.uploadCsv(selectedFeed.getName(), selectedTYpe, uploadedMedia.getStreamData());
			}
			else
			{
				operationStatus = csvImportClient.uploadCsv(selectedFeed.getName(), selectedTYpe, uploadedMedia.getStringData());
			}
		}
		else
		{
			final String rawContent = controller.getValue(QuickUploadController.TEXTAREA_CONTENT_MODEL_PARAMETER, String.class);
			if (StringUtils.isNotBlank(rawContent))
			{
				operationStatus = csvImportClient.uploadCsv(selectedFeed.getName(), selectedTYpe, rawContent);
			}
		}
		final Map<String, Object> results = Maps.newHashMap();
		results.put(OPERATION_STATUS, operationStatus ? "SUCCESS" : "FAILURE");
		return results;
	}

	@Override
	public String getBusyMessage(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		return controller.getLabel(LOAD_IN_PROGRESS);
	}

	@Override
	public void notifyWhenFinished(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final String status = getContextValue(ctx, OPERATION_STATUS);
		notificationService.notifyUser(controller.getWidgetId(), IMPORT_FINISHED,
				NotificationEvent.Level.INFO, getSelectedDataHubServer(ctx).getName(), status);
	}

	@Override
	public String getAlreadyRunning(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		return controller.getLabel(LOAD_IN_PROGRESS);
	}

	@Override
	protected DynamicRestClient restClient()
	{
		return csvImportClient;
	}

	@Override
	public boolean supports(final String key)
	{
		final String LOAD_ID = "load";
		return StringUtils.equals(LOAD_ID, key);
	}

	@Override
	public boolean validate(final QuickUploadController controller, final Map<String, Object> ctx)
	{

		final FeedData selectedFeed = getContextValue(ctx, QuickUploadController.FEED_MODEL_PARAMETER);
		final String selectedTYpe = getContextValue(ctx, QuickUploadController.TYPE_MODEL_PARAMETER);
		boolean valid = true;
		String editorId = StringUtils.EMPTY;
		if (selectedFeed == null)
		{
			editorId = String.format("%s.%s", QuickUploadController.DATAHUB_FEED_TYPECODE,
					QuickUploadController.FEED_MODEL_PARAMETER);
			valid = false;
		}
		else if (StringUtils.isEmpty(selectedTYpe))
		{
			editorId = QuickUploadController.TYPE_MODEL_PARAMETER;
			valid = false;
		}

		if (!valid)
		{
			controller.markEditorError(editorId);
		}

		return valid;
	}

	public void setCsvImportClient(final DynamicCsvImportClient client)
	{
		csvImportClient = client;
	}
}
