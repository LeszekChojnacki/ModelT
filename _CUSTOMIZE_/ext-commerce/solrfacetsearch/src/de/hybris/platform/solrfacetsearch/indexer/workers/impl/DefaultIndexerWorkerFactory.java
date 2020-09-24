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
package de.hybris.platform.solrfacetsearch.indexer.workers.impl;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorker;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link IndexerWorkerFactory}.
 */
public class DefaultIndexerWorkerFactory implements IndexerWorkerFactory, ApplicationContextAware
{
	private String workerBeanId;

	private ApplicationContext applicationContext;

	public String getWorkerBeanId()
	{
		return workerBeanId;
	}

	@Required
	public void setWorkerBeanId(final String workerBeanId)
	{
		this.workerBeanId = workerBeanId;
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
	public IndexerWorker createIndexerWorker(final FacetSearchConfig facetSearchConfig) throws IndexerException
	{
		try
		{
			return applicationContext.getBean(workerBeanId, IndexerWorker.class);
		}
		catch (final BeansException exception)
		{
			throw new IndexerException("Cannot create indexer worker [" + workerBeanId + "]", exception);
		}
	}
}
