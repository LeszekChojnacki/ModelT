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

import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.FreeTextFuzzyQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextPhraseQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextWildcardQueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


public class MultiFieldFreeTextQueryBuilder extends AbstractFreeTextQueryBuilder
{
	private static final Logger LOG = Logger.getLogger(MultiFieldFreeTextQueryBuilder.class);

	private FieldNameTranslator fieldNameTranslator;

	@Override
	public String buildQuery(final SearchQuery searchQuery)
	{
		if (StringUtils.isBlank(searchQuery.getUserQuery()))
		{
			return StringUtils.EMPTY;
		}

		final Map<String, List<String>> queryFields = new LinkedHashMap<>();

		final List<QueryValue> terms = prepareTerms(searchQuery);
		final List<QueryValue> phraseQueries = preparePhraseQueries(searchQuery);

		addFreeTextQuery(searchQuery, terms, queryFields);
		addFreeTextFuzzyQuery(searchQuery, terms, queryFields);
		addFreeTextWildCardQuery(searchQuery, terms, queryFields);
		addFreeTextPhraseQuery(searchQuery, phraseQueries, queryFields);

		final String query = buildQuery(searchQuery, queryFields);

		LOG.debug(query);

		return query;
	}

	protected void addFreeTextQuery(final SearchQuery searchQuery, final List<QueryValue> terms,
			final Map<String, List<String>> queryFields)
	{
		final List<FreeTextQueryField> fields = searchQuery.getFreeTextQueries();

		for (final FreeTextQueryField field : fields)
		{
			String boostString = "";
			if (field.getBoost() != null)
			{
				boostString = "^" + field.getBoost();
			}

			for (final QueryValue term : terms)
			{
				if (shouldIncludeTerm(term, field.getMinTermLength()))
				{
					addQueryField(field.getField(), term.getEscapedValue() + boostString, queryFields);
				}
			}
		}
	}

	protected void addFreeTextFuzzyQuery(final SearchQuery searchQuery, final List<QueryValue> terms,
			final Map<String, List<String>> queryFields)
	{
		final List<FreeTextFuzzyQueryField> fields = searchQuery.getFreeTextFuzzyQueries();

		for (final FreeTextFuzzyQueryField field : fields)
		{
			String boostString = "";
			if (field.getBoost() != null)
			{
				boostString = "^" + field.getBoost();
			}

			for (final QueryValue term : terms)
			{
				if (shouldIncludeTerm(term, field.getMinTermLength()) && shouldIncludeFuzzyQuery(term))
				{
					addQueryField(field.getField(),
							term.getEscapedValue() + "~" + (((field.getFuzziness() == null) ? "" : field.getFuzziness()) + boostString),
							queryFields);
				}
			}
		}
	}

	protected void addFreeTextWildCardQuery(final SearchQuery searchQuery, final List<QueryValue> terms,
			final Map<String, List<String>> queryFields)
	{
		final List<FreeTextWildcardQueryField> fields = searchQuery.getFreeTextWildcardQueries();

		for (final FreeTextWildcardQueryField field : fields)
		{
			String boostString = "";
			if (field.getBoost() != null)
			{
				boostString = "^" + field.getBoost();
			}

			for (final QueryValue term : terms)
			{
				if (shouldIncludeTerm(term, field.getMinTermLength()) && shouldIncludeWildcardQuery(term))
				{
					final String value = applyWildcardType(term.getEscapedValue(), field.getWildcardType());
					addQueryField(field.getField(), value + boostString, queryFields);
				}
			}
		}
	}

	protected void addFreeTextPhraseQuery(final SearchQuery searchQuery, final List<QueryValue> phraseQueries,
			final Map<String, List<String>> queryFields)
	{
		final List<FreeTextPhraseQueryField> fields = searchQuery.getFreeTextPhraseQueries();

		for (final FreeTextPhraseQueryField field : fields)
		{
			String slopString = "";
			if (field.getSlop() != null)
			{
				slopString = "~" + field.getSlop();
			}

			String boostString = "";
			if (field.getBoost() != null)
			{
				boostString = "^" + field.getBoost();
			}

			for (final QueryValue phraseQuery : phraseQueries)
			{
				addQueryField(field.getField(), phraseQuery.getEscapedValue() + slopString + boostString, queryFields);
			}
		}
	}

	protected void addQueryField(final String fieldName, final String fieldValue, final Map<String, List<String>> queryFields)
	{
		List<String> fieldValues = queryFields.get(fieldName);
		if (fieldValues == null)
		{
			fieldValues = new ArrayList<>();
			queryFields.put(fieldName, fieldValues);
		}

		fieldValues.add(fieldValue);
	}

	protected String buildQuery(final SearchQuery searchQuery, final Map<String, List<String>> queryFields)
	{
		final List<String> joinedQueries = new ArrayList<>();

		for (final Map.Entry<String, List<String>> entry : queryFields.entrySet())
		{
			final String field = translateField(entry.getKey(), searchQuery);
			final List<String> values = entry.getValue();

			if (values.size() == 1)
			{
				joinedQueries.add("(" + field + ":" + values.iterator().next() + ")");
			}
			else
			{
				joinedQueries.add("(" + field + ":(" + StringUtils.join(values, SearchQuery.Operator.OR.getName()) + "))");
			}
		}

		return StringUtils.join(joinedQueries, SearchQuery.Operator.OR.getName());
	}

	protected String translateField(final String field, final SearchQuery searchQuery)
	{
		return escape(fieldNameTranslator.translate(searchQuery, field, FieldType.INDEX));
	}

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}
}
