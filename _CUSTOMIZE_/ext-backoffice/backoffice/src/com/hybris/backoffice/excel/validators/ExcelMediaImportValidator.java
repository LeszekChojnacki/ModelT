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

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.ExcelImportService;
import com.hybris.backoffice.excel.translators.AbstractExcelMediaImportTranslator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Validator which checks if media import data is correct. Checks:
 * <ul>
 * <li>if at least one {@link AbstractExcelMediaImportTranslator#PARAM_FILE_PATH} or
 * {@link AbstractExcelMediaImportTranslator#PARAM_CODE} is defined</li>
 * <li>if given {@link AbstractExcelMediaImportTranslator#PARAM_FILE_PATH} exists in uploaded content</li>
 * <li>if there is only one reference defined</li>
 * </ul>
 */
public class ExcelMediaImportValidator implements ExcelSingleMediaValidator
{
	public static final String VALIDATION_DECLARED_MULTIPLE_REFERENCES = "excel.import.validation.media.multiple.declared";
	public static final String VALIDATION_PATH_AND_CODE_EMPTY = "excel.import.validation.media.pathandcode.empty";
	public static final String VALIDATION_MISSING_FILE_IN_ZIP = "excel.import.validation.media.content.missing.entry";
	public static final String VALIDATION_MISSING_ZIP = "excel.import.validation.media.content.missing.zip";
	private TypeService typeService;

	/**
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	private ExcelImportService importService;

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return hasImportData(importParameters)
				&& getTypeService().isAssignableFrom(attributeDescriptor.getAttributeType().getCode(), MediaModel._TYPECODE);
	}

	protected boolean hasImportData(final ImportParameters importParameters)
	{
		return importParameters.isCellValueNotBlank() || importParameters.getMultiValueParameters().stream()
				.anyMatch(singleParams -> !StringUtils.isAllBlank(singleParams.get(AbstractExcelMediaImportTranslator.PARAM_CODE),
						singleParams.get(AbstractExcelMediaImportTranslator.PARAM_FILE_PATH)));
	}

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		if (importParameters.getMultiValueParameters().size() > 1)
		{
			return new ExcelValidationResult(new ValidationMessage(VALIDATION_DECLARED_MULTIPLE_REFERENCES));
		}

		final List<ValidationMessage> validationMessages = validateSingleValue(context,
				importParameters.getSingleValueParameters());

		return validationMessages.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(validationMessages);
	}

	@Override
	public List<ValidationMessage> validateSingleValue(final Map<String, Object> context, final Map<String, String> parameters)
	{
		final List<ValidationMessage> validations = new ArrayList<>();
		final String filePath = parameters.get(AbstractExcelMediaImportTranslator.PARAM_FILE_PATH);
		final String code = parameters.get(AbstractExcelMediaImportTranslator.PARAM_CODE);

		if (StringUtils.isAllBlank(filePath, code))
		{
			validations.add(new ValidationMessage(VALIDATION_PATH_AND_CODE_EMPTY, AbstractExcelMediaImportTranslator.PARAM_FILE_PATH,
					AbstractExcelMediaImportTranslator.PARAM_CODE));
		}

		if (StringUtils.isNotBlank(filePath))
		{
			final Set<String> zipEntries = (Set<String>) context.get(CTX_MEDIA_CONTENT_ENTRIES);
			if (zipEntries == null)
			{
				validations.add(
						new ValidationMessage(VALIDATION_MISSING_ZIP, AbstractExcelMediaImportTranslator.PARAM_FILE_PATH, filePath));

			}
			else if (!zipEntries.contains(filePath))
			{
				validations.add(new ValidationMessage(VALIDATION_MISSING_FILE_IN_ZIP, filePath));
			}
		}

		return validations;
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
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	public ExcelImportService getImportService()
	{
		return importService;
	}

	/**
	 * @deprecated since 1808, not used anymore
	 */
	@Deprecated
	@Required
	public void setImportService(final ExcelImportService importService)
	{
		this.importService = importService;
	}
}
