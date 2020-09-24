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

import de.hybris.platform.solrfacetsearch.reporting.SolrReportingService;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.fest.util.Arrays;
import org.springframework.beans.factory.annotation.Required;


/**
 * Uses Log4j log files to store information about single query to SOLR
 */
public class Log4jSolrReportingService implements SolrReportingService
{
	private static final Logger LOG = Logger.getLogger("solrStatisticLogger");
	private SimpleDateFormat formatter;

	@Override
	public void saveQueryResult(final SearchQueryInfo reportingQueryResult)
	{
		LOG.info(StringUtils.join(getArray(reportingQueryResult), '|'));
	}

	protected Object[] getArray(final SearchQueryInfo reportingQueryResult)
	{
		return Arrays.array(formatter.format(reportingQueryResult.date), reportingQueryResult.query,
				reportingQueryResult.indexConfiguration, reportingQueryResult.language, Long.valueOf(reportingQueryResult.count));
	}

	@Required
	public void setFormatter(final SimpleDateFormat formatter)
	{
		this.formatter = formatter;
	}


}
