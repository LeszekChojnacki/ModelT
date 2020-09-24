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
package com.hybris.backoffice.excel.template;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;


/**
 * {@inheritDoc} The pattern used for a collection of "apple","banana","orange" looks as follows: <code>
 *     {apple},{banana},{orange}
 * </code>
 */
public class ExcelCollectionFormatter implements CollectionFormatter
{
	private static final Pattern BETWEEN_CURLY_BRACES_PATTERN = Pattern.compile("\\{(.+)},*"); // e.g. "{something},"
	private static final int SPLIT_LIMIT = 1_000;
	private static final String SPLIT_REGEX = "(?<=}),(?=\\{)"; // splits by "},{" keeping curly braces on elements
	private static final String JOIN_CHARACTER = ",";
	private static final UnaryOperator<String> MAP_TO_CURLY_BRACES_FORMAT = text -> "{" + StringUtils.trim(text) + "}";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String formatToString(final @Nonnull Collection<String> collection)
	{
		return collection.stream() //
				.map(MAP_TO_CURLY_BRACES_FORMAT) //
				.distinct() //
				.collect(Collectors.joining(JOIN_CHARACTER));
	}

	/**
	 * {@inheritDoc} The maximum number of elements extracted is 1000.
	 */
	@Override
	public Set<String> formatToCollection(final @Nonnull String string)
	{
		final Set<String> objects = new LinkedHashSet<>();
		for (final String element : string.split(SPLIT_REGEX, SPLIT_LIMIT))
		{
			final Matcher matcher = BETWEEN_CURLY_BRACES_PATTERN.matcher(element);
			if (matcher.find())
			{
				objects.add(matcher.group(1));
			}
		}
		return objects;
	}
}
