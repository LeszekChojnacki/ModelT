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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.SearchQueryConditionList;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class DefaultSearchQueryConditionsConverterTest
{
	public static final String ATTR_NAME = "name";
	public static final String ATTR_CODE = "code";
	public static final String ATTR_DESC = "desc";
	public static final String VAL_1 = "v1";
	public static final String VAL_2 = "v2";
	public static final String VAL_3 = "v3";
	private DefaultSearchQueryConditionsConverter conditionsConverter;
	@Mock
	private IndexedType indexedType;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		conditionsConverter = new DefaultSearchQueryConditionsConverter();
	}

	@Test
	public void testQueryWithOneAttributeAndMultipleValues()
	{
		final List<SearchQueryCondition> conditions = Lists.newArrayList();
		conditions.add(prepareCondition(ATTR_NAME, VAL_1, ValueComparisonOperator.EQUALS));
		conditions.add(prepareCondition(ATTR_NAME, VAL_2, ValueComparisonOperator.CONTAINS));
		conditions.add(prepareCondition(ATTR_NAME, VAL_3, ValueComparisonOperator.IS_NOT_EMPTY));

		final Map<String, IndexedProperty> indexedPropertyMap = Maps.newHashMap();
		indexedPropertyMap.put(ATTR_NAME, prepareIndexedProperty(ATTR_NAME, false));
		when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);

		final List<SolrSearchCondition> converted = conditionsConverter.convert(conditions, SearchQuery.Operator.AND, indexedType);
		assertThat(converted.size()).isEqualTo(1);
		assertThat(converted.get(0).getAttributeName()).isEqualTo(ATTR_NAME);
		assertThat(converted.get(0).getOperator()).isEqualTo(SearchQuery.Operator.AND);
		assertThat(converted.get(0).getConditionValues().size()).isEqualTo(3);
		assertThat(converted.get(0).getConditionValues().get(0).getValue()).isEqualTo(VAL_1);
		assertThat(converted.get(0).getConditionValues().get(1).getValue()).isEqualTo(VAL_2);
		assertThat(converted.get(0).getConditionValues().get(2).getValue()).isEqualTo(VAL_3);

		assertThat(converted.get(0).getConditionValues().get(0).getComparisonOperator()).isEqualTo(ValueComparisonOperator.EQUALS);
		assertThat(converted.get(0).getConditionValues().get(1).getComparisonOperator())
				.isEqualTo(ValueComparisonOperator.CONTAINS);
		assertThat(converted.get(0).getConditionValues().get(2).getComparisonOperator())
				.isEqualTo(ValueComparisonOperator.IS_NOT_EMPTY);
	}

	@Test
	public void testQueryWithManyAttributes()
	{
		final List<SearchQueryCondition> conditions = Lists.newArrayList();
		conditions.add(prepareCondition(ATTR_NAME, VAL_1, ValueComparisonOperator.EQUALS));
		conditions.add(prepareCondition(ATTR_CODE, VAL_2, ValueComparisonOperator.CONTAINS));
		conditions.add(prepareCondition(ATTR_DESC, VAL_3, ValueComparisonOperator.IS_NOT_EMPTY));

		final Map<String, IndexedProperty> indexedPropertyMap = Maps.newHashMap();
		indexedPropertyMap.put(ATTR_NAME, prepareIndexedProperty(ATTR_NAME, false));
		indexedPropertyMap.put(ATTR_CODE, prepareIndexedProperty(ATTR_CODE, false));
		indexedPropertyMap.put(ATTR_DESC, prepareIndexedProperty(ATTR_DESC, false));
		when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);

		final List<SolrSearchCondition> converted = conditionsConverter.convert(conditions, SearchQuery.Operator.AND, indexedType);
		assertThat(converted.size()).isEqualTo(3);
		assertThat(converted.get(0).getOperator()).isEqualTo(SearchQuery.Operator.AND);

		assertThat(containsConditionForName(converted, ATTR_NAME)).isTrue();
		assertThat(containsConditionForName(converted, ATTR_CODE)).isTrue();
		assertThat(containsConditionForName(converted, ATTR_DESC)).isTrue();


		assertThat(converted.get(0).getConditionValues().size()).isEqualTo(1);
		assertThat(converted.get(1).getConditionValues().size()).isEqualTo(1);
		assertThat(converted.get(2).getConditionValues().size()).isEqualTo(1);

		assertThat(containsConditionValue(converted, VAL_1)).isTrue();
		assertThat(containsConditionValue(converted, VAL_2)).isTrue();
		assertThat(containsConditionValue(converted, VAL_3)).isTrue();
	}

	@Test
	public void testWithLocalizedAttribute()
	{
		final Map<String, IndexedProperty> indexedPropertyMap = Maps.newHashMap();
		indexedPropertyMap.put(ATTR_NAME, prepareIndexedProperty(ATTR_NAME, true));
		when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);

		final Map<Locale, Object> localizedValue = Maps.newHashMap();
		localizedValue.put(Locale.ENGLISH, VAL_1);
		final List<SearchQueryCondition> conditions = Lists.newArrayList();
		conditions.add(prepareCondition(ATTR_NAME, localizedValue, ValueComparisonOperator.EQUALS));
		final List<SolrSearchCondition> converted = conditionsConverter.convert(conditions, SearchQuery.Operator.OR, indexedType);
		assertThat(converted.size()).isEqualTo(1);
		assertThat(converted.get(0).getAttributeName()).isEqualTo(ATTR_NAME);
		assertThat(converted.get(0).getConditionValues().size()).isEqualTo(1);
		assertThat(converted.get(0).getConditionValues().get(0).getValue(Locale.ENGLISH)).isEqualTo(VAL_1);
	}

	@Test
	public void testWithInnerConditions()
	{
		final List<SearchQueryCondition> conditions = Lists.newArrayList();
		conditions.add(prepareCondition(ATTR_NAME, VAL_1, ValueComparisonOperator.EQUALS));
		final SearchQueryConditionList inner = new SearchQueryConditionList();
		inner.setConditions(Lists.newArrayList(prepareCondition(ATTR_CODE, VAL_2, ValueComparisonOperator.CONTAINS),
				prepareCondition(ATTR_DESC, VAL_3, ValueComparisonOperator.OR)));
		conditions.add(inner);

		final Map<String, IndexedProperty> indexedPropertyMap = Maps.newHashMap();
		indexedPropertyMap.put(ATTR_NAME, prepareIndexedProperty(ATTR_NAME, false));
		indexedPropertyMap.put(ATTR_CODE, prepareIndexedProperty(ATTR_CODE, false));
		indexedPropertyMap.put(ATTR_DESC, prepareIndexedProperty(ATTR_DESC, false));
		when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);

		final List<SolrSearchCondition> converted = conditionsConverter.convert(conditions, SearchQuery.Operator.AND, indexedType);

		assertThat(converted.size()).isEqualTo(2);
		final SolrSearchCondition innerConverted = extractInnerCondition(converted);
		assertThat(innerConverted).isNotNull();
		assertThat(innerConverted.isNestedCondition()).isTrue();
		assertThat(innerConverted.getNestedConditions().size()).isEqualTo(2);
		assertThat(innerConverted.getOperator()).isEqualTo(SearchQuery.Operator.OR);
		assertThat(containsConditionForName(innerConverted.getNestedConditions(), ATTR_CODE)).isTrue();
		assertThat(containsConditionForName(innerConverted.getNestedConditions(), ATTR_DESC)).isTrue();

		assertThat(containsConditionValue(innerConverted.getNestedConditions(), VAL_2)).isTrue();
		assertThat(containsConditionValue(innerConverted.getNestedConditions(), VAL_3)).isTrue();
	}

	protected SolrSearchCondition extractInnerCondition(final List<SolrSearchCondition> conditions)
	{
		final Optional<SolrSearchCondition> first = conditions.stream().filter(SolrSearchCondition::isNestedCondition).findFirst();
		return first.isPresent() ? first.get() : null;
	}

	protected boolean containsConditionForName(final List<SolrSearchCondition> conditions, final String name)
	{
		return conditions.stream().filter(c -> c.getAttributeName().equals(name)).findAny().isPresent();
	}

	protected boolean containsConditionValue(final List<SolrSearchCondition> conditions, final Object value)
	{
		return conditions.stream()
				.filter(c -> c.getConditionValues().stream().filter(cv -> cv.getValue().equals(value)).findAny().isPresent())
				.findAny().isPresent();
	}

	protected IndexedProperty prepareIndexedProperty(final String attrName, final boolean localized)
	{
		final IndexedProperty indexedProperty = mock(IndexedProperty.class);
		when(indexedProperty.getName()).thenReturn(attrName);
		when(Boolean.valueOf(indexedProperty.isLocalized())).thenReturn(Boolean.valueOf(localized));
		return indexedProperty;
	}

	protected SearchQueryCondition prepareCondition(final String attrName, final Object value,
			final ValueComparisonOperator operator)
	{
		return new SearchQueryCondition(new SearchAttributeDescriptor(attrName), value, operator);
	}
}
