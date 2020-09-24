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

import de.hybris.platform.solrfacetsearch.enums.SolrPropertiesTypes;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class DefaultSearchConditionDataConverterTest
{
	public static final String STRING_CONDITION_TYPE = "string";
	private static final String ATTR_CODE = "code";
	private static final String ATTR_DESC = "desc";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_USER_QUERY = "noFQUserQuery";
	private static final String ATTR_CATALOG_VERSION = "catalogVersion";
	@InjectMocks
	private DefaultSearchConditionDataConverter conditionsConverter;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		conditionsConverter.setFqApplicableOperators(Sets.newHashSet(ValueComparisonOperator.EQUALS));
		conditionsConverter.setFqApplicablePropertiesTypes(Sets.newHashSet(SolrPropertiesTypes.LONG, SolrPropertiesTypes.STRING));
	}

	@Test
	public void testConvertConditions()
	{
		final List<SolrSearchCondition> conditions = Lists.newArrayList();
		SolrSearchCondition condition = new SolrSearchCondition(ATTR_CATALOG_VERSION, STRING_CONDITION_TYPE,
				SearchQuery.Operator.AND);
		condition.addConditionValue("fq", ValueComparisonOperator.EQUALS);
		conditions.add(condition);

		condition = new SolrSearchCondition(ATTR_CODE, STRING_CONDITION_TYPE, SearchQuery.Operator.AND);
		condition.addConditionValue("noFQWrongOperator", ValueComparisonOperator.CONTAINS);
		conditions.add(condition);

		condition = new SolrSearchCondition(ATTR_DESC, "text", SearchQuery.Operator.AND);
		condition.addConditionValue("noFQNotString", ValueComparisonOperator.AND);
		conditions.add(condition);

		final SolrSearchCondition nestedFromSearch = new SolrSearchCondition(conditions, SearchQuery.Operator.AND);
		final SolrSearchCondition simpleTextQuery = new SolrSearchCondition(ATTR_USER_QUERY, "text", SearchQuery.Operator.AND);
		final SolrSearchCondition nestedSimpleWitFromSearch = new SolrSearchCondition(
				Lists.newArrayList(nestedFromSearch, simpleTextQuery), SearchQuery.Operator.AND);

		final SolrSearchCondition type = new SolrSearchCondition(ATTR_TYPE, STRING_CONDITION_TYPE, SearchQuery.Operator.AND);
		type.addConditionValue("product", ValueComparisonOperator.EQUALS);

		final SolrSearchCondition topWithType = new SolrSearchCondition(Lists.newArrayList(nestedSimpleWitFromSearch, type),
				SearchQuery.Operator.AND);

		final SearchConditionData searchConditionData = conditionsConverter.convertConditions(Lists.newArrayList(topWithType),
				SearchQuery.Operator.AND);

		assertThat(searchConditionData.getFilterQueryConditions()).hasSize(2);
		assertThat(hasConditionForAttribute(searchConditionData.getFilterQueryConditions(), ATTR_TYPE)).isTrue();
		assertThat(hasConditionForAttribute(searchConditionData.getFilterQueryConditions(), ATTR_CATALOG_VERSION)).isTrue();

		assertThat(searchConditionData.getQueryConditions()).hasSize(3);
		assertThat(hasConditionForAttribute(searchConditionData.getQueryConditions(), ATTR_CODE)).isTrue();
		assertThat(hasConditionForAttribute(searchConditionData.getQueryConditions(), ATTR_DESC)).isTrue();
		assertThat(hasConditionForAttribute(searchConditionData.getQueryConditions(), ATTR_USER_QUERY)).isTrue();
	}

	@Test
	public void testFlattenedConditions()
	{
		final SolrSearchCondition A = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE,
				SearchQuery.Operator.AND);
		final SolrSearchCondition B = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE,
				SearchQuery.Operator.AND);
		final SolrSearchCondition C = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE, SearchQuery.Operator.OR);
		final SolrSearchCondition D = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE, SearchQuery.Operator.OR);
		final SolrSearchCondition E = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE,
				SearchQuery.Operator.AND);
		final SolrSearchCondition F = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE,
				SearchQuery.Operator.AND);
		final SolrSearchCondition G = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE, SearchQuery.Operator.OR);
		final SolrSearchCondition H = new SolrSearchCondition(generateRandomName(), STRING_CONDITION_TYPE, SearchQuery.Operator.OR);

		final SolrSearchCondition nestedBCD = new SolrSearchCondition(Lists.newArrayList(B, C, D), SearchQuery.Operator.AND);
		final SolrSearchCondition nestedEF = new SolrSearchCondition(Lists.newArrayList(E, F), SearchQuery.Operator.OR);
		final SolrSearchCondition nestedGH = new SolrSearchCondition(Lists.newArrayList(G, H), SearchQuery.Operator.AND);
		final SolrSearchCondition combinedNested = new SolrSearchCondition(Lists.newArrayList(nestedEF, nestedGH),
				SearchQuery.Operator.AND);

		final SolrSearchCondition query = new SolrSearchCondition(Lists.newArrayList(A, nestedBCD, combinedNested),
				SearchQuery.Operator.AND);

		final List<SolrSearchCondition> flattened = conditionsConverter.flattenConditions(Lists.newArrayList(query));
		assertThat(flattened).hasSize(8);
	}

	protected String generateRandomName()
	{
		return Long.toString(System.nanoTime(), 24);
	}

	protected boolean hasConditionForAttribute(final List<SolrSearchCondition> conditions, final String attributeName)
	{
		return conditions.stream().filter(c -> c.getAttributeName().equals(attributeName)).findAny().isPresent();
	}
}
