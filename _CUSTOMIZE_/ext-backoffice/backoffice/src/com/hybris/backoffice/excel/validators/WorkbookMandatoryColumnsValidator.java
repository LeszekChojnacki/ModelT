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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.SelectedAttribute;
import com.hybris.backoffice.excel.template.DisplayNameAttributeNameFormatter;
import com.hybris.backoffice.excel.template.ExcelTemplateService;
import com.hybris.backoffice.excel.template.mapper.ExcelMapper;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Default excel validator which checks whether all mandatory columns are selected for each excel sheet. If mandatory
 * column is localized then column for current language is required.
 */
public class WorkbookMandatoryColumnsValidator implements WorkbookValidator
{

	public static final String VALIDATION_MESSAGE_HEADER = "excel.import.validation.workbook.mandatory.column.header";
	public static final String VALIDATION_MESSAGE_DESCRIPTION = "excel.import.validation.workbook.mandatory.column.description";

	private static final Logger LOG = LoggerFactory.getLogger(WorkbookMandatoryColumnsValidator.class);
	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	private ExcelTemplateService excelTemplateService;
	private ExcelWorkbookService excelWorkbookService;
	private ExcelSheetService excelSheetService;
	private TypeService typeService;
	private CommonI18NService commonI18NService;
	private DisplayNameAttributeNameFormatter displayNameAttributeNameFormatter;

	private ExcelMapper<String, AttributeDescriptorModel> mapper;

	@Override
	public List<ExcelValidationResult> validate(final Workbook workbook)
	{
		final List<ExcelValidationResult> validationResults = new ArrayList<>();
		final Sheet typeSystemSheet = getExcelWorkbookService().getMetaInformationSheet(workbook);
		getExcelSheetService().getSheets(workbook).stream().map(sheet -> validateSheet(typeSystemSheet, sheet))
				.filter(Optional::isPresent).map(Optional::get).forEach(validationResults::add);
		return validationResults;
	}

	protected Optional<ExcelValidationResult> validateSheet(final Sheet typeSystemSheet, final Sheet sheet)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		final String typeCode = getExcelSheetService().findTypeCodeForSheetName(sheet.getWorkbook(), sheet.getSheetName());
		final Set<AttributeDescriptorModel> mandatoryFields = findAllMandatoryFields(typeCode);
		for (final AttributeDescriptorModel mandatoryField : mandatoryFields)
		{
			final SelectedAttribute selectedAttribute = prepareSelectedAttribute(mandatoryField);
			final int columnIndex = getExcelSheetService().findColumnIndex(typeSystemSheet, sheet,
					prepareExcelAttribute(selectedAttribute.getAttributeDescriptor(), selectedAttribute.getIsoCode()));
			if (columnIndex == -1)
			{
				messages.add(new ValidationMessage(VALIDATION_MESSAGE_DESCRIPTION, getAttributeDisplayedName(mandatoryField),
						sheet.getSheetName()));
			}
		}

		if (messages.isEmpty())
		{
			return Optional.empty();
		}

		final ValidationMessage header = new ValidationMessage(VALIDATION_MESSAGE_HEADER, sheet.getSheetName());
		return Optional.of(new ExcelValidationResult(header, messages));
	}

	protected ExcelAttributeDescriptorAttribute prepareExcelAttribute(final AttributeDescriptorModel attributeDescriptor,
			final String isoCode)
	{
		return new ExcelAttributeDescriptorAttribute(attributeDescriptor, isoCode);
	}

	protected SelectedAttribute prepareSelectedAttribute(final AttributeDescriptorModel attributeDescriptor)
	{
		final String isoCode = BooleanUtils.isTrue(attributeDescriptor.getLocalized())
				? getCommonI18NService().getCurrentLanguage().getIsocode()
				: StringUtils.EMPTY;
		return new SelectedAttribute(isoCode, attributeDescriptor);
	}

	protected String getAttributeDisplayedName(final AttributeDescriptorModel attributeDescriptor)
	{
		final String isoCode = BooleanUtils.isTrue(attributeDescriptor.getLocalized())
				? getCommonI18NService().getCurrentLanguage().getIsocode()
				: StringUtils.EMPTY;
		return getDisplayNameAttributeNameFormatter()
				.format(DefaultExcelAttributeContext.ofExcelAttribute(prepareExcelAttribute(attributeDescriptor, isoCode)));
	}

	public Set<AttributeDescriptorModel> findAllMandatoryFields(final String typeCode)
	{
		try
		{
			return new HashSet<>(mapper.apply(typeCode));
		}
		catch (final UnknownIdentifierException ex)
		{
			// do nothing if typeCode doesn't exist. WorkbookTypeCodeValidator is responsible for checking that.
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Error occurred while finding all mandatory attributes", ex);
			}
		}
		return new HashSet<>();
	}

	/**
	 * @deprecated since 1808. Use {@link com.hybris.backoffice.excel.template.filter.DefaultValueCheckingFilter} instead
	 */
	@Deprecated
	protected boolean hasNotDefaultValue(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return attributeDescriptorModel.getDefaultValue() == null;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	public TypeService getTypeService()
	{
		return typeService;
	}

	/**
	 * @deprecated since 1808.
	 */
	@Deprecated
	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
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

	public DisplayNameAttributeNameFormatter getDisplayNameAttributeNameFormatter()
	{
		return displayNameAttributeNameFormatter;
	}

	@Required
	public void setDisplayNameAttributeNameFormatter(final DisplayNameAttributeNameFormatter displayNameAttributeNameFormatter)
	{
		this.displayNameAttributeNameFormatter = displayNameAttributeNameFormatter;
	}

	public ExcelMapper<String, AttributeDescriptorModel> getMapper()
	{
		return mapper;
	}

	@Required
	public void setMapper(final ExcelMapper<String, AttributeDescriptorModel> mapper)
	{
		this.mapper = mapper;
	}
}
