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
package com.hybris.backoffice.excel.template.header;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableList;
import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.data.SelectedAttributeQualifier;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;
import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;


/**
 * Default implementation of {@link ExcelHeaderService}
 */
public class DefaultExcelHeaderService implements ExcelHeaderService
{

	private static final Pattern LANG_EXTRACT_PATTERN = Pattern.compile(".+\\[(.+)]"); // e.g.: description[en]

	private ExcelTemplateConstants.Header headerRowIndex = ExcelTemplateConstants.Header.DISPLAY_NAME;
	private ExcelTemplateConstants.Header referencePatternRowIndex = ExcelTemplateConstants.Header.REFERENCE_PATTERN;
	private ExcelTemplateConstants.Header defaultValueIndex = ExcelTemplateConstants.Header.DEFAULT_VALUE;

	private ExcelCellService excelCellService;
	private ExcelSheetService excelSheetService;

	private AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter;
	private CollectionFormatter collectionFormatter;
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	private TypeService typeService;

	@Override
	public Collection<SelectedAttribute> getHeaders(final Sheet metaInformationSheet, final Sheet typeSheet)
	{
		if (typeSheet.getLastRowNum() <= defaultValueIndex.getIndex())
		{
			return Collections.emptyList();
		}

		final Collection<SelectedAttribute> selectedAttributes = new ArrayList<>();
		final Row headerRow = typeSheet.getRow(headerRowIndex.getIndex());
		final Row valuesRow = typeSheet.getRow(defaultValueIndex.getIndex());
		for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++)
		{
			final String headerValue = excelCellService.getCellValue(headerRow.getCell(columnIndex));
			if (StringUtils.isNotBlank(headerValue))
			{
				final String typeCode = excelSheetService.findTypeCodeForSheetName(typeSheet.getWorkbook(), typeSheet.getSheetName());
				final Optional<Row> typeSystemRow = findTypeSystemRowForGivenHeader(metaInformationSheet, typeCode, headerValue);
				if (typeSystemRow.isPresent())
				{
					final Row row = typeSystemRow.get();
					final AttributeDescriptorModel attributeDescriptor = loadAttributeDescriptor(row, typeCode);
					final SelectedAttribute selectedAttribute = new SelectedAttribute(loadIsoCode(row, headerValue),
							attributeDescriptor);
					excelTranslatorRegistry.getTranslator(attributeDescriptor).ifPresent(
							translator -> selectedAttribute.setReferenceFormat(translator.referenceFormat(attributeDescriptor)));
					selectedAttribute.setDefaultValues(excelCellService.getCellValue(valuesRow.getCell(columnIndex)));
					selectedAttributes.add(selectedAttribute);
				}
			}
		}

