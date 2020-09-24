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
package de.hybris.platform.solrfacetsearch.reporting.data;

import java.util.Date;


/**
 * Aggregated statistic, contains information about number of keyword queries and average number of results
 */
public class AggregatedSearchQueryInfo
{
	private final String indexName;
	private final String query;
	private final String language;
	private long count;
	private double averageNumberOfResults;
	private final Date date;

	public AggregatedSearchQueryInfo(final String indexName, final String query, final String language,
			final long numberOfResults, final Date date)
	{
		this.indexName = indexName;
		this.query = query;
		this.language = language;
		this.date = date;
		this.count = 1L;
		this.averageNumberOfResults = numberOfResults;
	}

	public void addNumberOfResults(final long numberOfResults)
	{
		averageNumberOfResults = ((averageNumberOfResults * count) + numberOfResults) / (count + 1);
		count++;
	}

	public String getIndexName()
	{
		return indexName;
	}

	public String getQuery()
	{
		return query;
	}

	public String getLanguage()
	{
		return language;
	}

	public long getCount()
	{
		return count;
	}

	public double getAverageNumberOfResults()
	{
		return averageNumberOfResults;
	}

	public Date getDate()
	{
		return date;
	}

}
