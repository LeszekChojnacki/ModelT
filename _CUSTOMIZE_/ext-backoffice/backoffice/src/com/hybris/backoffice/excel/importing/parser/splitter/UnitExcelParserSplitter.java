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
package com.hybris.backoffice.excel.importing.parser.splitter;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Allows to split given input with unit into 2 values - first value is attribute's value and the second is unit's
 * value.<br/>
 * E.g. for given input "10.04.2018 05:55:12:132", which is a Date value with unit "132", array with 2 values is
 * returned. First value of the array equals "10.04.2018 05:55:12" and second "132".
 */
public class UnitExcelParserSplitter implements ExcelParserSplitter
{

	@Override
	public String[] apply(final @Nonnull String s)
	{
		if (StringUtils.isBlank(s))
		{
			return new String[] {};
		}

		final int unitSeparatorIndex = s.lastIndexOf(ExcelTemplateConstants.REFERENCE_PATTERN_SEPARATOR);
		final String left = s.substring(0, unitSeparatorIndex);
		final String right = s.substring(unitSeparatorIndex + 1);
		return new String[]
		{ left, right };
	}
}
