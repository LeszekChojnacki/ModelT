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
import de.hybris.platform.core.model.type.CollectionTypeModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.AbstractExcelMediaImportTranslator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Validator which checks if media import data is correct. Checks:
 * <ul>
 * <li>if at least one {@link AbstractExcelMediaImportTranslator#PARAM_FILE_PATH} or
 * {@link AbstractExcelMediaImportTranslator#PARAM_CODE} is defined</li>
 * <li>if given {@link AbstractExcelMediaImportTranslator#PARAM_FILE_PATH} exists in uploaded content</li>
 * </ul>
 */
public class ExcelMediaCollectionImportValidator extends ExcelMediaImportValidator
{

	private List<ExcelSingleMediaValidator> singleMediaValidators = Collections.emptyList();

	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return hasImportData(importParameters) && attributeDescriptor.getAttributeType() instanceof CollectionTypeModel
				&& getTypeService().isAssignableFrom(
						((CollectionTypeModel) attributeDescriptor.getAttributeType()).getElementType().getCode(),
						MediaModel._TYPECODE);
	}

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final List<ValidationMessage> validationMessages = new ArrayList<>();

		for (final Map<String, String> parameters : importParameters.getMultiValueParameters())
		{
			final Collection<ValidationMessage> validationMessagesFromValidators = singleMediaValidators.stream()
					.map(validator -> validator.validateSingleValue(context, parameters)).flatMap(Collection::stream)
					.collect(Collectors.toList());
			validationMessages.addAll(validationMessagesFromValidators);
		}
		return validationMessages.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(validationMessages);
	}

	public List<ExcelSingleMediaValidator> getSingleMediaValidators()
	{
		return singleMediaValidators;
	}

	// optional
	public void setSingleMediaValidators(final List<ExcelSingleMediaValidator> singleMediaValidators)
	{
		this.singleMediaValidators = singleMediaValidators;
	}
}
