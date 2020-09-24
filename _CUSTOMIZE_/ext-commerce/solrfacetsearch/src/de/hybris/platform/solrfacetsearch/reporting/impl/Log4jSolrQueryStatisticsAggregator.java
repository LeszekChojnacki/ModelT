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

import de.hybris.platform.solrfacetsearch.reporting.ReportingRuntimeException;
import de.hybris.platform.solrfacetsearch.reporting.SolrQueryStatisticsAggregator;
import de.hybris.platform.solrfacetsearch.reporting.StatisticsCollector;
import de.hybris.platform.solrfacetsearch.reporting.data.AggregatedSearchQueryInfo;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;


/**
 * Uses Log4j log files to aggregate data.
 *
 * Reads files from folder (property 'statisticFilesFolder'), processes it, deletes files and collects statistics using
 * collector.
 */
public class Log4jSolrQueryStatisticsAggregator implements SolrQueryStatisticsAggregator
{
	private static final Logger LOG = Logger.getLogger(Log4jSolrQueryStatisticsAggregator.class);

	private String statisticFilesFolder;
	private String filePrefix;
	private SimpleDateFormat formatter;
	private StatisticsCollector statisticsCollector;

	@Override
	public List<AggregatedSearchQueryInfo> aggregate()
	{
		final File[] files = getLogFilesToProcess();

		for (final File file : files)
		{
			try
			{
				final List<SearchQueryInfo> r = processFile(file);
				if (!file.delete())
				{
					LOG.warn("Could not delete " + file);
				}

				for (final SearchQueryInfo res : r)
				{
					statisticsCollector.addStatistic(res);
				}
			}
			catch (final IOException e)
			{
				throw new ReportingRuntimeException(e);
			}
		}

		final List<AggregatedSearchQueryInfo> aggregatedStatistics = statisticsCollector.getAggregatedStatistics();
		statisticsCollector.clear();
		return aggregatedStatistics;
	}

	protected File[] getLogFilesToProcess()
	{
		final File[] files = new File(statisticFilesFolder).listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				return name.startsWith(filePrefix) && !name.equals(filePrefix);
			}
		});
		return files != null ? files : new File[] {};
	}

	protected List<SearchQueryInfo> processFile(final File file) throws IOException
	{
		final List<SearchQueryInfo> r = Files.readLines(file, Charset.defaultCharset(), new LogEntryProcessor());

		return r;
	}

	public void setStatisticFilesFolder(final String statisticFilesFolder)
	{
		this.statisticFilesFolder = statisticFilesFolder;
	}

	public void setFilePrefix(final String filePrefix)
	{
		this.filePrefix = filePrefix;
	}

	public void setFormatter(final SimpleDateFormat formatter)
	{
		this.formatter = formatter;
	}

	public void setStatisticsCollector(final StatisticsCollector statisticsCollector)
	{
		this.statisticsCollector = statisticsCollector;
	}

	protected class LogEntryProcessor implements LineProcessor<List<SearchQueryInfo>>
	{
		List<SearchQueryInfo> res = new ArrayList<SearchQueryInfo>();

		@Override
		public List<SearchQueryInfo> getResult()
		{
			return res;
		}

		@Override
		public boolean processLine(final String line) throws IOException
		{
			try
			{
				final String[] parts = line.split("\\|");
				res.add(new SearchQueryInfo(parts[1], Long.parseLong(parts[4]), parts[2], parts[3], formatter.parse(parts[0])));
			}
			catch (final NumberFormatException | ParseException e)
			{
				throw new IOException(e);
			}
			return true;
		}
	}
}
