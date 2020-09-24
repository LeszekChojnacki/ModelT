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

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.Config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.data.SelectedAttributeQualifier;
import com.hybris.backoffice.excel.template.cell.ExcelCellService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.typesheet.TypeSystemSheetPopulator;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;
import com.hybris.backoffice.excel.util.ExcelDateUtils;


/**
 * @deprecated since 1808.
 **/
@Deprecated
public class DefaultExcelTemplateService implements ExcelTemplateService
{
	private static final UnaryOperator<String> EMPTY_PATTERN = arg -> String.format("%s cannot be empty or null", arg);
	private static final UnaryOperator<String> NULL_PATTERN = arg -> String.format("%s cannot be null", arg);
	private static final String SELECTED_ATTRIBUTE_ARG = "selectedAttribute";
	private static final String SHEET_ARG = "Sheet";
	private static final String WORKBOOK_ARG = "Workbook";
	private static final String VALUE_ARG = "Value";

	private ExcelCellService cellService;
	private ExcelSheetService sheetService;
	private ExcelHeaderService headerService;
	private ExcelWorkbookService workbookService;

	private AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter;

	private TypeService typeService;

	/**
	 * @deprecated since 1808. Not used anymore.
	 */
	@Deprecated
	private ExcelDateUtils excelDateUtils;
	/**
	 * @deprecated since 1808. Not used anymore.
	 */
	@Deprecated
	private ExcelSheetNamingStrategy excelSheetNamingStrategy;


	/**
	 * @deprecated since 1808. This field is used only by deprecated methods
	 *             ({@link #addAttributeToExcelTypeSystemSheet(AttributeDescriptorModel, Sheet)},
	 *             {@link #isMandatory(AttributeDescriptorModel, String)}). Should be removed together with this method.
	 */
	@Deprecated
	private CommonI18NService commonI18NService;
	/**
	 * @deprecated since 1808. This field is used only by deprecated methods
	 *             ({@link #addAttributeToExcelTypeSystemSheet(AttributeDescriptorModel, Sheet)},
	 *             {@link #addAttributeToExcelTypeSystemSheet(AttributeDescriptorModel, Sheet, String)}). Should be removed
	 *             together with this method.
	 */
	@Deprecated
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	/**
	 * @deprecated since 1808. This field is used only by deprecated methods
	 *             ({@link #populateTypeSystemSheet(ComposedTypeModel, Workbook)}). Should be removed together with this
	 *             method.
	 */
	@Deprecated
	private PermissionCRUDService permissionCRUDService;

	@Override
	public Workbook createWorkbook(final InputStream is)
	{
		return workbookService.createWorkbook(is);
	}

	@Override
	public Sheet getTypeSystemSheet(final Workbook workbook)
	{
		return workbookService.getMetaInformationSheet(workbook);
	}

	@Override
	public List<String> getSheetsNames(final Workbook workbook)
	{
		return new ArrayList<>(sheetService.getSheetsNames(workbook));
	}

	@Override
	public List<Sheet> getSheets(final Workbook workbook)
	{
		return new ArrayList<>(sheetService.getSheets(workbook));
	}

	@Override
	public String getCellValue(final Cell cell)
	{
		return cellService.getCellValue(cell);
	}

	@Override
	public List<SelectedAttribute> getHeaders(final Sheet typeSystemSheet, final Sheet typeSheet)
	{
		return new ArrayList<>(headerService.getHeaders(typeSystemSheet, typeSheet));
	}

	@Override
	public List<SelectedAttributeQualifier> getSelectedAttributesQualifiers(final Sheet typeSystemSheet, final Sheet typeSheet)
	{
		return new ArrayList<>(headerService.getSelectedAttributesQualifiers(typeSystemSheet, typeSheet));
	}

	@Override
	public int findColumnIndex(final Sheet typeSystemSheet, final Sheet sheet, final SelectedAttribute selectedAttribute)
	{
		return sheetService.findColumnIndex(typeSystemSheet, sheet,
				new ExcelAttributeDescriptorAttribute(selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()));
	}

