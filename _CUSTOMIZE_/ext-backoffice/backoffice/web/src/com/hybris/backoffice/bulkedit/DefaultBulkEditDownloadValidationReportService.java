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
package com.hybris.backoffice.bulkedit;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Filedownload;

import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.validation.model.ValidationInfo;


public class DefaultBulkEditDownloadValidationReportService implements BulkEditDownloadValidationReportService
{

	private LabelService labelService;

	@Override
	public void downloadReport(final List<ValidationResult> validationResults)
	{
		final String fileContent = generateValidationContent(validationResults);
		triggerDownload(fileContent);
	}

	protected String generateValidationContent(final List<ValidationResult> validationResults)
	{
		final StringBuilder sb = new StringBuilder();
		validationResults.forEach(validationResult -> generateValidationRow(sb, validationResult));
		return sb.toString();
	}

	protected void generateValidationRow(final StringBuilder sb, final ValidationResult validationResult)
	{
		generateValidationRowHeader(sb, validationResult);
		for (final ValidationInfo validationInfo : validationResult.getValidationInfos())
		{
			generateValidationError(sb, validationInfo);
		}
		appendNewLine(sb);
	}

	protected void generateValidationRowHeader(final StringBuilder sb, final ValidationResult validationResult)
	{
		sb.append(labelService.getShortObjectLabel(validationResult.getItem()));
		appendNewLine(sb);
	}

	public void generateValidationError(final StringBuilder sb, final ValidationInfo validationInfo)
	{
		sb.append("\t" + validationInfo.getValidationMessage());
		appendNewLine(sb);
	}

	protected void appendNewLine(final StringBuilder sb)
	{
		sb.append(System.lineSeparator());
	}

	protected void triggerDownload(final String fileContent)
	{
		Filedownload.save(fileContent, MediaType.TEXT_PLAIN, "bulk-edit-validation-report.txt");
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
