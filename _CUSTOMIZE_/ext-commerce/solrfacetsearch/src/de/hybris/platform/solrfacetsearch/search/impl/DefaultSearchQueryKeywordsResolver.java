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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.Keyword;
import de.hybris.platform.solrfacetsearch.search.KeywordModifier;
import de.hybris.platform.solrfacetsearch.search.SearchQueryKeywordsResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * Default implementation of the interface {@Link SearchQueryKeywordsResolver}
 */
public class DefaultSearchQueryKeywordsResolver implements SearchQueryKeywordsResolver
{
	// regular expression to split based on whitespaces but not spaces inside quotes
	protected static final Pattern SPLIT_REGEX = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

	// regular expression to match one or more whitespaces
	protected static final Pattern WS_REGEX = Pattern.compile("\\s+");

	@Override
	public List<Keyword> resolveKeywords(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final String userQuery)
	{
		final List<Keyword> keywords = new ArrayList<>();

		if (StringUtils.isNotBlank(userQuery))
		{
			final Matcher splitMatcher = SPLIT_REGEX.matcher(userQuery);
			while (splitMatcher.find())
			{
				if (StringUtils.isNotBlank(splitMatcher.group(1)))
				{
					// add double-quoted string without the quotes
					final String value = WS_REGEX.matcher(splitMatcher.group(1)).replaceAll(" ");
					keywords.add(new Keyword(value, KeywordModifier.EXACT_MATCH));
				}
				else if (StringUtils.isNotBlank(splitMatcher.group(2)))
				{
					// add unquoted string
					final String value = splitMatcher.group(2);
					keywords.add(new Keyword(value));
				}
			}
		}

		return keywords;
	}
}
