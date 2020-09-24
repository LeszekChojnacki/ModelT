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

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.importing.ExcelAttributeTypeSystemService;
import com.hybris.backoffice.excel.importing.ExcelClassificationTypeSystemService;
import com.hybris.backoffice.excel.importing.ExcelTypeSystemService;
import com.hybris.backoffice.excel.importing.data.ClassificationTypeSystemRow;
import com.hybris.backoffice.excel.template.header.ExcelHeaderService;
import com.hybris.backoffice.excel.template.populator.typesheet.TypeSystemRow;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator for types and attributes. The validator checks whether given sheet name and columns
 * (attributes) exist and user has permission to them. This validator supports classification system, and validates
 * classification attributes as well.
 */
public class WorkbookTypeCodeAndAttributesValidator extends WorkbookTypeCodeAndSelectedAttributeValidator
{

	protected static final String CLASSIFICATION_SYSTEM_ERRORS_HEADER = "excel.import.validation.workbook.classification.header";
	protected static final String UNKNOWN_CLASSIFICATION_SYSTEM_VERSION = "excel.import.validation.workbook.classification.unknown.system.version";
	protected static final String INSUFFICIENT_PERMISSIONS_TO_TYPE = "excel.import.validation.workbook.insufficient.permissions.to.type";

	private ExcelHeaderService excelHeaderService;
	private CatalogVersionService catalogVersionService;
	private UserService userService;
	private ExcelTypeSystemService<ExcelAttributeTypeSystemService.ExcelTypeSystem> excelAttributeTypeSystemService;
	private ExcelTypeSystemService<ExcelClassificationTypeSystemService.ExcelClassificationTypeSystem> excelClassificationTypeSystemService;

	@Override
	protected List<ExcelValidationResult> validateSelectedColumns(final Sheet typeSystemSheet, final Sheet sheet)
	{
		final List<ExcelValidationResult> validationResults = new ArrayList<>();
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(sheet.getWorkbook(), sheet.getSheetName());
		final Collection<String> headerNames = excelHeaderService.getHeaderDisplayNames(sheet);

		final List<TypeSystemRow> standardAttributes = new ArrayList<>();
		final List<ClassificationTypeSystemRow> classificationAttributes = new ArrayList<>();
		final List<String> unknownAttributes = new ArrayList<>();

		final ExcelAttributeTypeSystemService.ExcelTypeSystem typeSystem = excelAttributeTypeSystemService
				.loadTypeSystem(sheet.getWorkbook());
		final ExcelClassificationTypeSystemService.ExcelClassificationTypeSystem classificationTypeSystem = excelClassificationTypeSystemService
				.loadTypeSystem(sheet.getWorkbook());

		for (final String headerName : headerNames)
		{
			final Optional<TypeSystemRow> typeSystemRow = typeSystem.findRow(headerName);
			final Optional<ClassificationTypeSystemRow> classificationTypeSystemRow = classificationTypeSystem.findRow(headerName);

			if (typeSystemRow.isPresent())
			{
				standardAttributes.add(typeSystemRow.get());
			}
			else if (classificationTypeSystemRow.isPresent())
			{
				classificationAttributes.add(classificationTypeSystemRow.get());
			}
			else
			{
				unknownAttributes.add(headerName);
			}
		}

		validateColumnUniqueness(typeCode, sheet).ifPresent(validationResults::add);
		validateThatColumnsExistAndUserHasPermission(typeCode, standardAttributes, unknownAttributes)
				.ifPresent(validationResults::add);
		if (classificationTypeSystem.exists())
		{
			validateClassificationAttributes(typeCode, classificationAttributes).ifPresent(validationResults::add);
		}
		return validationResults;
	}

	protected Optional<ExcelValidationResult> validateColumnUniqueness(final String typeCode, final Sheet sheet)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		final Collection<String> attributeNames = excelHeaderService.getHeaderDisplayNames(sheet);
		final Set<String> duplicates = findDuplicates(attributeNames);

