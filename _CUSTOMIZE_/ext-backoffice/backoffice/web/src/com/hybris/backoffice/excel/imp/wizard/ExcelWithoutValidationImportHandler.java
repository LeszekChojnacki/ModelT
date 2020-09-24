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
package com.hybris.backoffice.excel.imp.wizard;

import de.hybris.platform.servicelayer.cronjob.CronJobService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.jobs.ExcelCronJobService;
import com.hybris.backoffice.excel.jobs.FileContent;
import com.hybris.backoffice.model.ExcelImportCronJobModel;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class ExcelWithoutValidationImportHandler implements FlowActionHandler
{

	private ExcelCronJobService excelCronJobService;
	private CronJobService cronJobService;
	private CockpitEventQueue cockpitEventQueue;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final ExcelImportWizardForm data = adapter.getWidgetInstanceManager().getModel()
				.getValue(ExcelConstants.EXCEL_FORM_PROPERTY, ExcelImportWizardForm.class);

		if (data == null)
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_IMPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_EXCEL_FORM_IN_MODEL, NotificationEvent.Level.FAILURE);
			return;
		}

		final FileContent excelFile = toFileContent(data.getExcelFile());
		if (excelFile == null)
		{
			getNotificationService().notifyUser(ExcelConstants.NOTIFICATION_SOURCE_EXCEL_IMPORT,
					ExcelConstants.NOTIFICATION_EVENT_TYPE_MISSING_EXCEL_FILE, NotificationEvent.Level.FAILURE);
			return;
		}

		final FileContent zipFile = toFileContent(data.getZipFile());
		importExcel(excelFile, zipFile);
		adapter.done();
	}

	protected void importExcel(final FileContent foundExcelFile, final FileContent foundZipFile)
	{
		final ExcelImportCronJobModel excelImportCronJob = getExcelCronJobService().createImportJob(foundExcelFile, foundZipFile);
		getCronJobService().performCronJob(excelImportCronJob, false);
		getCockpitEventQueue().publishEvent(new DefaultCockpitEvent("updateProcessForCronJob", excelImportCronJob.getCode(), null));
	}

	protected FileContent toFileContent(final FileUploadResult uploadResult)
	{
		return uploadResult != null ? new FileContent(uploadResult.getData(), uploadResult.getContentType(), uploadResult.getName())
				: null;
	}

	public ExcelCronJobService getExcelCronJobService()
	{
		return excelCronJobService;
	}

	@Required
	public void setExcelCronJobService(final ExcelCronJobService excelCronJobService)
	{
		this.excelCronJobService = excelCronJobService;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	public CockpitEventQueue getCockpitEventQueue()
	{
		return cockpitEventQueue;
	}

	@Required
	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
