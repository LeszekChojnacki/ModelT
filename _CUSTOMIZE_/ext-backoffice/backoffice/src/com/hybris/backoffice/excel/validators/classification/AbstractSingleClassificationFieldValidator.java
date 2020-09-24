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
package com.hybris.backoffice.excel.validators.classification;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.ExcelAttributeValidator;


/**
 * All {@link ExcelAttributeValidator}s which validates a specific type should extend this abstract. It allows to
 * prevent invoking the validator more than 1 time, when given type is multivalue or range.
 */
public abstract class AbstractSingleClassificationFieldValidator implements ExcelAttributeValidator<ExcelClassificationAttribute>
{

	@Override
	public boolean canHandle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters)
	{
		return importParameters.isCellValueNotBlank() && isSimpleSingleValue(importParameters)
				&& canHandleSingle(excelAttribute, importParameters);
	}

	private static boolean isSimpleSingleValue(final @Nonnull ImportParameters importParameters)
	{
		return ExcelValidatorUtils.isNotRange(importParameters) && !ExcelValidatorUtils.hasUnit(importParameters)
				&& ExcelValidatorUtils.isNotMultivalue(importParameters);
	}

	public abstract boolean canHandleSingle(@Nonnull final ExcelClassificationAttribute excelAttribute,
			@Nonnull final ImportParameters importParameters);

}
