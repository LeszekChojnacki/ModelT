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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;


public class BackofficeSearchQuery extends SearchQuery
{

	private SearchConditionData searchConditionData;


	public BackofficeSearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		super(facetSearchConfig, indexedType);
	}

	public void setSearchConditionData(final SearchConditionData searchConditionData)
	{
		this.searchConditionData = searchConditionData;
	}

	@Override
	public List<QueryField> getQueries()
	{
		final List<SolrSearchCondition> searchConditions = searchConditionData.getQueryConditions();
		final List<SolrSearchCondition> flatConditions = getFlatQueryConditions(searchConditions);
		final List<QueryField> fieldQueries = new ArrayList<>();
		flatConditions.forEach(condition -> condition.getConditionValues().forEach(conditionValue -> {
			final BackofficeQueryField queryField = new BackofficeQueryField(condition.getAttributeName(), condition.getOperator(),
					QueryOperator.CONTAINS, condition.getLanguage(), conditionValue.getValue().toString());
			fieldQueries.add(queryField);
		}));

		return fieldQueries;
	}

	protected List<SolrSearchCondition> getFlatQueryConditions(final List<SolrSearchCondition> searchConditions)
	{
		final List<SolrSearchCondition> flatConditions = new ArrayList<>();
		searchConditions.forEach(condition -> {
			if (condition.isNestedCondition())
			{
				flatConditions.addAll(getFlatQueryConditions(condition.getNestedConditions()));
			}
			else
			{
				flatConditions.add(condition);
			}
		});
		return flatConditions;
	}

	public List<SolrSearchCondition> getFilterQueryConditions()
	{
		return searchConditionData.getFilterQueryConditions();
	}

	public List<SolrSearchCondition> getFieldConditions(final String field)
	{
		final List<SolrSearchCondition> fieldConditions = Lists.newArrayList();
		populateFieldConditions(field, searchConditionData.getQueryConditions(), fieldConditions);
		return fieldConditions;
	}

	private void populateFieldConditions(final String field, final List<SolrSearchCondition> source,
			final List<SolrSearchCondition> target)
	{
		if (CollectionUtils.isNotEmpty(source))
		{
			source.forEach(condition -> {
				if (condition.isNestedCondition())
				{
					populateFieldConditions(field, condition.getNestedConditions(), target);
				}
				else if (StringUtils.equalsIgnoreCase(condition.getAttributeName(), field))
				{
					target.add(condition);
				}
			});
		}
	}

}