		return ImmutableList.copyOf(selectedAttributes);
	}

	@Override
	public Collection<String> getHeaderDisplayNames(final Sheet sheet)
	{
		final List<String> attributeDisplayNames = new ArrayList<>();

		final Row headerRow = sheet.getRow(headerRowIndex.getIndex());
		for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++)
		{
			final String headerValue = excelCellService.getCellValue(headerRow.getCell(columnIndex));
			if (StringUtils.isNotBlank(headerValue))
			{
				attributeDisplayNames.add(headerValue);
			}
		}

		return attributeDisplayNames;
	}

	@Override
	public Collection<SelectedAttributeQualifier> getSelectedAttributesQualifiers(final Sheet metaInformationSheet,
			final Sheet typeSheet)
	{
		if (typeSheet.getLastRowNum() <= defaultValueIndex.getIndex())
		{
			return Collections.emptyList();
		}

		final Collection<SelectedAttributeQualifier> selectedAttributesQualifiers = new ArrayList<>();
		final Row headerRow = typeSheet.getRow(headerRowIndex.getIndex());
		for (int columnIndex = 0; columnIndex < headerRow.getLastCellNum(); columnIndex++)
		{
			final String headerValue = excelCellService.getCellValue(headerRow.getCell(columnIndex));
			final String headerValueWithoutMetadata = getHeaderValueWithoutSpecialMarks(headerValue);
			if (StringUtils.isNotBlank(headerValue))
			{
				final Optional<Row> typeSystemRow = findTypeSystemRowForGivenHeader(metaInformationSheet, typeSheet.getSheetName(),
						headerValue);
				if (typeSystemRow.isPresent())
				{
					final Row row = typeSystemRow.get();
					selectedAttributesQualifiers.add(new SelectedAttributeQualifier(headerValueWithoutMetadata,
							excelCellService.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_QUALIFIER.getIndex()))));
				}
				else
				{
					selectedAttributesQualifiers.add(new SelectedAttributeQualifier(headerValueWithoutMetadata, null));
				}
			}
		}

		return ImmutableList.copyOf(selectedAttributesQualifiers);
	}

	@Override
	public void insertAttributeHeader(final Sheet sheet, final ExcelAttribute excelAttribute, final int columnIndex)
	{
		final String nameToDisplay = attributeNameFormatter
				.format(DefaultExcelAttributeContext.ofExcelAttribute((ExcelAttributeDescriptorAttribute) excelAttribute));
		final Row row = sheet.getRow(headerRowIndex.getIndex());
		final Cell cell = row.createCell(row.getFirstCellNum() + columnIndex);
		excelCellService.insertAttributeValue(cell, nameToDisplay);

		final Row patternRow = sheet.getRow(referencePatternRowIndex.getIndex());
		final Cell patternCell = patternRow.getCell(row.getFirstCellNum() + columnIndex);
		patternCell.setCellFormula(patternCell.getCellFormula());
	}

	@Override
	public void insertAttributesHeader(final Sheet sheet, final Collection<? extends ExcelAttribute> excelAttributes)
	{
		int idx = 0;
		for (final ExcelAttribute attribute : excelAttributes)
		{
			insertAttributeHeader(sheet, attribute, idx++);
		}
	}

	protected Optional<Row> findTypeSystemRowForGivenHeader(final Sheet metaInformationSheet, final String typeCode,
			final String header)
	{
		final Predicate<Row> isRowForGivenTypeCode = row -> collectionFormatter
				.formatToCollection(
						excelCellService.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.TYPE_CODE.getIndex())))
				.contains(typeCode);

		final Predicate<Row> isRowForGivenHeader = row -> collectionFormatter
				.formatToCollection(
						excelCellService.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_DISPLAYED_NAME.getIndex())))
				.contains(header);

		return IntStream.rangeClosed(0, metaInformationSheet.getLastRowNum()) //
				.mapToObj(metaInformationSheet::getRow) //
				.filter(isRowForGivenHeader.and(isRowForGivenTypeCode)) //
				.findFirst();
	}

	protected String loadIsoCode(final Row row, final String header)
	{
		final boolean isLocalized = BooleanUtils
				.toBoolean(excelCellService.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_LOCALIZED.getIndex())));
		if (isLocalized)
		{
			final Matcher matcher = LANG_EXTRACT_PATTERN.matcher(header);
			if (matcher.find())
			{
				return matcher.group(1);
			}
		}
		return null;
	}

	protected AttributeDescriptorModel loadAttributeDescriptor(final Row row, final String typeCode)
	{
		final String qualifier = excelCellService
				.getCellValue(row.getCell(ExcelTemplateConstants.TypeSystem.ATTR_QUALIFIER.getIndex()));
		return typeService.getAttributeDescriptor(typeCode, qualifier);
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setCollectionFormatter(final CollectionFormatter collectionFormatter)
	{
		this.collectionFormatter = collectionFormatter;
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}

	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}

	// optional
	public void setHeaderRowIndex(final ExcelTemplateConstants.Header headerRowIndex)
	{
		this.headerRowIndex = headerRowIndex;
	}

	// optional
	public void setReferencePatternRowIndex(final ExcelTemplateConstants.Header referencePatternRowIndex)
	{
		this.referencePatternRowIndex = referencePatternRowIndex;
	}

	// optional
	public void setDefaultValueIndex(final ExcelTemplateConstants.Header defaultValueIndex)
	{
		this.defaultValueIndex = defaultValueIndex;
	}
}
