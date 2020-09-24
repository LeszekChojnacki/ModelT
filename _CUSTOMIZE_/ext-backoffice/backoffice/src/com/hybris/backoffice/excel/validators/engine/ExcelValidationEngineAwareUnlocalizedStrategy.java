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
package com.hybris.backoffice.excel.validators.engine;

import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Excel validation strategy for unlocalized fields which uses validation engine.
 */
public class ExcelValidationEngineAwareUnlocalizedStrategy extends ExcelAbstractValidationEngineAwareStrategy
{

	@Override
	public boolean canHandle(final ImportParameters importParameters, final ExcelAttribute excelAttribute)
	{
		return !excelAttribute.isLocalized();
	}

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters, final ExcelAttribute excelAttribute)
	{
		final List<ValidationMessage> mappedErrors = new ArrayList<>();
		final Collection<HybrisConstraintViolation> validationErrors = validateValue(importParameters, excelAttribute);
		if (CollectionUtils.isNotEmpty(validationErrors))
		{
			for (final HybrisConstraintViolation error : validationErrors)
			{
				mappedErrors.add(new ValidationMessage(error.getLocalizedMessage(), error.getViolationSeverity()));
			}
		}
		return mappedErrors.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(mappedErrors);
	}
}
