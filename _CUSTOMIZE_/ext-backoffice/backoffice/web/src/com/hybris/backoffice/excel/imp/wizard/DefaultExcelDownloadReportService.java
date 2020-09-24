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

import java.io.Serializable;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Filedownload;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;

/**
 * Default implementation of {@link ExcelDownloadReportService}
 */
public class DefaultExcelDownloadReportService implements ExcelDownloadReportService
{

	@Override
	public void downloadReport(final Collection<ExcelValidationResult> excelValidationResults)
	{
		final String fileContent = generateValidationContent(excelValidationResults);
		triggerDownload(fileContent);
	}

	protected String generateValidationContent(final Collection<ExcelValidationResult> validationWorkbookResults)
	{
		final StringBuilder sb = new StringBuilder();
		validationWorkbookResults.forEach(excelValidationResult -> generateValidationRow(sb, excelValidationResult));
		return sb.toString();
	}

	protected void generateValidationRow(final StringBuilder sb, final ExcelValidationResult value)
	{
		generateValidationRowHeader(sb, value);
		for (final ValidationMessage validationMessage : value.getValidationErrors())
		{
			generateValidationError(sb, validationMessage);
		}
		appendNewLine(sb);
	}

	protected void generateValidationRowHeader(final StringBuilder sb, final ExcelValidationResult excelValidationResult)
	{
		sb.append(getLabel(excelValidationResult.getHeader().getMessageKey(), excelValidationResult.getHeader().getParams()));
		appendNewLine(sb);
	}

	public void generateValidationError(final StringBuilder sb, final ValidationMessage validationMessage)
	{
		final Object selectedAttributeDisplayedName = validationMessage
				.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.SELECTED_ATTRIBUTE_DISPLAYED_NAME_KEY);
		if (selectedAttributeDisplayedName != null && StringUtils.isNotBlank(selectedAttributeDisplayedName.toString()))
		{
			sb.append(String.format("\t[%s]:", selectedAttributeDisplayedName));
		}
		sb.append(String.format("%s", getLabel(validationMessage.getMessageKey(), validationMessage.getParams())));
		appendNewLine(sb);
	}

	protected String getLabel(final String key, final Serializable... params)
	{
		final String label = Labels.getLabel(key, params);
		return label != null ? label : key;
	}

	protected void appendNewLine(final StringBuilder sb)
	{
		sb.append(System.lineSeparator());
	}

	protected void triggerDownload(final String fileContent)
	{
		Filedownload.save(fileContent, MediaType.TEXT_PLAIN, "excel-validation-report.txt");
	}

}
