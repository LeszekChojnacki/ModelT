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
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategyFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DefaultIndexerBatchStrategyFactory implements IndexerBatchStrategyFactory, ApplicationContextAware
{
	private String indexerBatchStrategyBeanId;

	private ApplicationContext applicationContext;

	@Override
	public IndexerBatchStrategy createIndexerBatchStrategy(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		try
		{
			return applicationContext.getBean(indexerBatchStrategyBeanId, IndexerBatchStrategy.class);
		}
		catch (final BeansException e)
		{
			throw new IndexerException("Cannot create indexer batch strategy [" + indexerBatchStrategyBeanId + "]", e);
		}
	}

	public String getIndexerBatchStrategyBeanId()
	{
		return indexerBatchStrategyBeanId;
	}

	@Required
	public void setIndexerBatchStrategyBeanId(final String indexerBatchStrategyBeanId)
	{
		this.indexerBatchStrategyBeanId = indexerBatchStrategyBeanId;
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
}
