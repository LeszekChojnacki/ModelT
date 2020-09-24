/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved
 */
package com.hybris.backoffice.solrsearch.decorators.impl;

import static org.assertj.core.api.Assertions.assertThat;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


@UnitTest
public class DefaultContainsConditionDecoratorTest
{

	private static final String CONTAINS_ATTR_NAME = "attrCont";
	private static final String EQUALS_ATTR_NAME = "attrEq";
	private static final String CONTAINS_VALUE = "cont";
	private static final String EQUALS_VALUE = "eq";

	private DefaultContainsConditionDecorator decorator;


	@Before
	public void setUp()
	{
		decorator = new DefaultContainsConditionDecorator();
	}

	@Test
	public void shouldDecorateContainsCondition()
	{
		// given
		final SearchConditionData conditionData = prepareSearchConditionDataWithQueryCondition();

		// when
		decorator.decorate(conditionData, null, null);
		final Map<String, SolrSearchCondition> decoratedConditionsByAttributeNames = conditionData.getQueryConditions().stream()
				.collect(Collectors.toMap(SolrSearchCondition::getAttributeName, Function.identity()));

		// then
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues()).hasSize(2);
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues())
				.allMatch(conditionValue -> CONTAINS_VALUE.equals(conditionValue.getValue()));
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues().stream()
				.map(SolrSearchCondition.ConditionValue::getComparisonOperator).collect(Collectors.toList()))
						.containsExactlyInAnyOrder(ValueComparisonOperator.EQUALS, ValueComparisonOperator.CONTAINS);
	}

	@Test
	public void shouldDecorateContainsFQCondition()
	{
		// given
		final SearchConditionData conditionData = prepareSearchConditionDataWithFilterQueryCondition();

		// when
		decorator.decorate(conditionData, null, null);
		final Map<String, SolrSearchCondition> decoratedConditionsByAttributeNames = conditionData.getFilterQueryConditions()
				.stream().collect(Collectors.toMap(SolrSearchCondition::getAttributeName, Function.identity()));

		// then
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues()).hasSize(2);
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues())
				.allMatch(conditionValue -> CONTAINS_VALUE.equals(conditionValue.getValue()));
		assertThat(decoratedConditionsByAttributeNames.get(CONTAINS_ATTR_NAME).getConditionValues().stream()
				.map(SolrSearchCondition.ConditionValue::getComparisonOperator).collect(Collectors.toList()))
						.containsExactlyInAnyOrder(ValueComparisonOperator.EQUALS, ValueComparisonOperator.CONTAINS);
	}

	@Test
	public void shouldNotDecorateEqualsCondition()
	{
		// given
		final SearchConditionData conditionData = prepareSearchConditionDataWithQueryCondition();

		// when
		decorator.decorate(conditionData, null, null);
		final Map<String, SolrSearchCondition> decoratedConditionsByAttributeNames = conditionData.getQueryConditions().stream()
				.collect(Collectors.toMap(SolrSearchCondition::getAttributeName, Function.identity()));

		// then
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues()).hasSize(1);
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues().get(0).getComparisonOperator())
				.isEqualTo(ValueComparisonOperator.EQUALS);
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues().get(0).getValue())
				.isEqualTo(EQUALS_VALUE);
	}

	@Test
	public void shouldNotDecorateEqualsFQCondition()
	{
		// given
		final SearchConditionData conditionData = prepareSearchConditionDataWithFilterQueryCondition();

		// when
		decorator.decorate(conditionData, null, null);
		final Map<String, SolrSearchCondition> decoratedConditionsByAttributeNames = conditionData.getFilterQueryConditions()
				.stream().collect(Collectors.toMap(SolrSearchCondition::getAttributeName, Function.identity()));

		// then
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues()).hasSize(1);
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues().get(0).getComparisonOperator())
				.isEqualTo(ValueComparisonOperator.EQUALS);
		assertThat(decoratedConditionsByAttributeNames.get(EQUALS_ATTR_NAME).getConditionValues().get(0).getValue())
				.isEqualTo(EQUALS_VALUE);
	}

	private SolrSearchCondition createContainsCondition(final String attributeName, final String value)
	{
		final SolrSearchCondition result = new SolrSearchCondition(attributeName, "type", SearchQuery.Operator.AND);
		result.addConditionValue(value, ValueComparisonOperator.CONTAINS);
		return result;
	}

	private SolrSearchCondition createEqualsCondition(final String attributeName, final String value)
	{
		final SolrSearchCondition result = new SolrSearchCondition(attributeName, "type", SearchQuery.Operator.AND);
		result.addConditionValue(value, ValueComparisonOperator.EQUALS);
		return result;
	}

	private SearchConditionData prepareSearchConditionDataWithQueryCondition()
	{
		final SearchConditionData conditionData = new SearchConditionData();
		conditionData.addQueryCondition(createContainsCondition(CONTAINS_ATTR_NAME, CONTAINS_VALUE));
		conditionData.addQueryCondition(createEqualsCondition(EQUALS_ATTR_NAME, EQUALS_VALUE));
		return conditionData;
	}

	private SearchConditionData prepareSearchConditionDataWithFilterQueryCondition()
	{
		final SearchConditionData conditionData = new SearchConditionData();
		conditionData.addFilterQueryCondition(createContainsCondition(CONTAINS_ATTR_NAME, CONTAINS_VALUE));
		conditionData.addFilterQueryCondition(createEqualsCondition(EQUALS_ATTR_NAME, EQUALS_VALUE));
		return conditionData;
	}

}
