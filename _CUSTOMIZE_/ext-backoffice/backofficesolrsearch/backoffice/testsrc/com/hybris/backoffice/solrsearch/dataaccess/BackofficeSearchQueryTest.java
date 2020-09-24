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
package com.hybris.backoffice.solrsearch.dataaccess;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;


public class BackofficeSearchQueryTest
{
	protected BackofficeSearchQuery query;

	@Before
	public void setUp()
	{
		query = new BackofficeSearchQuery(mock(FacetSearchConfig.class), mock(IndexedType.class));
	}

	@Test
	public void testGetFieldConditions() throws Exception
	{
		final List<SolrSearchCondition> conditions = Lists.newArrayList();
		conditions.add(prepareCondition("test1"));
		conditions.add(prepareCondition("test2"));
		conditions.add(prepareCondition("test3"));

		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareCondition("test1"))));
		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareCondition("test2"))));
		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareCondition("test3"))));

		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareNestedCondition(Lists
				.newArrayList(prepareCondition("test1"))))));
		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareNestedCondition(Lists
				.newArrayList(prepareCondition("test2"))))));
		conditions.add(prepareNestedCondition(Lists.newArrayList(prepareNestedCondition(Lists
				.newArrayList(prepareCondition("test3"))))));

		final SearchConditionData searchConditionData = new SearchConditionData();
		searchConditionData.setSearchConditionData(conditions);
		query.setSearchConditionData(searchConditionData);
		assertThat(query.getFieldConditions("test1").size()).isEqualTo(3);
		assertThat(query.getFieldConditions("test2").size()).isEqualTo(3);
		assertThat(query.getFieldConditions("test3").size()).isEqualTo(3);
	}

	protected SolrSearchCondition prepareCondition(final String filedName)
	{
		return new SolrSearchCondition(filedName, null, null, SearchQuery.Operator.OR);
	}

	protected SolrSearchCondition prepareNestedCondition(final List<SolrSearchCondition> conditions)
	{
		return new SolrSearchCondition(conditions, SearchQuery.Operator.OR);
	}
}
