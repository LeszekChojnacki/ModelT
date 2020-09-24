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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Holds list of {@link SolrSearchCondition} separated into query conditions and filter query conditions.
 */
public class SearchConditionData implements Serializable
{
	private final List<SolrSearchCondition> queryConditions = new ArrayList<>();
	private final List<SolrSearchCondition> filterQueryConditions = new ArrayList<>();

	public List<SolrSearchCondition> getQueryConditions()
	{
		return queryConditions;
	}

	public void setSearchConditionData(final List<SolrSearchCondition> queryConditions)
	{
		this.queryConditions.clear();
		this.queryConditions.addAll(queryConditions);
	}

	public void addQueryCondition(final SolrSearchCondition condition)
	{
		queryConditions.add(condition);
	}

	public List<SolrSearchCondition> getFilterQueryConditions()
	{
		return filterQueryConditions;
	}

	public void setFilterQueryConditions(final List<SolrSearchCondition> filterQueryConditions)
	{
		this.filterQueryConditions.clear();
		this.filterQueryConditions.addAll(filterQueryConditions);
	}

	public void addFilterQueryCondition(final SolrSearchCondition condition)
	{
		filterQueryConditions.add(condition);
	}

}
