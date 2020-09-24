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
package com.hybris.backoffice.excel.translators;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelValidator;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Abstract class for translator which can have validators.
 * 
 * @param <T>
 *           - class which should be handled by translator
 */
public abstract class AbstractValidationAwareTranslator<T> implements ExcelValueTranslator<T>
{

	protected List<ExcelValidator> validators = new ArrayList<>();

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final List<ExcelValidationResult> validationResults = getValidators().stream()
				.filter(validator -> validator.canHandle(importParameters, attributeDescriptor))
				.map(validator -> validator.validate(importParameters, attributeDescriptor, context)).collect(Collectors.toList());

		final Optional<ValidationMessage> validationMessageHeader = validationResults.stream().filter(e -> e.getHeader() != null)
				.map(ExcelValidationResult::getHeader).findFirst();
		final List<ValidationMessage> validationMessages = validationResults.stream()
				.flatMap(validationResult -> validationResult.getValidationErrors().stream()).collect(Collectors.toList());

		final ExcelValidationResult excelValidationResult = new ExcelValidationResult(new ArrayList<>());
		validationMessageHeader.ifPresent(excelValidationResult::setHeader);

		excelValidationResult.setValidationErrors(validationMessages);
		return excelValidationResult;
	}

	/**
	 * Returns list of validators for current translator.
	 * 
	 * @return list of validators for current translator.
	 */
	public List<ExcelValidator> getValidators()
	{
		return validators;
	}

	/**
	 * Sets list of validators for current translator.
	 * 
	 * @param validators list of validators for current translator.
	 */
	public void setValidators(final List<ExcelValidator> validators)
	{
		this.validators = validators;
	}
}
