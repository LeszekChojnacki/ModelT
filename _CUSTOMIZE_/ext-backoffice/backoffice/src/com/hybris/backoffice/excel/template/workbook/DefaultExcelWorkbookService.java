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
package com.hybris.backoffice.excel.template.workbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Default implementation of {@link ExcelWorkbookService}
 */
public class DefaultExcelWorkbookService implements ExcelWorkbookService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultExcelWorkbookService.class);

	private ExcelTemplateConstants.UtilitySheet metaInformationSheet = ExcelTemplateConstants.UtilitySheet.TYPE_SYSTEM;

	@Override
	public Sheet getMetaInformationSheet(@WillNotClose final Workbook workbook)
	{
		return workbook.getSheet(metaInformationSheet.getSheetName());
	}

	@Override
	public Workbook createWorkbook(@WillNotClose final InputStream is)
	{
		try
		{
			return new XSSFWorkbook(is);
		}
		catch (final IOException ex)
		{
			LOG.error("Cannot read excel template. Returning empty workbook", ex);
			return new XSSFWorkbook();
		}
	}

	@Override
	public void addProperty(@WillNotClose final Workbook workbook, @Nonnull final String key, @Nonnull final String value)
	{
		getProperties(workbook).ifPresent(customProperties -> customProperties.addProperty(key, value));
	}

	@Override
	public Optional<String> getProperty(@WillNotClose final Workbook workbook, @Nonnull final String key)
	{
		return getProperties(workbook)//
				.map(customProperties -> customProperties.getProperty(key))//
				.map(CTProperty::getLpwstr);
	}

	@Override
	public Collection<CTProperty> getUnderlyingProperties(final Workbook workbook)
	{
		return getProperties(workbook) //
				.map( //
						customProperties -> customProperties.getUnderlyingProperties().getPropertyList() //
				) //
				.orElse(Collections.emptyList());
	}

	protected Optional<POIXMLProperties.CustomProperties> getProperties(final Workbook workbook)
	{
		return Optional.ofNullable(workbook)//
				.filter(XSSFWorkbook.class::isInstance)//
				.map(XSSFWorkbook.class::cast)//
				.map(XSSFWorkbook::getProperties)//
				.map(POIXMLProperties::getCustomProperties);
	}

	// optional
	public void setMetaInformationSheet(final ExcelTemplateConstants.UtilitySheet metaInformationSheet)
	{
		this.metaInformationSheet = metaInformationSheet;
	}
}
