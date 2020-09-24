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
package de.hybris.platform.solrfacetsearch.indexer.strategies.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategyFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link IndexerStrategyFactory}.
 */
public class DefaultIndexerStrategyFactory implements IndexerStrategyFactory, ApplicationContextAware
{
	private String indexerStrategyBeanId;
	private String distributedIndexerStrategyBeanId;

	private ApplicationContext applicationContext;

	public String getIndexerStrategyBeanId()
	{
		return indexerStrategyBeanId;
	}

	@Required
	public void setIndexerStrategyBeanId(final String indexerStrategyBeanId)
	{
		this.indexerStrategyBeanId = indexerStrategyBeanId;
	}

	public String getDistributedIndexerStrategyBeanId()
	{
		return distributedIndexerStrategyBeanId;
	}

	@Required
	public void setDistributedIndexerStrategyBeanId(final String distributedIndexerStrategyBeanId)
	{
		this.distributedIndexerStrategyBeanId = distributedIndexerStrategyBeanId;
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

	@Override
	public IndexerStrategy createIndexerStrategy(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		try
		{
			if (facetSearchConfig.getIndexConfig().isDistributedIndexing())
			{
				return applicationContext.getBean(distributedIndexerStrategyBeanId, IndexerStrategy.class);
			}
			else
			{
				return applicationContext.getBean(indexerStrategyBeanId, IndexerStrategy.class);
			}
		}
		catch (final BeansException e)
		{
			throw new IndexerException("Cannot create indexer strategy [" + indexerStrategyBeanId + "]", e);
		}
	}
}
