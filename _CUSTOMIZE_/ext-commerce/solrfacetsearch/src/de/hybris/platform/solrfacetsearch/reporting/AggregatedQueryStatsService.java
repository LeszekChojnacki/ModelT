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

import java.util.List;


/**
 * Service to manipulate aggregated query statistics
 */
public interface AggregatedQueryStatsService
{
	/**
	 * Saves aggregated query statistics
	 *
	 * @param aggregatedStatistics List of {@link AggregatedSearchQueryInfo}
	 */
	void save(final List<AggregatedSearchQueryInfo> aggregatedStatistics);
}
