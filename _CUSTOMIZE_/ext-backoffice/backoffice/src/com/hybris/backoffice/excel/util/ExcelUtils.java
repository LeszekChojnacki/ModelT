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
package com.hybris.backoffice.excel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * Excel utility class.
 */
public class ExcelUtils
{
	public static final Pattern PATTERN_CELL_TOKENS = Pattern.compile("([^:\\[\\]]+)|(\\[.*?\\])|()");

	private ExcelUtils()
	{
	}

	/**
	 * Splits cell value into tokens
	 * 
	 * <pre>
	 *	 input: "a:b:c" output: {"a","b","c"}
	 *	 input: "a:b:[c:d]:f" output: {"a","b","c:d","f}
	 * </pre>
	 * 
	 * @param cellValue
	 *           cell value to split.
	 * @return array of tokens.
	 */
	public static String[] extractExcelCellTokens(final String cellValue)
	{
		if (StringUtils.isEmpty(cellValue))
		{
			return new String[0];
		}

		final List<String> tokens = new ArrayList<>();
		final Matcher matcher = PATTERN_CELL_TOKENS.matcher(cellValue);
		int lastGroupEnd = -1;
		while (matcher.find())
		{
			if (lastGroupEnd != matcher.start())
			{

				final String group = matcher.group().trim();
				if (group.length() > 1 && group.charAt(0) == '[' && group.charAt(group.length() - 1) == ']')
				{
					tokens.add(group.substring(1, group.length() - 1).trim());
				}
				else
				{
					tokens.add(group);
				}
			}
			lastGroupEnd = matcher.end();
		}
		return tokens.toArray(new String[0]);
	}
}
