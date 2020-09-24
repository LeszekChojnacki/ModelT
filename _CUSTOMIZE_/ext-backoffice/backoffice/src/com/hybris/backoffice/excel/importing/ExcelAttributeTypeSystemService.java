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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

import com.hybris.backoffice.excel.importing.data.TypeSystem;
import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.populator.typesheet.TypeSystemRow;


/**
 * Allows to read the information from the hidden TypeSystem sheet in previously exported Excel file.
 */
public class ExcelAttributeTypeSystemService implements ExcelTypeSystemService<ExcelAttributeTypeSystemService.ExcelTypeSystem>
{

	private static final String LANG_GROUP_NAME = "lang";
	private static final Pattern LANG_EXTRACT_PATTERN = Pattern.compile(".+\\[(?<lang>.+)]"); // e.g.: description[en]

	private ExcelCellService cellService;
	private CollectionFormatter collectionFormatter;

	@Override
	public ExcelTypeSystem loadTypeSystem(final Workbook workbook)
	{
		final ExcelTypeSystem typeSystem = new ExcelTypeSystem();
		final Sheet typeSheet = workbook.getSheet(ExcelTemplateConstants.UtilitySheet.TYPE_SYSTEM.getSheetName());
		for (int rowIndex = TYPE_SYSTEM_FIRST_ROW_INDEX; rowIndex <= typeSheet.getLastRowNum(); rowIndex++)
		{
			final Row row = typeSheet.getRow(rowIndex);
			final List<TypeSystemRow> typeSystemRows = createTypeSystemRows(row);
			typeSystemRows.forEach(typeSystemRow -> typeSystem.putRow(StringUtils.trim(typeSystemRow.getAttrDisplayName()), typeSystemRow));
		}
		return typeSystem;
	}

	private List<TypeSystemRow> createTypeSystemRows(final Row row)
	{
		final ArrayList<TypeSystemRow> typeSystemRows = new ArrayList<>();
		if (row != null)
		{
			for (final String fullName : decompressCellValue(
					getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_DISPLAYED_NAME)))
			{
				typeSystemRows.add(createTypeSystemRow(row, fullName));
			}
		}
		return typeSystemRows;
	}

	private TypeSystemRow createTypeSystemRow(final Row row, final String fullName)
	{
		final TypeSystemRow typeSystemRow = new TypeSystemRow();

		typeSystemRow.setTypeCode(getCellValue(row, ExcelTemplateConstants.TypeSystem.TYPE_CODE));
		typeSystemRow.setTypeName(getCellValue(row, ExcelTemplateConstants.TypeSystem.TYPE_NAME));
		typeSystemRow.setAttrQualifier(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_QUALIFIER));
		typeSystemRow.setAttrName(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_NAME));
		typeSystemRow.setAttrOptional("true".equalsIgnoreCase(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_OPTIONAL)));
		typeSystemRow.setAttrTypeCode(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_TYPE_CODE));
		typeSystemRow.setAttrTypeItemType(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_TYPE_ITEMTYPE));
		typeSystemRow
				.setAttrLocalized("true".equalsIgnoreCase(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_LOCALIZED)));
		typeSystemRow.setAttrLocLang(extractIsoCode(fullName));
		typeSystemRow.setAttrDisplayName(fullName);
		typeSystemRow.setAttrUnique("true".equalsIgnoreCase(getCellValue(row, ExcelTemplateConstants.TypeSystem.ATTR_UNIQUE)));
		typeSystemRow.setAttrReferenceFormat(getCellValue(row, ExcelTemplateConstants.TypeSystem.REFERENCE_FORMAT));

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

	private String getCellValue(final Row row, final ExcelTemplateConstants.TypeSystem column)
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
	 * Represents the hidden TypeSystem sheet in previously exported excel file.
	 */
	public static class ExcelTypeSystem implements TypeSystem<TypeSystemRow>
	{
		private final Map<String, TypeSystemRow> typeSystemRows = new HashMap<>();

		private ExcelTypeSystem()
		{
		}

		public Optional<TypeSystemRow> findRow(final String attributeDisplayName)
		{
			return Optional.ofNullable(typeSystemRows.get(attributeDisplayName));
		}

		public void putRow(final String key, final TypeSystemRow value)
		{
			typeSystemRows.put(key, value);
		}

		public boolean exists()
		{
			return !typeSystemRows.isEmpty();
		}

	}
}
