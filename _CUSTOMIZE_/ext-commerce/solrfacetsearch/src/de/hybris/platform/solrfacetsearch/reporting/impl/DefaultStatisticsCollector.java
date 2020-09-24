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
package de.hybris.platform.solrfacetsearch.reporting.impl;

import de.hybris.platform.solrfacetsearch.reporting.StatisticsCollector;
import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;


/**
 * Default query statistics collector.
 *
 * Maps formatted date, index name, language and query to query statistics
 *
 */
public class DefaultStatisticsCollector implements StatisticsCollector
{
	private final Map<String, Map<String, Map<String, Map<String, AggregatedSearchQueryInfo>>>> dateIndexMap = Maps.newHashMap();
	private SimpleDateFormat formatter;

	@Override
	public void addStatistic(final SearchQueryInfo searchResult)
	{
		final String formattedDate = formatter.format(searchResult.date);
		if (dateIndexMap.containsKey(formattedDate))
		{
			updateDateIndexEntry(searchResult, formattedDate);
		}
		else
		{
			createDateIndexEntry(searchResult, formattedDate);
		}
	}

	protected void updateDateIndexEntry(final SearchQueryInfo searchResult, final String formattedDate)
	{
		final Map<String, Map<String, Map<String, AggregatedSearchQueryInfo>>> indexLanguageMap = dateIndexMap.get(formattedDate);
		if (indexLanguageMap.containsKey(searchResult.indexConfiguration))
		{
			final Map<String, Map<String, AggregatedSearchQueryInfo>> languageQueryMap = indexLanguageMap
					.get(searchResult.indexConfiguration);
			if (languageQueryMap.containsKey(searchResult.language))
			{
				final Map<String, AggregatedSearchQueryInfo> queryReportingMap = languageQueryMap.get(searchResult.language);

				if (queryReportingMap.containsKey(searchResult.query))
				{
					queryReportingMap.get(searchResult.query).addNumberOfResults(searchResult.count);
				}
				else
				{
					queryReportingMap.put(searchResult.query, new AggregatedSearchQueryInfo(searchResult.indexConfiguration,
							searchResult.query, searchResult.language, searchResult.count, searchResult.date));
				}
			}
			else
			{
				languageQueryMap.put(searchResult.language, Maps.<String, AggregatedSearchQueryInfo> newHashMap());
				addStatistic(searchResult);
			}
		}
		else
		{
			indexLanguageMap.put(searchResult.indexConfiguration,
					Maps.<String, Map<String, AggregatedSearchQueryInfo>> newHashMap());
			addStatistic(searchResult);
		}
	}

	protected void createDateIndexEntry(final SearchQueryInfo searchResult, final String formattedDate)
	{
		dateIndexMap.put(formattedDate, Maps.<String, Map<String, Map<String, AggregatedSearchQueryInfo>>> newHashMap());
		addStatistic(searchResult);
	}

	@Override
	public List<AggregatedSearchQueryInfo> getAggregatedStatistics()
	{
		final List<AggregatedSearchQueryInfo> results = new ArrayList<AggregatedSearchQueryInfo>();
		for (final Map<String, Map<String, Map<String, AggregatedSearchQueryInfo>>> m : dateIndexMap.values())
		{
			for (final Map<String, Map<String, AggregatedSearchQueryInfo>> m2 : m.values())
			{
				for (final Map<String, AggregatedSearchQueryInfo> m3 : m2.values())
				{
					results.addAll(m3.values());
				}
			}
		}

		return results;
	}

	@Override
	public void clear()
	{
		dateIndexMap.clear();
	}

	public void setFormatter(final SimpleDateFormat formatter)
	{
		this.formatter = formatter;
	}


}
