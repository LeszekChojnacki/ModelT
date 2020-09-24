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
package com.hybris.backoffice.solrsearch.selenium.remote;

import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


public class TestBackofficeSolrTestingService implements BackofficeSolrTestingService
{

	private static final Logger LOG = LogManager.getLogger(TestBackofficeSolrTestingService.class);

	protected BackofficeFacetSearchConfigService facetSearchConfigService;
	protected IndexerService indexerService;

	@Override
	public void reindexSolr()
	{
		getFacetSearchConfigService().getAllMappedFacetSearchConfigs().forEach(config -> {
			try
			{
				getIndexerService().performFullIndex(config);
			}
			catch (final IndexerException ex)
			{
				LOG.error(ex.getLocalizedMessage(), ex);
			}
		});
	}

	public IndexerService getIndexerService()
	{
		return indexerService;
	}

	@Required
	public void setIndexerService(final IndexerService indexerService)
	{
		this.indexerService = indexerService;
	}

	public BackofficeFacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final BackofficeFacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}
}
