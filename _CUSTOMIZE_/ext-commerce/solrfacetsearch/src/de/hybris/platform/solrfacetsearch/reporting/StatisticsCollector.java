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
package de.hybris.platform.solrfacetsearch.reporting;

import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;

import java.util.List;


/**
 * Collects statistics for Solr queries
 */
public interface StatisticsCollector
{
	/**
	 * Adds single statistic
	 */
	void addStatistic(final SearchQueryInfo searchResult);

	/**
	 * Returns all aggregated statistics for all single queries that had been added
	 */
	List<AggregatedSearchQueryInfo> getAggregatedStatistics();

	/**
	 * Clears all statistics
	 */
	void clear();
}
