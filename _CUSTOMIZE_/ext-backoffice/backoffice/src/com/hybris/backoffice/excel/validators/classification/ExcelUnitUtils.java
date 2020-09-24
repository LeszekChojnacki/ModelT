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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.data.ImportParameters;


class ExcelUnitUtils
{

	static final String UNIT_KEY = "unit";
	static final String VALUE_KEY = "value";

	private ExcelUnitUtils()
	{
		throw new AssertionError();
	}

	static ImportParameters getImportParametersForValue(final ImportParameters importParameters, final String cellValue)
	{
		final String typeCode = importParameters.getTypeCode();
		final String isoCode = importParameters.getIsoCode();
		final String entryRef = importParameters.getEntryRef();
		final List<Map<String, String>> multiValueParametersWithoutUnits = importParameters.getMultiValueParameters() //
				.stream() //
				.peek(m -> m.remove(UNIT_KEY)) //
				.collect(Collectors.toList());
		return new ImportParameters(typeCode, isoCode, cellValue, entryRef, multiValueParametersWithoutUnits);
	}

	static String extractUnitFromParams(final Map<String, String> params)
	{
		return params.getOrDefault(UNIT_KEY, StringUtils.EMPTY);
	}

	static String extractValueFromParams(final Map<String, String> params)
	{
		return params.getOrDefault(VALUE_KEY, StringUtils.EMPTY);
	}
}
