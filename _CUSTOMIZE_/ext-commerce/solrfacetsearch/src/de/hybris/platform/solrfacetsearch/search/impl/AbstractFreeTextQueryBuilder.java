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

import de.hybris.platform.solrfacetsearch.config.WildcardType;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryBuilder;
import de.hybris.platform.solrfacetsearch.search.Keyword;
import de.hybris.platform.solrfacetsearch.search.KeywordModifier;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.solr.client.solrj.util.ClientUtils;


public abstract class AbstractFreeTextQueryBuilder implements FreeTextQueryBuilder
{
	protected List<QueryValue> prepareTerms(final SearchQuery searchQuery)
	{
		if (CollectionUtils.isEmpty(searchQuery.getKeywords()))
		{
			return Collections.emptyList();
		}

		final List<QueryValue> queryValues = new ArrayList<>();

		for (final Keyword keyword : searchQuery.getKeywords())
		{
			final Collection<KeywordModifier> modifiers = keyword.getModifiers();

			final String value = keyword.getValue();
			String escapedValue;

			if (modifiers.contains(KeywordModifier.EXACT_MATCH))
			{
				escapedValue = escapePhraseQuery(value);
			}
			else
			{
				escapedValue = escape(value);
			}

			queryValues.add(new QueryValue(value, escapedValue, keyword));
		}

		return queryValues;
	}

	protected List<QueryValue> preparePhraseQueries(final SearchQuery searchQuery)
	{
		if (CollectionUtils.isEmpty(searchQuery.getKeywords()))
		{
			return Collections.emptyList();
		}

		final StringJoiner phraseQuery = new StringJoiner(" ");

		for (final Keyword keyword : searchQuery.getKeywords())
		{
			if (keyword.getModifiers().contains(KeywordModifier.EXACT_MATCH))
			{
				return Collections.emptyList();
			}

			phraseQuery.add(keyword.getValue());
		}

		final String value = phraseQuery.toString();
		final String escapedValue = escapePhraseQuery(value);

		return Collections.singletonList(new QueryValue(value, escapedValue));
	}

	protected String escape(final String value)
	{
		if ("AND".equals(value) || "OR".equals(value) || "NOT".equals(value))
		{
			return "\\" + value;
		}

		return ClientUtils.escapeQueryChars(value);
	}

	protected String escapePhraseQuery(final String value)
	{
		return "\"" + escapeInnerPhraseQueryValue(value) + "\"";
	}

	protected String escapeInnerPhraseQueryValue(final String value)
	{
		return value
				.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("\\\\"))	//escape backslash
				.replaceAll("\"", "\\\\\"");										//escape quote
	}

	protected boolean shouldIncludeTerm(final QueryValue term, final Integer minTermLength)
	{
		return (minTermLength == null) || (term.getValue().length() >= minTermLength);
	}

	protected boolean shouldIncludeFuzzyQuery(final QueryValue term)
	{
		final Keyword keyword = term.getKeyword();
		return (keyword != null) && !keyword.getModifiers().contains(KeywordModifier.EXACT_MATCH);
	}

	protected boolean shouldIncludeWildcardQuery(final QueryValue term)
	{
		final Keyword keyword = term.getKeyword();
		return (keyword != null) && !keyword.getModifiers().contains(KeywordModifier.EXACT_MATCH);
	}

	protected String applyWildcardType(final String term, final WildcardType wildcardType)
	{
		final String defaultValue = "*" + term + "*";

		if (wildcardType != null)
		{
			switch (wildcardType)
			{
				case PREFIX:
					return "*" + term;
				case POSTFIX:
					return term + "*";
				case PREFIX_AND_POSTFIX:
					return "*" + term + "*";
				default:
					return defaultValue;
			}
		}

		return defaultValue;
	}

	protected static class QueryValue
	{
		private final String value;
		private final String escapedValue;
		private final Keyword keyword;

		public QueryValue(final String value, final String escapedValue)
		{
			this.value = value;
			this.escapedValue = escapedValue;
			this.keyword = null;
		}

		public QueryValue(final String value, final String escapedValue, final Keyword keyword)
		{
			this.value = value;
			this.escapedValue = escapedValue;
			this.keyword = keyword;
		}

		public String getValue()
		{
			return value;
		}

		public String getEscapedValue()
		{
			return escapedValue;
		}

		public Keyword getKeyword()
		{
			return keyword;
		}
	}
}
