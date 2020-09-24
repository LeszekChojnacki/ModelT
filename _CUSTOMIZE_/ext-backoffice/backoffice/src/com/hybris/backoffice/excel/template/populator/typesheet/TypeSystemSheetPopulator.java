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
package com.hybris.backoffice.excel.template.populator.typesheet;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.mapper.ExcelMapper;
import com.hybris.backoffice.excel.template.populator.ExcelSheetPopulator;


/**
 * Populates workbook with information about the type system.
 */
public class TypeSystemSheetPopulator implements ExcelSheetPopulator
{
	private ExcelMapper<ExcelExportResult, AttributeDescriptorModel> mapper;
	private ExcelCellService excelCellService;
	private TypeSystemRowFactory typeSystemRowFactory;

	/**
	 * Populates given workbook with information about given types
	 * 
	 * @param excelExportResult
	 *           that contains the workbook to populate and export results to be used
	 */
	@Override
	public void populate(@Nonnull final ExcelExportResult excelExportResult)
	{
		final Sheet typeSystemSheet = excelExportResult.getWorkbook()
				.getSheet(ExcelTemplateConstants.UtilitySheet.TYPE_SYSTEM.getSheetName());
		populate(typeSystemSheet, mapper.apply(excelExportResult));
	}

	protected void populate(final Sheet typeSystemSheet, final Collection<AttributeDescriptorModel> attributeDescriptors)
	{
		final Map<String, TypeSystemRow> typeSystemRows = mergeAttributesByQualifier(attributeDescriptors);
		populateTypeSystemSheet(typeSystemSheet, typeSystemRows.values());
	}

	/**
	 * Groups {@link AttributeDescriptorModel}s by {@link ComposedTypeModel}'s code.
	 *
	 * @param attributes
	 *           which should be grouped
	 * @return a map where the key is {@link ComposedTypeModel}'s code and values are {@link TypeSystemRow}s
	 */
	protected Map<String, TypeSystemRow> mergeAttributesByQualifier(final Collection<AttributeDescriptorModel> attributes)
	{
		return attributes.stream().collect(
				Collectors.toMap(AttributeDescriptorModel::getQualifier, typeSystemRowFactory::create, typeSystemRowFactory::merge));
	}

	/**
	 * Populates sheet with collection of {@link TypeSystemRow}s. It uses
	 * {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants.TypeSystem} as columns.
	 *
	 * @param typeSystemSheet
	 *           a sheet where the information should be put
	 * @param typeSystemRows
	 *           the collection of {@link TypeSystemRow}s that should be used to populate sheet
	 */
	protected void populateTypeSystemSheet(final Sheet typeSystemSheet, final Collection<TypeSystemRow> typeSystemRows)
	{
		typeSystemRows.forEach(typeSystemRow -> appendTypeSystemRowToSheet(typeSystemSheet, typeSystemRow));
	}

	private void appendTypeSystemRowToSheet(final Sheet typeSystemSheet, final TypeSystemRow typeSystemRow)
	{
		final Row row = appendRow(typeSystemSheet);
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.TYPE_CODE.getIndex()),
				typeSystemRow.getTypeCode());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.TYPE_NAME.getIndex()),
				typeSystemRow.getTypeName());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_QUALIFIER.getIndex()),
				typeSystemRow.getAttrQualifier());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_NAME.getIndex()),
				typeSystemRow.getAttrName());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_OPTIONAL.getIndex()),
				typeSystemRow.getAttrOptional());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_TYPE_CODE.getIndex()),
				typeSystemRow.getAttrTypeCode());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_TYPE_ITEMTYPE.getIndex()),
				typeSystemRow.getAttrTypeItemType());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_LOCALIZED.getIndex()),
				typeSystemRow.getAttrLocalized());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_LOC_LANG.getIndex()),
				typeSystemRow.getAttrLocLang());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_DISPLAYED_NAME.getIndex()),
				typeSystemRow.getAttrDisplayName());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.ATTR_UNIQUE.getIndex()),
				typeSystemRow.getAttrUnique());
		excelCellService.insertAttributeValue(row.createCell(ExcelTemplateConstants.TypeSystem.REFERENCE_FORMAT.getIndex()),
				typeSystemRow.getAttrReferenceFormat());
	}

	private static Row appendRow(final Sheet sheet)
	{
		return sheet.createRow(sheet.getLastRowNum() + 1);
	}

	@Required
	public void setMapper(final ExcelMapper<ExcelExportResult, AttributeDescriptorModel> mapper)
	{
		this.mapper = mapper;
	}

	@Required
	public void setTypeSystemRowFactory(final TypeSystemRowFactory typeSystemRowFactory)
	{
		this.typeSystemRowFactory = typeSystemRowFactory;
	}

	@Required
	public void setExcelCellService(final ExcelCellService excelCellService)
	{
		this.excelCellService = excelCellService;
	}
}
