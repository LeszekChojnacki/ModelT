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
package de.hybris.platform.solrfacetsearch.solr.impl;

import de.hybris.platform.solrfacetsearch.config.SolrConfig;

import java.io.Closeable;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Container for Solr clients.
 */
public class SolrClientsWrapper implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(SolrClientsWrapper.class);

	private final String configName;
	private final String configVersion;
	private CachedSolrClient searchClient;
	private CachedSolrClient indexClient;

	/**
	 * Default constructor.
	 *
	 * @param solrConfig
	 *           - the Solr configuration
	 */
	public SolrClientsWrapper(final SolrConfig solrConfig)
	{
		this.configName = solrConfig.getName();
		this.configVersion = solrConfig.getVersion();
	}

	public String getConfigName()
	{
		return configName;
	}

	public String getConfigVersion()
	{
		return configVersion;
	}

	public CachedSolrClient getSearchClient()
	{
		return searchClient;
	}

	public void setSearchClient(final CachedSolrClient searchClient)
	{
		this.searchClient = searchClient;
	}

	public CachedSolrClient getIndexClient()
	{
		return indexClient;
	}

	public void setIndexClient(final CachedSolrClient indexClient)
	{
		this.indexClient = indexClient;
	}

	/**
	 * Closes all {@link SolrClient} instances that it has reference to.
	 */
	public void close()
	{
		LOG.info("Closing Solr clients [config={}]", getConfigName());

		IOUtils.closeQuietly(searchClient);
		IOUtils.closeQuietly(indexClient);
	}
}
