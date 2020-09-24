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
package com.hybris.backoffice.excel.jobs;

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.model.ExcelImportCronJobModel;
import com.hybris.backoffice.model.ExcelImportJobModel;


/**
 * Default implementation of {@link ExcelCronJobService}
 */
public class DefaultExcelCronJobService implements ExcelCronJobService
{

	public static final String EXCEL_IMPORT_CRON_JOB_CODE_PREFIX = "ExcelImport";
	private String mediaFolder;
	private MediaService mediaService;
	private ModelService modelService;
	private String cronJobPerformableSpringId;

	@Override
	public ExcelImportCronJobModel createImportJob(final FileContent excelFile, final FileContent referencedContentFile)
	{
		final MediaModel excelMedia = createMedia(excelFile.getData(), excelFile.getName(), excelFile.getContentType());
		final MediaModel referencedContentMedia = referencedContentFile != null ? createMedia(referencedContentFile.getData(),
				referencedContentFile.getName(), referencedContentFile.getContentType()) : null;

		return createCronJob(excelMedia, referencedContentMedia);
	}

	protected MediaModel createMedia(final byte[] data, final String fileName, final String contentType)
	{
		final CatalogUnawareMediaModel mediaModel = getModelService().create(CatalogUnawareMediaModel._TYPECODE);
		mediaModel.setCode(generateId(fileName));
		final MediaFolderModel importFolder = getMediaService().getFolder(getMediaFolder());
		mediaModel.setFolder(importFolder);
		mediaModel.setRealFileName(fileName);
		mediaModel.setMime(contentType);
		getModelService().save(mediaModel);
		getMediaService().setDataForMedia(mediaModel, data);
		return mediaModel;
	}

	protected ExcelImportCronJobModel createCronJob(final MediaModel excelMedia, final MediaModel referencedContentMedia)
	{
		final String id = generateId(excelMedia.getRealFileName());

		final ExcelImportJobModel jobModel = getModelService().create(ExcelImportJobModel.class);
		jobModel.setCode(id);
		jobModel.setSpringId(getCronJobPerformableSpringId());
		getModelService().save(jobModel);

		final ExcelImportCronJobModel cronJobModel = getModelService().create(ExcelImportCronJobModel.class);
		cronJobModel.setCode(id);
		cronJobModel.setActive(Boolean.TRUE);
		cronJobModel.setJob(jobModel);
		cronJobModel.setExcelFile(excelMedia);
		cronJobModel.setReferencedContent(referencedContentMedia);
		getModelService().save(cronJobModel);
		return cronJobModel;
	}

	protected String generateId(final String fileName)
	{
		return String.format("%s%s%s", EXCEL_IMPORT_CRON_JOB_CODE_PREFIX, fileName.replaceAll("[^0-9a-zA-Z]", ""),
				UUID.randomUUID().getMostSignificantBits());
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

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setMediaFolder(final String mediaFolder)
	{
		this.mediaFolder = mediaFolder;
	}

	public String getMediaFolder()
	{
		return mediaFolder;
	}

	public String getCronJobPerformableSpringId()
	{
		return cronJobPerformableSpringId;
	}

	@Required
	public void setCronJobPerformableSpringId(final String cronJobPerformableSpringId)
	{
		this.cronJobPerformableSpringId = cronJobPerformableSpringId;
	}
}
