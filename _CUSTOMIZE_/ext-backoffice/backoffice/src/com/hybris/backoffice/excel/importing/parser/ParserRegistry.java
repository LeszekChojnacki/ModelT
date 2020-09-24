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


/**
 * Registry of {@link ImportParameterParser}s
 */
public interface ParserRegistry
{

	/**
	 * Returns {@link ImportParameterParser} for given referenceFormat. If no parser matches given referenceFormat then a
	 * {@link RuntimeException} is thrown.
	 *
	 * @param referenceFormat
	 *           format to check
	 * @return parser which matches to given referenceFormat
	 */
	ImportParameterParser getParser(@Nonnull final String referenceFormat);

}
