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
package com.hybris.backoffice.solrsearch.resolvers;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;


/**
 * Formatter that can change case of a given {@link String}
 */
public class CaseFormat extends Format
{
	/**
	 * Change of case to be made
	 */
	public enum Case
	{
		/**
		 * To lower case {@link StringUtils#lowerCase(String)}
		 */
		TO_LOWER(StringUtils::lowerCase),
		/**
		 * To upper case {@link StringUtils#upperCase(String)}
		 */
		TO_UPPER(StringUtils::upperCase),
		/**
		 * Swap case {@link StringUtils#swapCase(String)}
		 */
		SWAP(StringUtils::swapCase);

		private final Function<String, String> function;

		Case(final Function<String, String> function)
		{
			this.function = function;
		}

		public String apply(final String string)
		{
			return function.apply(string);
		}
	}

	private final Case aCase;

	/**
	 * Costructor allowing to choose case change
	 * @param aCase {@link Case}
	 */
	public CaseFormat(final Case aCase)
	{
		this.aCase = aCase;
	}

	@Override
	public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos)
	{
		if (obj != null)
		{
			toAppendTo.append(aCase.apply(obj.toString()));
		}

		return toAppendTo;
	}

	@Override
	public Object parseObject(final String source, final ParsePosition pos)
	{
		if (source != null)
		{
			pos.setIndex(source.length() > 0 ? source.length() : 1);
		}
		else
		{
			pos.setIndex(1);
		}

		return source;
	}
}
