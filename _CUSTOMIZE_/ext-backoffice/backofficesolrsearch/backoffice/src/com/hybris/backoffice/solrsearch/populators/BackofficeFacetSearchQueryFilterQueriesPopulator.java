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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.populators.FacetSearchQueryFilterQueriesPopulator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.core.util.Validate;


public class BackofficeFacetSearchQueryFilterQueriesPopulator extends FacetSearchQueryFilterQueriesPopulator
{

	public static final String QUOTE = "\"";
	public static final String WILDCARD_ANY_STRING = "*";
	public static final String FQ_VALUE_GROUP_PREFIX = "(";
	public static final String FQ_VALUE_GROUP_SUFFIX = ")";
	public static final String FQ_FIELD_VALUE_SEPARATOR = ":";
	public static final String FQ_CONDITION_GREATER = " TO *";
	public static final String FQ_CONDITION_LESS = "* TO ";
	public static final String FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX = "[";
	public static final String FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX = "]";
	public static final String FQ_CONDITION_BETWEEN_EXCLUSIVE_PREFIX = "{";
	public static final String FQ_CONDITION_BETWEEN_EXCLUSIVE_SUFFIX = "}";

	private FieldNamePostProcessor fieldNamePostProcessor;
	private Map<String, Function<Serializable, String>> conditionValueConverterMap;


	@Override
	protected void addRawQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		super.addRawQueries(searchQuery, queries);
		if (searchQuery instanceof BackofficeSearchQuery)
		{
			queries.addAll(buildFilterQueries(searchQuery, ((BackofficeSearchQuery) searchQuery).getFilterQueryConditions()));
		}
	}

	protected List<String> buildFilterQueries(final SearchQuery searchQuery, final List<SolrSearchCondition> conditions)
	{
		return conditions.stream().map(condition -> convertSearchConditionToFilterQuery(searchQuery, condition))
				.filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	protected String convertSearchConditionToFilterQuery(final SearchQuery searchQuery, final SolrSearchCondition condition)
	{
		final String fieldName = convertAttributeNameToFieldName(searchQuery, condition);
		final String flatFQValue = convertSearchConditionValuesToFilterQueryValue(condition);

		return fieldName.concat(FQ_FIELD_VALUE_SEPARATOR).concat(flatFQValue);
	}

	protected String convertAttributeNameToFieldName(final SearchQuery searchQuery, final SolrSearchCondition condition)
	{
		final String translatedFieldName = getFieldNameTranslator().translate(searchQuery, condition.getAttributeName(),
				FieldNameProvider.FieldType.INDEX);
		return getFieldNamePostProcessor().process(searchQuery, condition.getLanguage(), translatedFieldName);
	}

	protected String convertSearchConditionValuesToFilterQueryValue(final SolrSearchCondition condition)
	{
		return condition.getConditionValues().stream().map(this::convertConditionValueToString).filter(StringUtils::isNotEmpty)
				.collect(Collectors.joining(condition.getOperator().getName(), FQ_VALUE_GROUP_PREFIX, FQ_VALUE_GROUP_SUFFIX));
	}

	protected String convertConditionValueToString(final SolrSearchCondition.ConditionValue conditionValue)
	{
		final Serializable value = conditionValue.getValue();

		if (value == null)
		{
			return "";
		}

		final String convertedValue = getConditionValueConverter(value.getClass().getName()).apply(value);

		switch (conditionValue.getComparisonOperator())
		{
			case EQUALS:
				return encloseString(convertedValue, QUOTE);
			case CONTAINS:
				return encloseString(convertedValue, WILDCARD_ANY_STRING);
			case GREATER:
				return FQ_CONDITION_BETWEEN_EXCLUSIVE_PREFIX + convertedValue + FQ_CONDITION_GREATER
						+ FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX;
			case GREATER_OR_EQUAL:
				return FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + convertedValue + FQ_CONDITION_GREATER
						+ FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX;
			case LESS:
				return FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + FQ_CONDITION_LESS + convertedValue + FQ_CONDITION_BETWEEN_EXCLUSIVE_SUFFIX;
			case LESS_OR_EQUAL:
				return FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + FQ_CONDITION_LESS + convertedValue
						+ FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX;
			default:
		}

		return convertedValue;
	}

	/**
	 * Encloses the {@code value} string in {@code enclosingString}. Resulting string is
	 * {@code enclosingString + value + enclosingString}. <br>
	 * <br>
	 * {@code enclosingString} cannot be null.
	 */
	protected String encloseString(String value, final String enclosingString)
	{
		Validate.notNull("enclosingString cannot be null", enclosingString);
		if (enclosingString.equals(QUOTE))
		{
			value = value.replace("\\","\\\\");
		}
		return enclosingString.concat(value).concat(enclosingString);
	}

	protected Function<Serializable, String> getConditionValueConverter(final String type)
	{
		return getConditionValueConverterMap().getOrDefault(type, (serializable -> Objects.toString(serializable, "")));
	}

	protected FieldNamePostProcessor getFieldNamePostProcessor()
	{
		return fieldNamePostProcessor;
	}

	@Required
	public void setFieldNamePostProcessor(final FieldNamePostProcessor fieldNamePostProcessor)
	{
		this.fieldNamePostProcessor = fieldNamePostProcessor;
	}

	protected Map<String, Function<Serializable, String>> getConditionValueConverterMap()
	{
		return this.conditionValueConverterMap;
	}

	@Required
	public void setConditionValueConverterMap(final Map<String, Function<Serializable, String>> conditionValueConverterMap)
	{
		this.conditionValueConverterMap = conditionValueConverterMap;
	}


}
