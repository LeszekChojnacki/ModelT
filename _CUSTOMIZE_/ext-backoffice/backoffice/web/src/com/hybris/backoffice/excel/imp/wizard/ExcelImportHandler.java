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

import de.hybris.platform.media.services.MimeService;
import de.hybris.platform.util.zip.SafeZipEntry;
import de.hybris.platform.validation.enums.Severity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.ExcelConstants;
import com.hybris.backoffice.excel.importing.ExcelImportService;
import com.hybris.backoffice.excel.importing.ExcelImportWorkbookPostProcessor;
import com.hybris.backoffice.excel.jobs.FileContent;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;
import com.hybris.backoffice.excel.validators.util.ExcelValidationResultUtil;
import com.hybris.backoffice.model.ExcelImportCronJobModel;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;
import de.hybris.platform.impex.jalo.cronjob.DefaultCronJobMediaDataHandler;
import de.hybris.platform.util.Config;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;


/**
 * Handler which is used in Excel Import Process' wizard. Handler allows to start import process if uploaded file passes
 * the validation process.
 */
public class ExcelImportHandler extends ExcelWithoutValidationImportHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelImportHandler.class);
	/**
	 * @deprecated since 6.7 no longer used
	 */
	@Deprecated
	public static final String ZIP_EXTENSION = "zip";
	/**
	 * @deprecated since 6.7 no longer used
	 */
	@Deprecated
	public static final String XLSX_EXTENSION = "xlsx";
	private static final String EXCEL_IMPORT_VALIDATION_INCORRECT_FILE_MESSAGE = "excel.import.validation.incorrect.file.description";
	private static final String EXCEL_IMPORT_VALIDATION_INCORRECT_FILE__HEADER_MESSAGE = "excel.import.validation.incorrect.file.header";

	private ExcelImportService excelImportService;
	private MimeService mimeService;
	private ExcelImportWorkbookPostProcessor excelImportWorkbookPostProcessor;

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
		final List<ExcelValidationResult> workbookValidationResult = validateExcel(excelFile, zipFile);
		if (!workbookValidationResult.isEmpty())
		{
			adapter.getWidgetInstanceManager().getModel().setValue(ExcelConstants.EXCEL_IMPORT_VALIDATION_RESULT,
					workbookValidationResult);
			adapter.getWidgetInstanceManager().getModel().setValue(ExcelConstants.EXCEL_HAS_VALIDATION_ERRORS,
					hasValidationErrors(workbookValidationResult));
			adapter.next();
		}
		else
		{
			importExcel(excelFile, zipFile);
			adapter.done();
		}
	}

	protected boolean hasValidationErrors(final Collection<ExcelValidationResult> excelValidationResults)
	{
		return excelValidationResults.stream().map(ExcelValidationResult::getValidationErrors).flatMap(Collection::stream)
				.map(ValidationMessage::getSeverity).anyMatch(severity -> severity == Severity.ERROR);
	}

	/**
	 * @deprecated since 6.7 please use {@link #validateExcel(FileContent, FileContent)}
	 */
	@Deprecated
	protected List<ExcelValidationResult> validateExcel(final FileContent excelFileContent)
	{
		Workbook workbook = null;
		try
		{
			workbook = createWorkbook(excelFileContent.getData());
			return getExcelImportService().validate(workbook);
		}
		finally
		{
			IOUtils.closeQuietly(workbook);
		}
	}

	protected List<ExcelValidationResult> validateExcel(final FileContent excelFileContent, final FileContent zipFileContent)
	{
		Workbook workbook = null;
		try
		{
			workbook = createWorkbook(excelFileContent.getData());

			final List<ExcelValidationResult> validationResults = new ArrayList<>();
			validationResults.addAll(getExcelImportService().validate(workbook, listZipEntries(zipFileContent).orElse(null)));
			if (validationResults.stream().noneMatch(ExcelValidationResult::isWorkbookValidationResult))
			{
				validationResults.addAll(excelImportWorkbookPostProcessor.validate(workbook));
			}
			return ExcelValidationResultUtil.mergeValidationResults(validationResults);
		}
		catch (final RuntimeException ex)
		{
			LOG.info("Could not process Excel file that was uploaded by the user", ex);
			return prepareValidationInfoAboutIncorrectFile();
		}
		finally
		{
			IOUtils.closeQuietly(workbook);
		}
	}

	private List<ExcelValidationResult> prepareValidationInfoAboutIncorrectFile()
	{
		final ExcelValidationResult incorrectFileValidationResult = new ExcelValidationResult(
				new ValidationMessage(EXCEL_IMPORT_VALIDATION_INCORRECT_FILE_MESSAGE));
		incorrectFileValidationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_INCORRECT_FILE__HEADER_MESSAGE));
		incorrectFileValidationResult.setWorkbookValidationResult(true);
		return Collections.singletonList(incorrectFileValidationResult);
	}

	protected void importExcel(final FileContent foundExcelFile, final FileContent foundZipFile)
	{
		final ExcelImportCronJobModel excelImportCronJob = getExcelCronJobService().createImportJob(foundExcelFile, foundZipFile);
		getCronJobService().performCronJob(excelImportCronJob, false);
		getCockpitEventQueue().publishEvent(new DefaultCockpitEvent("updateProcessForCronJob", excelImportCronJob.getCode(), null));
	}

	/**
	 * @deprecated since 6.7 no longer used. Use {@link #toFileContent(FileUploadResult)} to map upload result and
	 *             {@link ExcelImportWizardForm#getExcelFile()} or {@link ExcelImportWizardForm#getZipFile()} to obtain file
	 *             for extension
	 */
	@Deprecated
	protected FileContent findUploadedFileWithExtension(final Set<FileUploadResult> uploadedFiles, final String extension)
	{
		final Optional<FileUploadResult> foundFirst = uploadedFiles.stream().filter(file -> file.getName().endsWith(extension))
				.findFirst();
		return foundFirst.map(fileUploadResult -> new FileContent(fileUploadResult.getData(), fileUploadResult.getContentType(),
				fileUploadResult.getName())).orElse(null);
	}

	protected Workbook createWorkbook(final byte[] fileContent)
	{
		try
		{
			return new XSSFWorkbook(new ByteArrayInputStream(fileContent));
		}
		catch (final IOException ex)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.error("Cannot create workbook", ex);
			}
			return new XSSFWorkbook();
		}
	}

	protected Set<String> getZipEntries(final FileContent zipFile)
	{
		final Collection<Charset> supportedCharsets = getSupportedCharsets();
		for (final Charset charset : supportedCharsets)
		{
			try
			{
				return getFallbackZipEntry(zipFile, charset);
			}
			catch (final IllegalArgumentException e)
			{
				LOG.warn("Cannot unzip the file with {} encoding", charset.name());
				LOG.debug("Reason: ", e);
			}
		}
		return new HashSet<>();
	}

	protected Optional<Set<String>> listZipEntries(final FileContent foundZipFile) {
		if (foundZipFile == null || foundZipFile.getData() == null || !mimeService.isZipRelatedMime(foundZipFile.getContentType())) {
			return Optional.empty();
		}

		return Optional.of(Collections.unmodifiableSet(getZipEntries(foundZipFile)));
	}

	protected Set<String> getFallbackZipEntry(final FileContent zipFile, final Charset charset)
	{
		final Set<String> entries = new HashSet<>();
		try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zipFile.getData()), charset))
		{
			ZipEntry entry;
			while ((entry = zipStream.getNextEntry()) != null)
			{
				entries.add(new SafeZipEntry(entry).getName());
				zipStream.closeEntry();
			}
		}
		catch (final IOException e)
		{
			LOG.warn("Cannot read uploaded zip content ", e);
		}
		return entries;
	}

	protected Collection<Charset> getSupportedCharsets()
	{
		final String baseEncoding = "UTF8";

		return Stream.concat(Stream.of(baseEncoding), Stream.of(getZipFilenameEncodings().split("[,;]")))
				.filter(StringUtils::isNotEmpty).map(String.class::cast).map(StringUtils::lowerCase).map(StringUtils::trim)
				.map(Charset::forName).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	protected String getZipFilenameEncodings()
	{
		return Config.getParameter(DefaultCronJobMediaDataHandler.ZIP_FILENAME_ENCODINGS);
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

	public MimeService getMimeService()
	{
		return mimeService;
	}

	@Required
	public void setMimeService(final MimeService mimeService)
	{
		this.mimeService = mimeService;
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
}
