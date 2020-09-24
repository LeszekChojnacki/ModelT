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
package com.hybris.backoffice.solrsearch.indexer.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategyFactory;
import de.hybris.platform.solrfacetsearch.indexer.strategies.impl.DefaultIndexerStrategyFactory;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


/**
 * Backoffice implementation of {@link IndexerStrategyFactory}
 */
public class BackofficeIndexerStrategyFactory implements IndexerStrategyFactory, ApplicationContextAware
{

	private DefaultIndexerStrategyFactory defaultIndexerStrategyFactory;
	private ApplicationContext applicationContext;
	private String indexerStrategyBeanName;
	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;


	public DefaultIndexerStrategyFactory getDefaultIndexerStrategyFactory()
	{
		return defaultIndexerStrategyFactory;
	}

	@Required
	public void setDefaultIndexerStrategyFactory(final DefaultIndexerStrategyFactory defaultIndexerStrategyFactory)
	{
		this.defaultIndexerStrategyFactory = defaultIndexerStrategyFactory;
	}

	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public String getIndexerStrategyBeanName()
	{
		return indexerStrategyBeanName;
	}

	@Required
	public void setIndexerStrategyBeanName(final String indexerStrategyBeanName)
	{
		this.indexerStrategyBeanName = indexerStrategyBeanName;
	}

	public BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	/**
	 * Check whether custom {@link IndexerStrategy} should be used (specified as name {@link #indexerStrategyBeanName})
	 * by checking if BackofficeSolrSearch is configured for given name.<br>
	 * If it is not, {@link DefaultIndexerStrategyFactory} is used for {@link IndexerStrategy} creation.
	 */
	@Override
	public IndexerStrategy createIndexerStrategy(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		if (backofficeFacetSearchConfigService.isBackofficeSolrSearchConfiguredForName(facetSearchConfig.getName()))
		{
			return applicationContext.getBean(getIndexerStrategyBeanName(), IndexerStrategy.class);
		}
		return defaultIndexerStrategyFactory.createIndexerStrategy(facetSearchConfig);
	}

}
