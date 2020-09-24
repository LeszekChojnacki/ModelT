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

import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.FreeTextFuzzyQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextPhraseQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextWildcardQueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation for the {@FreeTextQueryBuilder} interface which returns lucene query string.
 */
public class DisMaxFreeTextQueryBuilder extends AbstractFreeTextQueryBuilder
{
	protected enum FieldType
	{
		PHRASE_QUERY, TEXT_QUERY, FUZZY_QUERY, WILD_CARD_QUERY
	}

	private static final Logger LOG = Logger.getLogger(DisMaxFreeTextQueryBuilder.class);

	public static final String TIE = "tie";
	public static final float TIE_DEFAULT_VALUE = 0.0f;

	public static final String GROUP_BY_QUERY_TYPE = "groupByQueryType";
	public static final boolean GROUP_BY_QUERY_TYPE_DEFAULT_VALUE = true;

	private FieldNameTranslator fieldNameTranslator;

	@Override
	public String buildQuery(final SearchQuery searchQuery)
	{
		if (StringUtils.isBlank(searchQuery.getUserQuery()))
		{
			return StringUtils.EMPTY;
		}

		final Map<String, List<FieldParameter>> queryFields = new LinkedHashMap<>();

		final String tieParam = searchQuery.getFreeTextQueryBuilderParameters().get(TIE);
		final float tie = StringUtils.isNotEmpty(tieParam) ? Float.valueOf(tieParam) : TIE_DEFAULT_VALUE;

		final String groupedByQueryTypeParam = searchQuery.getFreeTextQueryBuilderParameters().get(GROUP_BY_QUERY_TYPE);
		final boolean groupByQueryType = StringUtils.isNotEmpty(groupedByQueryTypeParam) ? Boolean.valueOf(groupedByQueryTypeParam)
				: GROUP_BY_QUERY_TYPE_DEFAULT_VALUE;

		final List<QueryValue> terms = prepareTerms(searchQuery);
		final List<QueryValue> phraseQueries = preparePhraseQueries(searchQuery);

		addFreeTextQuery(searchQuery, terms, groupByQueryType, queryFields);
		addFreeTextFuzzyQuery(searchQuery, terms, groupByQueryType, queryFields);
		addFreeTextWildCardQuery(searchQuery, terms, groupByQueryType, queryFields);
		addFreeTextPhraseQuery(searchQuery, phraseQueries, groupByQueryType, queryFields);

		final String query = buildQuery(queryFields, tie, searchQuery);

		LOG.debug(query);

		return query;
	}

	protected void addFreeTextQuery(final SearchQuery searchQuery, final List<QueryValue> terms, final boolean groupByQueryType,
			final Map<String, List<FieldParameter>> queryFields)
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
					addQueryField(term.getValue(), FieldType.TEXT_QUERY, field.getField(), term.getEscapedValue() + boostString,
							groupByQueryType, queryFields);
				}
			}
		}
	}

	protected void addFreeTextFuzzyQuery(final SearchQuery searchQuery, final List<QueryValue> terms,
			final boolean groupByQueryType, final Map<String, List<FieldParameter>> queryFields)
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
					addQueryField(term.getValue(), FieldType.FUZZY_QUERY, field.getField(),
							term.getEscapedValue() + "~" + (((field.getFuzziness() == null) ? "" : field.getFuzziness()) + boostString),
							groupByQueryType, queryFields);
				}
			}
		}
	}

	protected void addFreeTextWildCardQuery(final SearchQuery searchQuery, final List<QueryValue> terms,
			final boolean groupByQueryType, final Map<String, List<FieldParameter>> queryFields)
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
					addQueryField(term.getValue(), FieldType.WILD_CARD_QUERY, field.getField(), value + boostString, groupByQueryType,
							queryFields);
				}
			}
		}
	}

	protected void addFreeTextPhraseQuery(final SearchQuery searchQuery, final List<QueryValue> phraseQueries,
			final boolean groupByQueryType, final Map<String, List<FieldParameter>> queryFields)
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
				addQueryField("", FieldType.PHRASE_QUERY, field.getField(), phraseQuery.getEscapedValue() + slopString + boostString,
						groupByQueryType, queryFields);
			}
		}
	}

	protected void addQueryField(final String term, final FieldType fieldType, final String fieldName, final String fieldValue,
			final boolean groupByQueryType, final Map<String, List<FieldParameter>> queryFields)
	{
		final String key = groupByQueryType ? term + "_" + fieldType : term;

		List<FieldParameter> fieldValues = queryFields.get(key);
		if (fieldValues == null)
		{
			fieldValues = new ArrayList<>();
			queryFields.put(key, fieldValues);
		}

		fieldValues.add(new FieldParameter(fieldName, fieldValue));
	}

	protected String buildQuery(final Map<String, List<FieldParameter>> queryFields, final float tie,
			final SearchQuery searchQuery)
	{
		final List<String> joinedQueries = new ArrayList<>();
		final Map<String, String> translatedFields = new HashMap<>();

		for (final Map.Entry<String, List<FieldParameter>> entry : queryFields.entrySet())
		{
			final StringBuilder stringBuilder = new StringBuilder();
			final List<FieldParameter> fields = entry.getValue();

			if (!fields.isEmpty())
			{
				final List<String> groupedQueries = new ArrayList<>();

				for (final FieldParameter field : fields)
				{
					final String translatedField = translateField(field.getFieldName(), translatedFields, searchQuery);

					groupedQueries.add("(" + translatedField + ":" + field.getFieldValue() + ")");
				}

				stringBuilder.append('(');
				stringBuilder.append(
						StringUtils.join(groupedQueries.toArray(new String[groupedQueries.size()]), SearchQuery.Operator.OR.getName()));
				stringBuilder.append(')');
			}

			joinedQueries.add(stringBuilder.toString());
		}

		if (CollectionUtils.isEmpty(joinedQueries))
		{
			return StringUtils.EMPTY;
		}

		return "{!multiMaxScore tie=" + tie + "}" + StringUtils.join(joinedQueries, SearchQuery.Operator.OR.getName());
	}

	protected String translateField(final String fieldName, final Map<String, String> translatedFields,
			final SearchQuery searchQuery)
	{
		String translatedField = translatedFields.get(fieldName);
		if (StringUtils.isEmpty(translatedField))
		{
			translatedField = escape(fieldNameTranslator.translate(searchQuery, fieldName, FieldNameProvider.FieldType.INDEX));
			translatedFields.put(fieldName, translatedField);
		}

		return translatedField;
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

	protected static final class FieldParameter
	{
		private final String fieldName;
		private final String fieldValue;

		public FieldParameter(final String fieldName, final String fieldValue)
		{
			this.fieldName = fieldName;
			this.fieldValue = fieldValue;
		}

		public String getFieldName()
		{
			return fieldName;
		}

		public String getFieldValue()
		{
			return fieldValue;
		}
	}
}
