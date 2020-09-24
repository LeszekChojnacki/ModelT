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
 * Stores single query statistic information
 */
public class SearchQueryInfo
{
	public final String query;
	public final long count;
	public final String indexConfiguration;
	public final String language;
	public final Date date;

	public SearchQueryInfo(final String query, final long count, final String indexConfiguration, final String language,
			final Date date)
	{
		super();
		this.query = query;
		this.count = count;
		this.indexConfiguration = indexConfiguration;
		this.language = language;
		this.date = date;
	}

}
