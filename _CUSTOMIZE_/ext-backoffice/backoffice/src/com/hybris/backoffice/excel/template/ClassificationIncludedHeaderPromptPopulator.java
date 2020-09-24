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
package com.hybris.backoffice.excel.template;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.ExcelCellPopulator;
import com.hybris.backoffice.excel.template.populator.ExcelSheetPopulator;


/**
 * Populates HEADER_PROMPT sheet - including classification attributes
 */
public class ClassificationIncludedHeaderPromptPopulator implements ExcelSheetPopulator
{

	private ExcelCellService excelCellService;

	private Map<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelAttributeDescriptorAttribute>> excelAttributeDescriptorPopulators;
	private Map<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelClassificationAttribute>> excelClassificationPopulators;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(@Nonnull final ExcelExportResult excelExportResult)
	{
		final List<ExcelAttributeDescriptorAttribute> descriptorAttributes = excelExportResult.getAvailableAdditionalAttributes()
				.stream() //
				.filter(ExcelAttributeDescriptorAttribute.class::isInstance) //
				.map(ExcelAttributeDescriptorAttribute.class::cast) //
				.sorted(Comparator.comparing(this::getAttributeDescriptorName))//
				.collect(Collectors.toList());

		final List<ExcelClassificationAttribute> classificationAttributes = excelExportResult.getSelectedAdditionalAttributes()
				.stream() //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.collect(Collectors.toList());

		final Map<String, List<ExcelAttributeDescriptorAttribute>> attrs = descriptorAttributes.stream()
				.collect(Collectors.groupingBy(e -> e.getAttributeDescriptorModel().getEnclosingType().getCode()));


		final Sheet sheet = getSheet(excelExportResult.getWorkbook());
		attrs.forEach((type, descriptors) -> {
			final String typeCode = populateAttributesBasedOnAttributeDescriptors(sheet, descriptors);
			populateAttributesBasedOnClassification(sheet, classificationAttributes, typeCode);
		});

	}

	/**
	 * Populates given sheet with list of attributes based on {@link AttributeDescriptorModel}
	 *
	 * @param sheet
	 *           to populate
	 * @param descriptors
	 *           source of population
	 * @return typeCode of sheet being in population. It is necessary for classification population process.
	 */
	private String populateAttributesBasedOnAttributeDescriptors(final Sheet sheet,
			final List<ExcelAttributeDescriptorAttribute> descriptors)
	{
		String typeCode = StringUtils.EMPTY;
		for (final ExcelAttributeDescriptorAttribute descriptor : descriptors)
		{
			final Row row = appendRow(sheet);
			for (final Map.Entry<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelAttributeDescriptorAttribute>> entry : excelAttributeDescriptorPopulators
					.entrySet())
			{
				final Map<String, Object> context = new HashMap<>();
				context.put(DefaultExcelAttributeContext.EXCEL_ATTRIBUTE, descriptor);

				final String cellValue = entry.getValue().apply(DefaultExcelAttributeContext.ofMap(descriptor, context));

				if (entry.getKey().getIndex() == ExcelTemplateConstants.TypeSystem.TYPE_CODE.getIndex())
				{
					typeCode = cellValue;
				}

				row.createCell(entry.getKey().getIndex()).setCellValue(cellValue);
			}
		}

		return typeCode;
	}

	/**
	 * Populates given sheet with list of attributes based on
	 * {@link de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel}
	 *
	 * @param sheet
	 *           to populate
	 * @param classificationAttributes
	 *           source of population
	 * @param typeCode
	 *           which is necessary for first column
	 */
	private void populateAttributesBasedOnClassification(final Sheet sheet,
			final List<ExcelClassificationAttribute> classificationAttributes, final String typeCode)
	{
		for (final ExcelClassificationAttribute classification : classificationAttributes)
		{
			final Row row = appendRow(sheet);
			row.createCell(ExcelTemplateConstants.HeaderPrompt.HEADER_TYPE_CODE.getIndex()).setCellValue(typeCode);

			for (final Map.Entry<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelClassificationAttribute>> entry : excelClassificationPopulators
					.entrySet())
			{
				final String cellValue = entry.getValue().apply(DefaultExcelAttributeContext.ofExcelAttribute(classification));
				excelCellService.insertAttributeValue(row.createCell(entry.getKey().getIndex()), cellValue);
			}
		}
	}

	protected String getAttributeDescriptorName(final ExcelAttributeDescriptorAttribute attributeDescriptorAttribute)
	{
		final AttributeDescriptorModel attributeDescriptor = attributeDescriptorAttribute.getAttributeDescriptorModel();
		return StringUtils.isNotEmpty(attributeDescriptor.getName()) ? attributeDescriptor.getName()
				: attributeDescriptor.getQualifier();
	}

	private static Row appendRow(final Sheet typeSystemSheet)
	{
		return typeSystemSheet.createRow(typeSystemSheet.getLastRowNum() + 1);
	}

	private static Sheet getSheet(final Workbook workbook)
	{
		return workbook.getSheet(ExcelTemplateConstants.UtilitySheet.HEADER_PROMPT.getSheetName());
	}

	@Required
	public void setExcelAttributeDescriptorPopulators(
			final Map<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelAttributeDescriptorAttribute>> excelAttributeDescriptorPopulators)
	{
		this.excelAttributeDescriptorPopulators = excelAttributeDescriptorPopulators;
	}

	@Required
	public void setExcelClassificationPopulators(
			final Map<ExcelTemplateConstants.HeaderPrompt, ExcelCellPopulator<ExcelClassificationAttribute>> excelClassificationPopulators)
	{
		this.excelClassificationPopulators = excelClassificationPopulators;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}
}
