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

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


public class DefaultExcelValidationEngineAwareValidator implements ExcelValidationEngineAwareValidator
{

	private Collection<ExcelValidationEngineAwareStrategy> strategies;

	public ExcelValidationResult validate(final ExcelAttribute excelAttribute, final ImportParameters importParameters)
	{
		final Optional<ExcelValidationEngineAwareStrategy> foundStrategy = strategies.stream()
				.filter(strategy -> strategy.canHandle(importParameters, excelAttribute)).findFirst();
		if (foundStrategy.isPresent())
		{
			return foundStrategy.get().validate(importParameters, excelAttribute);
		}
		return ExcelValidationResult.SUCCESS;
	}

	public Collection<ExcelValidationEngineAwareStrategy> getStrategies()
	{
		return strategies;
	}

	@Required
	public void setStrategies(final Collection<ExcelValidationEngineAwareStrategy> strategies)
	{
		this.strategies = strategies;
	}
}
