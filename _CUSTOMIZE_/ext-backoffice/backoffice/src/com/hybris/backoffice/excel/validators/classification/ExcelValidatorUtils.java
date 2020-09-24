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

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.importing.parser.RangeParserUtils;


class ExcelValidatorUtils
{

	private ExcelValidatorUtils()
	{
	}

	static boolean isMultivalue(final ImportParameters importParameters)
	{
		return StringUtils.contains(String.valueOf(importParameters.getCellValue()), ImportParameters.MULTIVALUE_SEPARATOR);
	}

	static boolean isNotRange(final ImportParameters importParameters)
	{
		return !RangeParserUtils.RANGE_PATTERN.matcher(String.valueOf(importParameters.getCellValue())).matches();
	}

	static boolean isNotMultivalue(final ImportParameters importParameters)
	{
		return !isMultivalue(importParameters);
	}

	static boolean hasUnit(final ImportParameters importParameters)
	{
		return importParameters.getSingleValueParameters().containsKey(ExcelUnitUtils.UNIT_KEY);
	}
}
