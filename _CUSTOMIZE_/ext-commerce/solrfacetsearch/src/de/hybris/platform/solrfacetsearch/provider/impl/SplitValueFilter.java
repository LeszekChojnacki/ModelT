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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.provider.ValueFilter;

import java.util.Arrays;


/**
 * splits any resolved value around matches of a regular expression (only if the value is of type String)
 */
@UnitTest
public class SplitValueFilter implements ValueFilter
{
	public static final String SPLIT_PARAM = "split";
	public static final boolean SPLIT_PARAM_DEFAULT_VALUE = false;

	public static final String SPLIT_REGEX_PARAM = "splitRegex";
	public static final String SPLIT_REGEX_PARAM_DEFAULT_VALUE = "\\s+";

	/**
	 * @param batchContext
	 * 		- The batch context
	 * @param indexedProperty
	 * 		- The indexed properties that should contains the Bean Id of the formatter bean
	 * @param value
	 * 		- The object to apply Split on in case in case it is of type String
	 * @return Returns the same value Object in case it is not string
	 * and in case it is string return List of the splitted values
	 */
	@Override
	public Object doFilter(final IndexerBatchContext batchContext, final IndexedProperty indexedProperty, final Object value)
	{
		final boolean isStringValue = value instanceof String;
		final boolean split = ValueProviderParameterUtils.getBoolean(indexedProperty, SPLIT_PARAM, SPLIT_PARAM_DEFAULT_VALUE);

		if (isStringValue && split)
		{
			final String splitRegex = ValueProviderParameterUtils.getString(indexedProperty, SPLIT_REGEX_PARAM,
					SPLIT_REGEX_PARAM_DEFAULT_VALUE);

			final String[] attributeValues = ((String) value).split(splitRegex);

			return Arrays.asList(attributeValues);
		}

		return value;
	}
}
