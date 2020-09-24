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
package com.hybris.backoffice.solrsearch.utils;

import de.hybris.platform.solrfacetsearch.config.SolrServerMode;

import java.util.Locale;

import com.hybris.backoffice.ApplicationUtils;


public final class SolrPlatformUtils
{
	public static final String SOLR_EXPORTER_BEAN_NAME_PREFIX = "solr.exporter.";

	private SolrPlatformUtils()
	{
	}

	/**
	 * Checks whether platform is ready for business process engine events
	 *
	 * @return true if events can be processed
	 * @deprecated since 1808, use {@link ApplicationUtils#isPlatformReady()}
	 */
	@Deprecated
	public static boolean isPlatformReady()
	{
		return ApplicationUtils.isPlatformReady();
	}

	public static String createSolrExporterBeanName(final SolrServerMode serverMode)
	{
		return SOLR_EXPORTER_BEAN_NAME_PREFIX + serverMode.toString().toLowerCase(Locale.getDefault());
	}
}