	@Override
	public Sheet createTypeSheet(final String typeCode, final Workbook workbook)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(typeCode), EMPTY_PATTERN.apply("typeCode"));
		Preconditions.checkNotNull(workbook, NULL_PATTERN.apply(WORKBOOK_ARG));

		return sheetService.createOrGetTypeSheet(workbook, typeCode);
	}

	@Override
	public String findTypeCodeForSheetName(final String sheetName, final Workbook workbook)
	{
		return sheetService.findTypeCodeForSheetName(workbook, sheetName);
	}

	@Override
	public String findSheetNameForTypeCode(final String typeCode, final Workbook workbook)
	{
		return sheetService.findSheetNameForTypeCode(workbook, typeCode);
	}

	@Override
	public void addTypeSheet(final String typeName, final Workbook workbook)
	{
		Preconditions.checkArgument(StringUtils.isNotEmpty(typeName), EMPTY_PATTERN.apply("typeName"));
		Preconditions.checkNotNull(workbook, NULL_PATTERN.apply(WORKBOOK_ARG));

		sheetService.createTypeSheet(workbook, typeName);
	}

	@Override
	public void insertAttributeHeader(final Sheet sheet, final SelectedAttribute selectedAttribute, final int columnIndex)
	{
		Preconditions.checkNotNull(sheet, NULL_PATTERN.apply(SHEET_ARG));
		Preconditions.checkNotNull(selectedAttribute, NULL_PATTERN.apply(SELECTED_ATTRIBUTE_ARG));

		headerService.insertAttributeHeader(sheet,
				new ExcelAttributeDescriptorAttribute(selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()),
				columnIndex);
	}

	@Override
	public void insertAttributesHeader(final Sheet sheet, final Collection<SelectedAttribute> selectedAttributes)
	{
		Preconditions.checkNotNull(sheet, NULL_PATTERN.apply(SHEET_ARG));
		Preconditions.checkNotNull(selectedAttributes, NULL_PATTERN.apply(SELECTED_ATTRIBUTE_ARG));

		headerService.insertAttributesHeader(sheet, //
				selectedAttributes.stream() //
						.map( //
								selectedAttribute -> new ExcelAttributeDescriptorAttribute( //
										selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()) //
						) //
						.collect(Collectors.toList()));
	}

	@Override
	public void insertAttributeValue(final Cell cell, final Object object)
	{
		Preconditions.checkNotNull(cell, NULL_PATTERN.apply("Cell"));
		Preconditions.checkNotNull(object, NULL_PATTERN.apply(VALUE_ARG));

		cellService.insertAttributeValue(cell, object);
	}

	@Override
	public Row createEmptyRow(final Sheet sheet)
	{
		Preconditions.checkNotNull(sheet, NULL_PATTERN.apply(SHEET_ARG));

		return sheetService.createEmptyRow(sheet);
	}

	@Override
	public String getAttributeDisplayName(final AttributeDescriptorModel attributeDescriptorModel, final String isoCode)
	{
		return attributeNameFormatter.format(DefaultExcelAttributeContext
				.ofExcelAttribute(new ExcelAttributeDescriptorAttribute(attributeDescriptorModel, isoCode)));
	}

	/**
	 * @deprecated since 1808. Process of hiding sheets was moved to
	 *             {@link com.hybris.backoffice.excel.exporting.HideUtilitySheetsDecorator}
	 */
	@Deprecated
	protected void hideUtilitySheet(final Workbook workbook, final String sheetName)
	{
		if (ExcelTemplateConstants.isUtilitySheet(sheetName))
		{
			final int sheetIndex = workbook.getSheetIndex(sheetName);

			if (!workbook.isSheetHidden(sheetIndex) || workbook.getSheetAt(sheetIndex).isSelected())
			{
				activateFirstNonUtilitySheet(workbook);
				workbook.getSheetAt(sheetIndex).setSelected(false);
				workbook.setSheetHidden(sheetIndex, getUtilitySheetHiddenLevel());
			}
		}
	}

	/**
	 * @deprecated since 1808. Process of hiding sheets was moved to
	 *             {@link com.hybris.backoffice.excel.exporting.HideUtilitySheetsDecorator}
	 */
	@Deprecated
	protected int getUtilitySheetHiddenLevel()
	{
		return Config.getBoolean("backoffice.excel.utility.sheets.hidden", true) ? Workbook.SHEET_STATE_VERY_HIDDEN
				: Workbook.SHEET_STATE_HIDDEN;
	}

	/**
	 * @deprecated since 1808. Process of hiding sheets was moved to
	 *             {@link com.hybris.backoffice.excel.exporting.HideUtilitySheetsDecorator}
	 */
	@Deprecated
	protected void activateFirstNonUtilitySheet(final Workbook workbook)
	{
		if (ExcelTemplateConstants.isUtilitySheet(workbook.getSheetName(workbook.getActiveSheetIndex())))
		{
			for (int i = 0; i < workbook.getNumberOfSheets(); i++)
			{
				if (!ExcelTemplateConstants.isUtilitySheet(workbook.getSheetName(i)))
				{
					workbook.setActiveSheet(i);
				}
			}
		}
	}

	/**
	 * @deprecated since 1808, please use {@link TypeSystemSheetPopulator#populate(ExcelExportResult)} instead. The
	 *             typeSystem sheet is now being populated in
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookPostProcessor} via
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookDecorator}
	 */
	@Deprecated
	protected void populateTypeSystemSheet(final ComposedTypeModel composedType, final Workbook workbook)
	{
		final Sheet typeSystemSheet = workbook.getSheet(ExcelTemplateConstants.TYPE_SYSTEM);
		typeService.getAttributeDescriptorsForType(composedType)//
				.stream()//
				.filter(attribute -> BooleanUtils.isTrue(attribute.getReadable()) && BooleanUtils.isTrue(attribute.getWritable()))
				.filter(permissionCRUDService::canReadAttribute)//
				.sorted(Comparator.comparing(this::getAttributeDescriptorName))//
				.forEach(attributeDescriptor -> addAttributeToExcelTypeSystemSheet(attributeDescriptor, typeSystemSheet));
	}

	/**
	 * @deprecated since 1808, please use {@link TypeSystemSheetPopulator#populate(ExcelExportResult)} instead. The
	 *             typeSystem sheet is now being populated in
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookPostProcessor} via
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookDecorator}
	 */
	@Deprecated
	protected void addAttributeToExcelTypeSystemSheet(final AttributeDescriptorModel attributeDescriptor, final Sheet sheet)
	{
		if (excelTranslatorRegistry.getTranslator(attributeDescriptor).isPresent())
		{
			if (attributeDescriptor.getLocalized())
			{
				commonI18NService.getAllLanguages().stream().filter(LanguageModel::getActive)
						.forEach(lang -> addAttributeToExcelTypeSystemSheet(attributeDescriptor, sheet, lang.getIsocode()));
			}
			else
			{
				addAttributeToExcelTypeSystemSheet(attributeDescriptor, sheet, "");
			}
		}
	}

	/**
	 * @deprecated since 1808, please use {@link TypeSystemSheetPopulator#populate(ExcelExportResult)} instead. The
	 *             typeSystem sheet is now being populated in
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookPostProcessor} via
	 *             {@link com.hybris.backoffice.excel.exporting.ExcelExportWorkbookDecorator}
	 */
	@Deprecated
	protected void addAttributeToExcelTypeSystemSheet(final AttributeDescriptorModel attributeDescriptor, final Sheet sheet,
			final String langIsoCode)
	{
		final Row row = sheet.createRow(sheet.getLastRowNum() + 1);

		final String sheetNameForTypeCode = findSheetNameForTypeCode(attributeDescriptor.getEnclosingType().getCode(),
				sheet.getWorkbook());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.TYPE_CODE).setCellValue(sheetNameForTypeCode);
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.TYPE_NAME)
				.setCellValue(attributeDescriptor.getEnclosingType().getName());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_QUALIFIER).setCellValue(attributeDescriptor.getQualifier());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_NAME).setCellValue(attributeDescriptor.getName());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_OPTIONAL).setCellValue(attributeDescriptor.getOptional());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_TYPE_CODE)
				.setCellValue(attributeDescriptor.getAttributeType().getCode());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_TYPE_ITEMTYPE)
				.setCellValue(attributeDescriptor.getDeclaringEnclosingType().getCode());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_LOCALIZED).setCellValue(attributeDescriptor.getLocalized());
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_LOC_LANG).setCellValue(langIsoCode);
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_DISPLAYED_NAME)
				.setCellValue(getAttributeDisplayName(attributeDescriptor, langIsoCode));
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.ATTR_UNIQUE).setCellValue(attributeDescriptor.getUnique());
		final String referenceFormat = excelTranslatorRegistry.getTranslator(attributeDescriptor)
				.map(excelValueTranslator -> excelValueTranslator.referenceFormat(attributeDescriptor)).orElse("");
		row.createCell(ExcelTemplateConstants.TypeSystemColumns.REFERENCE_FORMAT).setCellValue(referenceFormat);
	}

	/**
	 * @deprecated since 1808. Logic was moved to {@link DisplayNameAttributeNameFormatter}
	 */
	@Deprecated
	protected boolean isMandatory(final AttributeDescriptorModel attributeDescriptor, final String langIsoCode)
	{
		if (BooleanUtils.isTrue(attributeDescriptor.getOptional()))
		{
			return false;
		}
		if (attributeDescriptor.getLocalized())
		{
			return commonI18NService.getCurrentLanguage().getIsocode().equals(langIsoCode);
		}
		return true;
	}

	/**
	 * @deprecated since 1808. Logic was moved to {@link DisplayNameAttributeNameFormatter}
	 */
	@Deprecated
	protected String getAttributeDescriptorName(final AttributeDescriptorModel attributeDescriptor)
	{
		return StringUtils.isNotEmpty(attributeDescriptor.getName()) ? attributeDescriptor.getName()
				: attributeDescriptor.getQualifier();
	}

	@Required
	public void setCellService(final ExcelCellService cellService)
	{
		this.cellService = cellService;
	}

	@Required
	public void setHeaderService(final ExcelHeaderService headerService)
	{
		this.headerService = headerService;
	}

	@Required
	public void setSheetService(final ExcelSheetService sheetService)
	{
		this.sheetService = sheetService;
	}

	@Required
	public void setWorkbookService(final ExcelWorkbookService workbookService)
	{
		this.workbookService = workbookService;
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelTranslatorRegistry getExcelTranslatorRegistry()
	{
		return excelTranslatorRegistry;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelDateUtils getExcelDateUtils()
	{
		return excelDateUtils;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public void setExcelDateUtils(final ExcelDateUtils excelDateUtils)
	{
		this.excelDateUtils = excelDateUtils;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelSheetNamingStrategy getExcelSheetNamingStrategy()
	{
		return excelSheetNamingStrategy;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public void setExcelSheetNamingStrategy(final ExcelSheetNamingStrategy excelSheetNamingStrategy)
	{
		this.excelSheetNamingStrategy = excelSheetNamingStrategy;
	}
}
