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
package de.hybris.platform.solrfacetsearch.reporting.processors;

import de.hybris.platform.solrfacetsearch.reporting.SolrReportingService;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.SolrResultPostProcessor;


/**
 * Query post processor - stores statistics for SOLR query
 */
public class SaveResultDataPostProcessor implements SolrResultPostProcessor
{
	private SolrReportingService solrReportingService;
	private boolean enableCollectingStatistics;

	@Override
	public SearchResult process(final SearchResult solrSearchResult)
	{
		if (enableCollectingStatistics)
		{
			final SearchQueryInfo result = solrSearchResult.getQueryInfo();
			solrReportingService.saveQueryResult(result);
		}
		return solrSearchResult;
	}

	public void setSolrReportingService(final SolrReportingService solrReportingService)
	{
		this.solrReportingService = solrReportingService;
	}

	public void setEnableCollectingStatistics(final boolean enableCollectingStatistics)
	{
		this.enableCollectingStatistics = enableCollectingStatistics;
	}
}