		duplicates.forEach(attributeName -> messages
				.add(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_DESCRIPTION, attributeName)));

		if (messages.isEmpty())
		{
			return Optional.empty();
		}

		final ExcelValidationResult validationResult = new ExcelValidationResult(messages);
		validationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_DUPLICATED_COLUMNS_HEADER, typeCode));
		return Optional.of(validationResult);
	}

	protected Optional<ExcelValidationResult> validateThatColumnsExistAndUserHasPermission(final String typeCode,
			final List<TypeSystemRow> standardAttributes, final List<String> unknownAttributes)
	{
		final List<ValidationMessage> messages = new ArrayList<>();

		messages.addAll(createValidationMessagesForUnknownAttributes(typeCode, unknownAttributes));

		final Set<TypeSystemRow> attributesWithoutDuplicates = new HashSet<>(standardAttributes);
		attributesWithoutDuplicates.stream()//
				.map(attribute -> validateSingleAttribute(typeCode, attribute.getAttrName(), attribute.getAttrQualifier()))//
				.filter(Optional::isPresent)//
				.map(Optional::get)//
				.forEach(messages::add);

		if (messages.isEmpty())
		{
			return Optional.empty();
		}

		final ExcelValidationResult validationResult = new ExcelValidationResult(messages);
		validationResult.setHeader(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_HEADER, typeCode));
		return Optional.of(validationResult);
	}

	protected List<ValidationMessage> createValidationMessagesForUnknownAttributes(final String typeCode,
			final List<String> attributes)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		for (final String unknownColumnName : attributes)
		{
			messages.add(new ValidationMessage(EXCEL_IMPORT_VALIDATION_WORKBOOK_UNKNOWN_ATTRIBUTE_DESCRIPTION, typeCode,
					unknownColumnName));
		}
		return messages;
	}

	protected Optional<ExcelValidationResult> validateClassificationAttributes(final String typeCode,
			final List<ClassificationTypeSystemRow> classificationAttributes)
	{
		final List<ValidationMessage> messages = new ArrayList<>();

		messages.addAll(validateClassificationSystemVersionsExistAndUserHasPermissions(classificationAttributes));
		messages.addAll(validatePermissionsToTypes());

		if (messages.isEmpty())
		{
			return Optional.empty();
		}
		final ExcelValidationResult validationResult = new ExcelValidationResult(messages);
		validationResult.setHeader(new ValidationMessage(CLASSIFICATION_SYSTEM_ERRORS_HEADER, typeCode));
		return Optional.of(validationResult);
	}

	protected List<ValidationMessage> validateClassificationSystemVersionsExistAndUserHasPermissions(
			final List<ClassificationTypeSystemRow> classificationAttributes)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		final UserModel currentUser = getUserService().getCurrentUser();
		final Set<Pair<String, String>> classificationSystemVersions = extractUniqueClassificationSystemVersions(
				classificationAttributes);

		for (final Pair<String, String> version : classificationSystemVersions)
		{
			final String classificationSystem = version.getLeft();
			final String classificationVersion = version.getRight();
			try
			{
				final CatalogVersionModel catalogVersion = getCatalogVersionService().getCatalogVersion(classificationSystem,
						classificationVersion);

				if (!getCatalogVersionService().canRead(catalogVersion, currentUser)
						|| !getCatalogVersionService().canWrite(catalogVersion, currentUser))
				{
					messages.add(
							new ValidationMessage(UNKNOWN_CLASSIFICATION_SYSTEM_VERSION, classificationSystem, classificationVersion));
				}
			}
			catch (final UnknownIdentifierException ex)
			{
				messages
						.add(new ValidationMessage(UNKNOWN_CLASSIFICATION_SYSTEM_VERSION, classificationSystem, classificationVersion));
			}
		}
		return messages;
	}

	private static Set<Pair<String, String>> extractUniqueClassificationSystemVersions(
			final List<ClassificationTypeSystemRow> classificationAttributes)
	{
		return classificationAttributes.stream()//
				.map(attr -> new ImmutablePair<>(attr.getClassificationSystem(), attr.getClassificationVersion()))//
				.collect(Collectors.toSet());
	}

	protected List<ValidationMessage> validatePermissionsToTypes()
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		final List<String> classificationTypes = Lists.newArrayList(ClassificationSystemModel._TYPECODE,
				ClassificationSystemVersionModel._TYPECODE, ClassificationClassModel._TYPECODE,
				ClassificationAttributeModel._TYPECODE);

		classificationTypes.forEach(type -> {
			if (!getPermissionCRUDService().canReadType(type) || !getPermissionCRUDService().canCreateTypeInstance(type)
					|| !getPermissionCRUDService().canChangeType(type))
			{
				messages.add(new ValidationMessage(INSUFFICIENT_PERMISSIONS_TO_TYPE, type));
			}
		});

		return messages;
	}

	@Override
	public ExcelHeaderService getExcelHeaderService()
	{
		return excelHeaderService;
	}

	@Override
	@Required
	public void setExcelHeaderService(final ExcelHeaderService excelHeaderService)
	{
		this.excelHeaderService = excelHeaderService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}


	@Required
	public void setExcelAttributeTypeSystemService(
			final ExcelTypeSystemService<ExcelAttributeTypeSystemService.ExcelTypeSystem> excelAttributeTypeSystemService)
	{
		this.excelAttributeTypeSystemService = excelAttributeTypeSystemService;
	}

	@Required
	public void setExcelClassificationTypeSystemService(
			final ExcelTypeSystemService<ExcelClassificationTypeSystemService.ExcelClassificationTypeSystem> excelClassificationTypeSystemService)
	{
		this.excelClassificationTypeSystemService = excelClassificationTypeSystemService;
	}

}
