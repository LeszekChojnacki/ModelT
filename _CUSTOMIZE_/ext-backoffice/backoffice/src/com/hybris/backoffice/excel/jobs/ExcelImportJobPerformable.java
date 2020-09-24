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

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.util.CSVConstants;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.importing.ExcelImportService;
import com.hybris.backoffice.excel.importing.ExcelImportWorkbookPostProcessor;
import com.hybris.backoffice.excel.importing.ImpexConverter;
import com.hybris.backoffice.excel.importing.data.ExcelImportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.model.ExcelImportCronJobModel;


public class ExcelImportJobPerformable extends AbstractJobPerformable<ExcelImportCronJobModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelImportJobPerformable.class);
	private ExcelImportService excelImportService;
	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	private ExcelTemplateService excelTemplateService;
	private ExcelWorkbookService excelWorkbookService;
	private ExcelImportWorkbookPostProcessor excelImportWorkbookPostProcessor;
	private ImpexConverter impexConverter;
	private ImportService importService;
	private MediaService mediaService;
	private Boolean failOnError = true;

	@Override
	public PerformResult perform(final ExcelImportCronJobModel cronJob)
	{
		try
		{
			return mapResult(getImportService().importData(createImportConfig(cronJob)));
		}
		catch (final RuntimeException ex)
		{
			LOG.error("Error occurred while importing excel file", ex);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}
	}

	protected ImportConfig createImportConfig(final ExcelImportCronJobModel cronJob)
	{
		final ImportConfig config = new ImportConfig();
		config.setFailOnError(getFailOnError());
		config.setScript(generateImpexScript(cronJob));
		config.setEnableCodeExecution(Boolean.FALSE);
		if (cronJob.getReferencedContent() != null)
		{
			config.setMediaArchive(new StreamBasedImpExResource(mediaService.getStreamFromMedia(cronJob.getReferencedContent()),
					CSVConstants.HYBRIS_ENCODING));
		}
		return config;
	}

	protected String generateImpexScript(final ExcelImportCronJobModel cronJob)
	{
		final Workbook workbook = getExcelWorkbookService()
				.createWorkbook(getMediaService().getStreamFromMedia(cronJob.getExcelFile()));
		try
		{
			final Impex impex = getExcelImportService().convertToImpex(workbook);
			getExcelImportWorkbookPostProcessor().process(new ExcelImportResult(workbook, impex));
			return getImpexConverter().convert(impex);
		}
		finally
		{
			IOUtils.closeQuietly(workbook);
		}
	}

	protected PerformResult mapResult(final ImportResult importResult)
	{
		if (importResult.isError())
		{
			logImpexResult(importResult);
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}
		if (importResult.isSuccessful())
		{
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		return new PerformResult(CronJobResult.UNKNOWN, CronJobStatus.UNKNOWN);
	}

	protected void logImpexResult(final ImportResult importResult)
	{
		if (importResult == null || importResult.getUnresolvedLines() == null)
		{
			return;
		}
		try (InputStream is = getMediaService().getStreamFromMedia(importResult.getUnresolvedLines()))
		{
			final StringBuilder unresolvedLines = new StringBuilder("	Unresolved impex lines\n");
			IOUtils.readLines(is).forEach(line -> {
				unresolvedLines.append(line);
				unresolvedLines.append("\n");
			});
			if (LOG.isWarnEnabled())
			{
				LOG.warn(unresolvedLines.toString());
			}
		}
		catch (final IOException e)
		{
			LOG.error("Cannot read unresolved lines: {}", e);
		}
	}

	public ExcelImportWorkbookPostProcessor getExcelImportWorkbookPostProcessor()
	{
		return excelImportWorkbookPostProcessor;
	}

	@Required
	public void setExcelImportWorkbookPostProcessor(final ExcelImportWorkbookPostProcessor excelImportWorkbookPostProcessor)
	{
		this.excelImportWorkbookPostProcessor = excelImportWorkbookPostProcessor;
	}

	public ExcelImportService getExcelImportService()
	{
		return excelImportService;
	}

	@Required
	public void setExcelImportService(final ExcelImportService excelImportService)
	{
		this.excelImportService = excelImportService;
	}

	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	public ExcelTemplateService getExcelTemplateService()
	{
		return excelTemplateService;
	}

	/**
	 * @deprecated since 1808
	 */
	@Deprecated
	@Required
	public void setExcelTemplateService(final ExcelTemplateService excelTemplateService)
	{
		this.excelTemplateService = excelTemplateService;
	}

	public ExcelWorkbookService getExcelWorkbookService()
	{
		return excelWorkbookService;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
	}

	public ImpexConverter getImpexConverter()
	{
		return impexConverter;
	}

	@Required
	public void setImpexConverter(final ImpexConverter impexConverter)
	{
		this.impexConverter = impexConverter;
	}

	public ImportService getImportService()
	{
		return importService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
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

	public Boolean getFailOnError()
	{
		return failOnError;
	}

	public void setFailOnError(final Boolean failOnError)
	{
		this.failOnError = failOnError;
	}
}
