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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.SelectedAttributeQualifier;
import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for types and attributes. The validator checks whether given sheet name and selected column
 * (selected attribute) exist and user has permission to them. <br/>
 * Note: Please use {@link WorkbookTypeCodeAndAttributesValidator} if you want support for classification system, as
 * this validator does not support it.
 */
public class WorkbookTypeCodeAndSelectedAttributeValidator implements WorkbookValidator
{

	public static final String EXCEL_IMPORT_VALIDATION_METADATA_UNKNOWN_TYPE_DESCRIPTION = "excel.import.validation.workbook.type.unknown.type";
	public static final String EXCEL_IMPORT_VALIDATION_METADATA_UNKNOWN_TYPE_HEADER = "excel.import.validation.workbook.type.unknown.header";
	public static final String EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_DESCRIPTION = "excel.import.validation.workbook.attribute.unknown.type";
	public static final String EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_HEADER = "excel.import.validation.workbook.attribute.unknown.header";
	public static final String EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_HEADER = "excel.import.validation.workbook.attribute.duplicated.header";
	public static final String EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_DESCRIPTION = "excel.import.validation.workbook.attribute.duplicated.description";

	private static final Logger LOG = LoggerFactory.getLogger(WorkbookTypeCodeAndSelectedAttributeValidator.class);
	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	private ExcelTemplateService excelTemplateService;
	private ExcelWorkbookService excelWorkbookService;
	private ExcelSheetService excelSheetService;
	private ExcelHeaderService excelHeaderService;
	private PermissionCRUDService permissionCRUDService;

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		final List<ExcelValidationResult> validationResults = new ArrayList<>();
		final Sheet typeSystemSheet = getExcelWorkbookService().getMetaInformationSheet(workbook);
		final Collection<Sheet> sheets = getExcelSheetService().getSheets(workbook);
		for (final Sheet sheet : sheets)
		{
			validateSheet(typeSystemSheet, sheet).stream().filter(result -> !result.getValidationErrors().isEmpty())
					.forEach(validationResults::add);
		}

