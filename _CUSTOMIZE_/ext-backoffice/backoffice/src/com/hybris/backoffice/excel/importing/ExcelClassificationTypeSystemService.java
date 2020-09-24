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
package com.hybris.backoffice.excel.importing;

import static com.hybris.backoffice.excel.template.ExcelTemplateConstants.TYPE_SYSTEM_FIRST_ROW_INDEX;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.importing.data.ClassificationTypeSystemRow;
import com.hybris.backoffice.excel.importing.data.TypeSystem;
import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;


/**
 * Allows to read the information from the hidden ClassificationTypeSystem sheet in previously exported Excel file.
 */
public class ExcelClassificationTypeSystemService
		implements ExcelTypeSystemService<ExcelClassificationTypeSystemService.ExcelClassificationTypeSystem>
{
	private static final String LANG_GROUP_NAME = "lang";
	private static final Pattern LANG_EXTRACT_PATTERN = Pattern.compile(".+\\[(?<lang>.+)].*"); // e.g.: electronics.dimensions[en] - SampleClassification/1.0

	private ExcelCellService cellService;
	private CollectionFormatter collectionFormatter;

	@Override
	public ExcelClassificationTypeSystem loadTypeSystem(final Workbook workbook)
	{
		final ExcelClassificationTypeSystem classificationTypeSystem = new ExcelClassificationTypeSystem();
		final Sheet classificationSheet = workbook
				.getSheet(ExcelTemplateConstants.UtilitySheet.CLASSIFICATION_TYPE_SYSTEM.getSheetName());
		for (int rowIndex = TYPE_SYSTEM_FIRST_ROW_INDEX; rowIndex <= classificationSheet.getLastRowNum(); rowIndex++)
		{
			final Row row = classificationSheet.getRow(rowIndex);
			final List<ClassificationTypeSystemRow> classificationTypeSystemRows = createClassificationTypeSystemRows(row);
			classificationTypeSystemRows
					.forEach(typeSystemRow -> classificationTypeSystem.putRow(typeSystemRow.getFullName(), typeSystemRow));
		}
		return classificationTypeSystem;
	}

	private List<ClassificationTypeSystemRow> createClassificationTypeSystemRows(final Row row)
	{
		final List<ClassificationTypeSystemRow> typeSystemRows = new LinkedList<>();
		if (row != null)
		{
			for (final String fullName : decompressCellValue(
					getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.FULL_NAME)))
			{
				typeSystemRows.add(createClassificationTypeSystemRow(row, fullName));
			}
		}
		return typeSystemRows;
	}

	private ClassificationTypeSystemRow createClassificationTypeSystemRow(final Row row, final String fullName)
	{
		final ClassificationTypeSystemRow typeSystemRow = new ClassificationTypeSystemRow();
		typeSystemRow.setFullName(fullName);
		typeSystemRow.setClassificationSystem(
				getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.CLASSIFICATION_SYSTEM));
		typeSystemRow.setClassificationVersion(
				getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.CLASSIFICATION_VERSION));
		typeSystemRow.setClassificationClass(
				getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.CLASSIFICATION_CLASS));
		typeSystemRow.setClassificationAttribute(
				getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.CLASSIFICATION_ATTRIBUTE));
		typeSystemRow.setLocalized("true"
				.equalsIgnoreCase(getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.ATTRIBUTE_LOCALIZED)));
		typeSystemRow.setIsoCode(extractIsoCode(fullName));
		typeSystemRow.setMandatory(
				"true".equalsIgnoreCase(getCellValue(row, ExcelTemplateConstants.ClassificationTypeSystemColumns.MANDATORY)));
		return typeSystemRow;
	}

	private Collection<String> decompressCellValue(final String value)
	{
		final Set<String> decompressedValues = collectionFormatter.formatToCollection(value);
		if (decompressedValues.isEmpty())
		{
			decompressedValues.add(value);
		}
		return decompressedValues;
	}

	private static String extractIsoCode(final String fullName)
	{
		final Matcher matcher = LANG_EXTRACT_PATTERN.matcher(fullName);
		if (matcher.find())
		{
			return matcher.group(LANG_GROUP_NAME);
		}
		return StringUtils.EMPTY;
	}

	private String getCellValue(final Row row, final ExcelTemplateConstants.ClassificationTypeSystemColumns column)
	{
		return cellService.getCellValue(row.getCell(column.getIndex()));
	}

	@Required
	public void setCellService(final ExcelCellService cellService)
	{
		this.cellService = cellService;
	}

	@Required
	public void setCollectionFormatter(final CollectionFormatter collectionFormatter)
	{
		this.collectionFormatter = collectionFormatter;
	}

	/**
	 * Represents the hidden ClassificationTypeSystem sheet in previously exported excel file.
	 */
	public static class ExcelClassificationTypeSystem implements TypeSystem<ClassificationTypeSystemRow>
	{
		private final Map<String, ClassificationTypeSystemRow> classificationTypeSystemRows = new HashMap<>();

		private ExcelClassificationTypeSystem()
		{
		}

		public Optional<ClassificationTypeSystemRow> findRow(final String attributeDisplayName)
		{
			return Optional.ofNullable(classificationTypeSystemRows.get(attributeDisplayName));
		}

		public void putRow(final String key, final ClassificationTypeSystemRow value)
		{
			classificationTypeSystemRows.put(key, value);
		}

		public boolean exists()
		{
			return !classificationTypeSystemRows.isEmpty();
		}

	}

}
