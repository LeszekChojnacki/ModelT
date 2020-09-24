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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class BackofficeFacetSearchQueryFilterQueriesPopulatorTest
{

	private static final SearchQuery.Operator CONDITION_VALUE_LINKING_OPERATOR = SearchQuery.Operator.OR;
	private static final String CONDITION_ATTRIBUTE_NAME1 = "someAttribute1";
	private static final String CONDITION_ATTRIBUTE_NAME2 = "someAttribute2";
	private static final Long CONDITION_ATTRIBUTE_VALUE1 = Long.valueOf(1234L);
	private static final Long CONDITION_ATTRIBUTE_VALUE2 = Long.valueOf(5678L);
	private static final String FQ_CONDITION_GREATER = " TO *";
	private static final String FQ_CONDITION_LESS = "* TO ";
	private static final String FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX = "[";
	private static final String FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX = "]";
	private static final String FQ_CONDITION_BETWEEN_EXCLUSIVE_PREFIX = "{";
	private static final String FQ_CONDITION_BETWEEN_EXCLUSIVE_SUFFIX = "}";

	@InjectMocks
	private final BackofficeFacetSearchQueryFilterQueriesPopulator populator = new BackofficeFacetSearchQueryFilterQueriesPopulator();
	private final SearchQuery searchQuery;
	@Mock
	private FieldNameTranslator fieldNameTranslator;
	@Mock
	private FieldNamePostProcessor fieldNamePostProcessor;
	@Mock
	private Map<String, Function<Serializable, String>> conditionValueConverterMap;
	private List<String> queries;


	public BackofficeFacetSearchQueryFilterQueriesPopulatorTest()
	{
		final SolrSearchCondition solrSearchCondition1 = new SolrSearchCondition(CONDITION_ATTRIBUTE_NAME1, "long",
				CONDITION_VALUE_LINKING_OPERATOR);
		solrSearchCondition1.addConditionValue(CONDITION_ATTRIBUTE_VALUE1, ValueComparisonOperator.EQUALS);
		solrSearchCondition1.addConditionValue(CONDITION_ATTRIBUTE_VALUE2, ValueComparisonOperator.EQUALS);

		final SolrSearchCondition solrSearchCondition2 = new SolrSearchCondition(CONDITION_ATTRIBUTE_NAME2, "long",
				CONDITION_VALUE_LINKING_OPERATOR);
		solrSearchCondition2.addConditionValue(CONDITION_ATTRIBUTE_VALUE1, ValueComparisonOperator.EQUALS);
		solrSearchCondition2.addConditionValue(CONDITION_ATTRIBUTE_VALUE2, ValueComparisonOperator.EQUALS);

		final SearchConditionData searchConditionData = new SearchConditionData();
		searchConditionData.addFilterQueryCondition(solrSearchCondition1);
		searchConditionData.addFilterQueryCondition(solrSearchCondition2);

		searchQuery = new BackofficeSearchQuery(new FacetSearchConfig(), new IndexedType());
		((BackofficeSearchQuery) searchQuery).setSearchConditionData(searchConditionData);
	}

	private static String getExpectedFQValue(final String attributeName)
	{
		return attributeName.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.FQ_FIELD_VALUE_SEPARATOR)
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.FQ_VALUE_GROUP_PREFIX)
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.QUOTE).concat(CONDITION_ATTRIBUTE_VALUE1.toString())
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.QUOTE).concat(CONDITION_VALUE_LINKING_OPERATOR.getName())
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.QUOTE).concat(CONDITION_ATTRIBUTE_VALUE2.toString())
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.QUOTE)
				.concat(BackofficeFacetSearchQueryFilterQueriesPopulator.FQ_VALUE_GROUP_SUFFIX);
	}

	@Before
	public void setUp()
	{
		queries = new ArrayList<>();
		when(conditionValueConverterMap.getOrDefault(any(String.class), any()))
				.thenReturn(serializable -> Objects.toString(serializable, ""));
	}

	@Test
	public void shouldAddRawQueries()
	{
		when(fieldNameTranslator.translate(searchQuery, CONDITION_ATTRIBUTE_NAME1, FieldNameProvider.FieldType.INDEX))
				.thenReturn(CONDITION_ATTRIBUTE_NAME1);
		when(fieldNamePostProcessor.process(searchQuery, null, CONDITION_ATTRIBUTE_NAME1)).thenReturn(CONDITION_ATTRIBUTE_NAME1);

		when(fieldNameTranslator.translate(searchQuery, CONDITION_ATTRIBUTE_NAME2, FieldNameProvider.FieldType.INDEX))
				.thenReturn(CONDITION_ATTRIBUTE_NAME2);
		when(fieldNamePostProcessor.process(searchQuery, null, CONDITION_ATTRIBUTE_NAME2)).thenReturn(CONDITION_ATTRIBUTE_NAME2);

		populator.addRawQueries(searchQuery, queries);

		assertThat(queries.size()).isEqualTo(2);
		assertThat(getExpectedFQValue(CONDITION_ATTRIBUTE_NAME1)).isEqualTo(queries.get(0));
		assertThat(getExpectedFQValue(CONDITION_ATTRIBUTE_NAME2)).isEqualTo(queries.get(1));
	}

	@Test
	public void shouldHandleLessOperator()
	{
		//given
		final SolrSearchCondition.ConditionValue conditionValue = mock(SolrSearchCondition.ConditionValue.class);
		when(conditionValue.getValue()).thenReturn(CONDITION_ATTRIBUTE_VALUE1);
		when(conditionValue.getComparisonOperator()).thenReturn(ValueComparisonOperator.LESS);


		//when
		final String convertedValue = populator.convertConditionValueToString(conditionValue);

		//then
		assertThat(convertedValue).isEqualTo(FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + FQ_CONDITION_LESS + CONDITION_ATTRIBUTE_VALUE1
				+ FQ_CONDITION_BETWEEN_EXCLUSIVE_SUFFIX);
	}

	@Test
	public void shouldHandleGreaterOperator()
	{
		//given
		final SolrSearchCondition.ConditionValue conditionValue = mock(SolrSearchCondition.ConditionValue.class);
		when(conditionValue.getValue()).thenReturn(CONDITION_ATTRIBUTE_VALUE1);
		when(conditionValue.getComparisonOperator()).thenReturn(ValueComparisonOperator.GREATER);


		//when
		final String convertedValue = populator.convertConditionValueToString(conditionValue);

		//then
		assertThat(convertedValue).isEqualTo(FQ_CONDITION_BETWEEN_EXCLUSIVE_PREFIX + CONDITION_ATTRIBUTE_VALUE1 + FQ_CONDITION_GREATER
				+ FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX);
	}

	@Test
	public void shouldHandleLessOrEqualOperator()
	{
		//given
		final SolrSearchCondition.ConditionValue conditionValue = mock(SolrSearchCondition.ConditionValue.class);
		when(conditionValue.getValue()).thenReturn(CONDITION_ATTRIBUTE_VALUE1);
		when(conditionValue.getComparisonOperator()).thenReturn(ValueComparisonOperator.LESS_OR_EQUAL);


		//when
		final String convertedValue = populator.convertConditionValueToString(conditionValue);

		//then
		assertThat(convertedValue).isEqualTo(FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + FQ_CONDITION_LESS + CONDITION_ATTRIBUTE_VALUE1
				+ FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX);
	}

	@Test
	public void shouldHandleGreaterOrEqualOperator()
	{
		//given
		final SolrSearchCondition.ConditionValue conditionValue = mock(SolrSearchCondition.ConditionValue.class);
		when(conditionValue.getValue()).thenReturn(CONDITION_ATTRIBUTE_VALUE1);
		when(conditionValue.getComparisonOperator()).thenReturn(ValueComparisonOperator.GREATER_OR_EQUAL);


		//when
		final String convertedValue = populator.convertConditionValueToString(conditionValue);

		//then
		assertThat(convertedValue).isEqualTo(FQ_CONDITION_BETWEEN_INCLUSIVE_PREFIX + CONDITION_ATTRIBUTE_VALUE1
				+ FQ_CONDITION_GREATER + FQ_CONDITION_BETWEEN_INCLUSIVE_SUFFIX);
	}


}