		return validationResults;
	}

	protected List<ExcelValidationResult> validateSheet(final Sheet typeSystemSheet, final Sheet sheet)
	{
		final List<ExcelValidationResult> validationResults = new ArrayList<>();
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(sheet.getWorkbook(), sheet.getSheetName());
		final Optional<ExcelValidationResult> typeCodeValidationResult = validateTypeCode(typeCode);
		if (typeCodeValidationResult.isPresent())
		{
			validationResults.add(typeCodeValidationResult.get());
		}
		else
		{
			validateSelectedColumns(typeSystemSheet, sheet).stream().filter(ExcelValidationResult::hasErrors)
					.forEach(validationResults::add);
		}
		return validationResults;
	}

	protected Optional<ExcelValidationResult> validateTypeCode(final String typeCode)
	{
		try
		{
			if (!hasPermissionsToTypeCode(typeCode))
			{
				return Optional.of(prepareValidationResultForUnknownType(typeCode));
			}
		}
		catch (final UnknownIdentifierException ex)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(String.format("Unknown type for code %s", typeCode), ex);
			}
			return Optional.of(prepareValidationResultForUnknownType(typeCode));
		}
		return Optional.empty();
	}

	protected List<ExcelValidationResult> validateSelectedColumns(final Sheet typeSystemSheet, final Sheet sheet)
	{
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(sheet.getWorkbook(), sheet.getSheetName());
		final List<SelectedAttributeQualifier> qualifiers = new ArrayList<>(getExcelHeaderService()
				.getSelectedAttributesQualifiers(typeSystemSheet, sheet));
		final List<ExcelValidationResult> validationResults = new ArrayList<>();
		validateColumnUniqueness(sheet.getSheetName(), qualifiers).ifPresent(validationResults::add);
		validateWhetherColumnExistAndUserHasPermission(typeCode, qualifiers).ifPresent(validationResults::add);
		return validationResults;
	}

	protected Optional<ExcelValidationResult> validateWhetherColumnExistAndUserHasPermission(final String typeCode,
			final List<SelectedAttributeQualifier> qualifiers)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		for (final SelectedAttributeQualifier qualifier : qualifiers)
		{
			if (StringUtils.isBlank(qualifier.getQualifier()))
			{
				messages.add(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_DESCRIPTION, typeCode,
						qualifier.getName()));
			}
			else
			{
				validateSingleAttribute(typeCode, qualifier).ifPresent(messages::add);
			}
		}
		if (messages.isEmpty())
		{
			return Optional.empty();
		}

		final ExcelValidationResult validationResult = new ExcelValidationResult(messages);
		validationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_HEADER, typeCode));
		return Optional.of(validationResult);
	}

	private Optional<ValidationMessage> validateSingleAttribute(final String typeCode, final SelectedAttributeQualifier qualifier)
	{
		return validateSingleAttribute(typeCode, qualifier.getName(), qualifier.getQualifier());
	}

	protected Optional<ValidationMessage> validateSingleAttribute(final String typeCode, final String columnName,
			final String qualifier)
	{
		try
		{
			if (!hasPermissionsToAttribute(typeCode, qualifier))
			{
				return Optional.of(
						new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_DESCRIPTION, typeCode, columnName));
			}
		}
		catch (final UnknownIdentifierException ex)
		{
			return Optional
					.of(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_DESCRIPTION, typeCode, columnName));
		}
		return Optional.empty();
	}

	protected Optional<ExcelValidationResult> validateColumnUniqueness(final String sheetName,
			final List<SelectedAttributeQualifier> selectedColumns)
	{
		final Set<SelectedAttributeQualifier> duplicatedColumns = findDuplicatedColumns(selectedColumns);
		final List<String> duplicateColumnsNames = duplicatedColumns.stream().map(SelectedAttributeQualifier::getName)
				.collect(Collectors.toList());
		return createValidationResult(sheetName, duplicateColumnsNames);
	}

	protected Optional<ExcelValidationResult> createValidationResult(final String sheetName,
			final List<String> duplicateColumnNames)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		for (final String columnName : duplicateColumnNames)
		{
			messages.add(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_DESCRIPTION, columnName));
		}

		if (messages.isEmpty())
		{
			return Optional.empty();
		}
		final ExcelValidationResult validationResult = new ExcelValidationResult(messages);
		validationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_HEADER, sheetName));
		return Optional.of(validationResult);
	}

	protected Set<SelectedAttributeQualifier> findDuplicatedColumns(final List<SelectedAttributeQualifier> selectedColumns)
	{
		return findDuplicates(selectedColumns);
	}

	protected <T> Set<T> findDuplicates(final Collection<T> collection)
	{
		final Set<T> uniques = new HashSet<>();
		return collection.stream().filter(e -> !uniques.add(e)).collect(Collectors.toSet());
	}

	protected boolean hasPermissionsToTypeCode(final String typeCode)
	{
		return getPermissionCRUDService().canReadType(typeCode) && getPermissionCRUDService().canChangeType(typeCode)
				&& getPermissionCRUDService().canCreateTypeInstance(typeCode);
	}

	protected boolean hasPermissionsToAttribute(final String typeCode, final SelectedAttributeQualifier qualifier)
	{
		return hasPermissionsToAttribute(typeCode, qualifier.getQualifier());
	}

	protected boolean hasPermissionsToAttribute(final String typeCode, final String qualifier)
	{
		return getPermissionCRUDService().canReadAttribute(typeCode, qualifier)
				&& getPermissionCRUDService().canChangeAttribute(typeCode, qualifier);
	}

	protected ExcelValidationResult prepareValidationResultForUnknownType(final String sheetName)
	{
		final ExcelValidationResult validationResult = new ExcelValidationResult(
				new ValidationMessage(EXCEL_IMPORT_VALIDATION_METADATA_UNKNOWN_TYPE_DESCRIPTION, sheetName));
		validationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_METADATA_UNKNOWN_TYPE_HEADER, sheetName));
		return validationResult;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public ExcelTemplateService getExcelTemplateService()
	{
		return excelTemplateService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setExcelTemplateService(final ExcelTemplateService excelTemplateService)
	{
		this.excelTemplateService = excelTemplateService;
	}

	public ExcelWorkbookService getExcelWorkbookService()
	{
		return excelWorkbookService;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
	}

	public ExcelSheetService getExcelSheetService()
	{
		return excelSheetService;
	}

	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}

	public ExcelHeaderService getExcelHeaderService()
	{
		return excelHeaderService;
	}

	@Required
	public void setExcelHeaderService(final ExcelHeaderService excelHeaderService)
	{
		this.excelHeaderService = excelHeaderService;
	}

	public PermissionCRUDService getPermissionCRUDService()
	{
		return permissionCRUDService;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}
}
