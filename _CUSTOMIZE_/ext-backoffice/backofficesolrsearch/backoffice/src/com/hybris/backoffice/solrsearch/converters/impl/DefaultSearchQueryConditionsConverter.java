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
package com.hybris.backoffice.solrsearch.converters.impl;


import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hybris.backoffice.solrsearch.converters.SearchQueryConditionsConverter;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.backoffice.solrsearch.utils.BackofficeSolrUtil;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.SearchQueryConditionList;


public class DefaultSearchQueryConditionsConverter implements SearchQueryConditionsConverter
{

	@Override
	public List<SolrSearchCondition> convert(final List<? extends SearchQueryCondition> conditions, final Operator globalOperator,
			final IndexedType indexedType)
	{
		final List<SolrSearchCondition> converted = Lists.newArrayList();
		convertConditions(converted, conditions, globalOperator, indexedType);
		return converted;
	}

	protected void convertConditions(final List<SolrSearchCondition> convertedConditions,
			final List<? extends SearchQueryCondition> conditions, final Operator globalOperator, final IndexedType indexedType)
	{
		final Map<String, List<SearchQueryCondition>> conditionsByAttribute = groupConditionsByAttribute(conditions,
				indexedType.getIndexedProperties());
		final List<SearchQueryConditionList> nestedConditions = extractNestedConditions(conditions);


		conditionsByAttribute.forEach((attributeName, attrConditions) -> appendAttributeCondition(convertedConditions,
				attrConditions, indexedType.getIndexedProperties().get(attributeName), globalOperator));

		nestedConditions.forEach(conditionList -> {

			final List<SolrSearchCondition> innerConditions = Lists.newArrayList();
			final Operator operator = BackofficeSolrUtil.convertToSolrOperator(conditionList.getOperator());
			convertConditions(innerConditions, conditionList.getConditions(), operator, indexedType);
			final SolrSearchCondition query = new SolrSearchCondition(innerConditions, operator,
					conditionList.isFilteringCondition());
			convertedConditions.add(query);
		});
	}

	protected Map<String, List<SearchQueryCondition>> groupConditionsByAttribute(
			final List<? extends SearchQueryCondition> conditions, final Map<String, IndexedProperty> properties)
	{
		final Predicate<SearchQueryCondition> indexedAttribute = condition -> condition.getDescriptor() != null
				&& properties.get(condition.getDescriptor().getAttributeName()) != null;

		return conditions.stream().filter(indexedAttribute)
				.collect(Collectors.groupingBy(condition -> condition.getDescriptor().getAttributeName()));
	}

	protected List<SearchQueryConditionList> extractNestedConditions(final List<? extends SearchQueryCondition> conditions)
	{
		return conditions.stream().filter(c -> c instanceof SearchQueryConditionList).map(c -> (SearchQueryConditionList) c)
				.collect(Collectors.toList());
	}

	protected void appendAttributeCondition(final List<SolrSearchCondition> convertedConditions,
			final List<SearchQueryCondition> conditions, final IndexedProperty property, final Operator operator)
	{
		if (property.isLocalized())
		{
			final Map<Locale, List<SearchQueryCondition>> conditionsByLanguage = splitConditionsByLanguage(conditions);
			conditionsByLanguage.forEach((language, localizedConditions) -> {

				final SolrSearchCondition condition = createConditionForProperty(property, localizedConditions, operator, language);
				convertedConditions.add(condition);
			});
		}
		else
		{
			final SolrSearchCondition query = createConditionForProperty(property, conditions, operator, null);
			convertedConditions.add(query);
		}
	}

	protected Map<Locale, List<SearchQueryCondition>> splitConditionsByLanguage(final List<SearchQueryCondition> conditions)
	{
		final Map<Locale, List<SearchQueryCondition>> conditionsByLanguage = Maps.newHashMap();
		conditions.forEach(condition -> {

			final Locale language = extractValueLocale(condition.getValue());
			List<SearchQueryCondition> searchQueryConditions = conditionsByLanguage.get(language);
			if (searchQueryConditions == null)
			{
				searchQueryConditions = Lists.newArrayList();
				conditionsByLanguage.put(language, searchQueryConditions);
			}
			searchQueryConditions.add(condition);
		});
		return conditionsByLanguage;
	}

	protected SolrSearchCondition createConditionForProperty(final IndexedProperty indexedProperty,
			final List<SearchQueryCondition> conditions, final Operator operator, final Locale locale)
	{
		final SolrSearchCondition convertedCondition = new SolrSearchCondition(indexedProperty.getName(), indexedProperty.getType(),
				indexedProperty.isMultiValue(), locale, operator,
				conditions.stream().allMatch(SearchQueryCondition::isFilteringCondition));

		conditions.forEach(condition -> {
			final Object value = locale != null ? extractLocalizedValue(condition.getValue()) : condition.getValue();

			if (value != null || !condition.getOperator().isRequireValue())
			{
				convertedCondition.addConditionValue(value, condition.getOperator());
			}
		});
		return convertedCondition;
	}

	protected Object extractLocalizedValue(final Object value)
	{
		if (value instanceof Map)
		{
			final Map localizedValue = (Map) value;
			if (localizedValue.size() == 1)
			{
				return localizedValue.values().iterator().next();
			}
		}
		return value;
	}

	protected Locale extractValueLocale(final Object value)
	{
		if (value instanceof Map)
		{
			final Map<Locale, ?> map = (Map<Locale, ?>) value;
			if (map.size() == 1)
			{
				return map.keySet().iterator().next();
			}
		}
		return null;
	}

}
