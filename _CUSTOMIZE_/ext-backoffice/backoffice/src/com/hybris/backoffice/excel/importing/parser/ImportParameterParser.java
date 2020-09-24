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
package com.hybris.backoffice.excel.importing.parser;

import javax.annotation.Nonnull;

import org.springframework.core.Ordered;


public interface ImportParameterParser extends Ordered
{

	boolean matches(@Nonnull final String referenceFormat);

	DefaultValues parseDefaultValues(final String referenceFormat, final String defaultValues);

	ParsedValues parseValue(final String cellValue, final DefaultValues defaultValues);

	default ParsedValues parseValue(@Nonnull final String referenceFormat, final String defaultValues, final String values)
	{
		final DefaultValues parsedDefaultValues = parseDefaultValues(referenceFormat, defaultValues);
		return parseValue(values, parsedDefaultValues);
	}

}
