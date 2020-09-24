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
package com.hybris.backoffice.excel.template.sheet;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;
import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.ExcelSheetNamingStrategy;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;


/**
 * Default implementation of {@link ExcelSheetService}
 */
public class DefaultExcelSheetService implements ExcelSheetService
{

	private Collection<ExcelTemplateConstants.UtilitySheet> excludedSheets = Collections.emptyList();
	private ExcelTemplateConstants.Header headerRowIndex = ExcelTemplateConstants.Header.DISPLAY_NAME;
	private ExcelTemplateConstants.UtilitySheet typeTemplate = ExcelTemplateConstants.UtilitySheet.TYPE_TEMPLATE;

	private AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter;
	private CollectionFormatter collectionFormatter;
	private ExcelCellService excelCellService;
	private ExcelSheetNamingStrategy excelSheetNamingStrategy;
	private ExcelWorkbookService excelWorkbookService;

	@Override
	public Collection<String> getSheetsNames(@WillNotClose final Workbook workbook)
	{
		return IntStream.range(0, workbook.getNumberOfSheets())//
				.mapToObj(workbook::getSheetName)//
				.filter(sheetName -> !ExcelTemplateConstants.UtilitySheet.isUtilitySheet(excludedSheets, sheetName))//
				.collect(ImmutableList.toImmutableList());
	}

	@Override
	public Collection<Sheet> getSheets(@WillNotClose final Workbook workbook)
	{
		return getSheetsNames(workbook).stream()//
				.map(workbook::getSheet)//
				.collect(ImmutableList.toImmutableList());
	}

	@Override
	public Sheet createOrGetTypeSheet(@WillNotClose final Workbook workbook, @Nonnull final String typeCode)
	{
		final String sheetName = excelSheetNamingStrategy.generateName(workbook, typeCode);
		excelWorkbookService.addProperty(workbook, sheetName, typeCode);

		return getSheet(workbook, sheetName).orElseGet(() -> createTypeSheet(workbook, sheetName));
	}

	@Override
	public Sheet createTypeSheet(@WillNotClose final Workbook workbook, @Nonnull final String sheetName)
	{
		final int typeSheetTemplateIndex = workbook.getSheetIndex(typeTemplate.getSheetName());

		final Sheet clonedSheet = workbook.cloneSheet(typeSheetTemplateIndex);
		workbook.setSheetName(workbook.getSheetIndex(clonedSheet), sheetName);
		return clonedSheet;
	}

	@Override
	public Sheet createOrGetUtilitySheet(@WillNotClose final Workbook workbook, @Nonnull final String sheetName)
	{
		return Optional.ofNullable(workbook.getSheet(sheetName)).orElseGet(() -> workbook.createSheet(sheetName));
	}

	@Override
	public int findColumnIndex(final Sheet typeSystemSheet, @Nonnull final Sheet sheet, final ExcelAttribute excelAttribute)
	{
		final String attributeDisplayName = findAttributeDisplayNameInTypeSystemSheet(typeSystemSheet, excelAttribute);
		if (StringUtils.isBlank(attributeDisplayName))
		{
			return -1;
		}
		final Set<String> attributeDisplayNames = collectionFormatter.formatToCollection(attributeDisplayName);
		final Row headerRow = sheet.getRow(headerRowIndex.getIndex());
		for (int i = 0; i <= headerRow.getLastCellNum(); i++)
		{
			final String cellValue = excelCellService.getCellValue(headerRow.getCell(i));
			if (attributeDisplayNames.contains(cellValue) && StringUtils.equals(cellValue,
					attributeNameFormatter.format(getExcelAttributeContext(sheet.getWorkbook(), excelAttribute))))
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public String findTypeCodeForSheetName(@WillNotClose final Workbook workbook, final String sheetName)
	{
		return excelWorkbookService.getProperty(workbook, sheetName).orElse(sheetName);
	}

	@Override
	public String findSheetNameForTypeCode(@WillNotClose final Workbook workbook, final String typeCode)
	{
		final Predicate<CTProperty> equals = property -> StringUtils.equals(property.getLpwstr(), typeCode);
		final Predicate<CTProperty> notBlank = property -> StringUtils.isNotBlank(property.getName());

		return excelWorkbookService.getUnderlyingProperties(workbook) //
				.stream() //
				.filter(equals.and(notBlank)) //
				.findAny() //
				.map(CTProperty::getName) //
				.orElse(typeCode);
	}

	protected String findAttributeDisplayNameInTypeSystemSheet(final Sheet typeSystemSheet, final ExcelAttribute excelAttribute)
	{
		if (typeSystemSheet != null && excelAttribute instanceof ExcelAttributeDescriptorAttribute)
		{
			final ExcelAttributeDescriptorAttribute descAttr = (ExcelAttributeDescriptorAttribute) excelAttribute;

			return IntStream.rangeClosed(0, typeSystemSheet.getLastRowNum()) //
					.mapToObj(typeSystemSheet::getRow) //
					.filter(row -> qualifierEquality(row).and(languageEquality(row)).test(descAttr)) //
					.map(row -> //
					excelCellService.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_DISPLAYED_NAME.getIndex())) //
					) //
					.findFirst() //
					.orElse(StringUtils.EMPTY);
		}
		return StringUtils.EMPTY;
	}

	private Predicate<ExcelAttributeDescriptorAttribute> qualifierEquality(final Row row)
	{
		return attr -> {
			final String cellQualifier = excelCellService
					.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_QUALIFIER.getIndex()));
			final String descQualifier = attr.getAttributeDescriptorModel().getQualifier();
			return StringUtils.equals(cellQualifier, descQualifier);
		};
	}

	private Predicate<ExcelAttributeDescriptorAttribute> languageEquality(final Row row)
	{
		return attr -> {
			final String cellLanguage = excelCellService
					.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_LOC_LANG.getIndex()));
			final String descLanguage = attr.getIsoCode() != null ? attr.getIsoCode() : StringUtils.EMPTY;
			return StringUtils.equals(cellLanguage, descLanguage)
					|| collectionFormatter.formatToCollection(cellLanguage).contains(descLanguage);
		};
	}

	private ExcelAttributeContext<ExcelAttributeDescriptorAttribute> getExcelAttributeContext(final Workbook workbook,
			final ExcelAttribute excelAttribute)
	{
		final String isoCodeKey = "isoCode";
		return DefaultExcelAttributeContext.ofMap((ExcelAttributeDescriptorAttribute) excelAttribute,
				ImmutableMap.of(isoCodeKey, excelWorkbookService.getProperty(workbook, isoCodeKey).orElse(StringUtils.EMPTY)));
	}

	@Required
	public void setCollectionFormatter(final CollectionFormatter collectionFormatter)
	{
		this.collectionFormatter = collectionFormatter;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}

	@Required
	public void setExcelSheetNamingStrategy(final ExcelSheetNamingStrategy excelSheetNamingStrategy)
	{
		this.excelSheetNamingStrategy = excelSheetNamingStrategy;
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
	}

	// optional
	public void setExcludedSheets(final Collection<ExcelTemplateConstants.UtilitySheet> excludedSheets)
	{
		this.excludedSheets = excludedSheets;
	}

	// optional
	public void setTypeTemplate(final ExcelTemplateConstants.UtilitySheet typeTemplate)
	{
		this.typeTemplate = typeTemplate;
	}

	// optional
	public void setHeaderRowIndex(final ExcelTemplateConstants.Header headerRowIndex)
	{
		this.headerRowIndex = headerRowIndex;
	}
}
