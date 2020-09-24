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
package de.hybris.platform.solrfacetsearch.config.factories.impl;

import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FlexibleSearchQuerySpec;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;
import de.hybris.platform.solrfacetsearch.config.exceptions.ParameterProviderException;
import de.hybris.platform.solrfacetsearch.config.factories.FlexibleSearchQuerySpecFactory;
import de.hybris.platform.solrfacetsearch.config.impl.IndexTypeFSQSpec;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.provider.ContextAwareParameterProvider;
import de.hybris.platform.solrfacetsearch.provider.ParameterProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexOperationService;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link FlexibleSearchQuerySpecFactory}.
 */
public class DefaultFlexibleSearchQuerySpecFactory implements FlexibleSearchQuerySpecFactory, BeanFactoryAware
{
	private static final Logger LOG = Logger.getLogger(DefaultFlexibleSearchQuerySpecFactory.class);

	protected static final String CURRENTDATE = "currentDate";
	protected static final String CURRENTTIME = "currentTime";
	protected static final String LASTINDEXTIME = "lastIndexTime";

	private SolrIndexService indexService;
	private SolrIndexOperationService indexOperationService;
	private TimeService timeService;
	private BeanFactory beanFactory;

	@Override
	public FlexibleSearchQuerySpec createIndexQuery(final IndexedTypeFlexibleSearchQuery indexTypeFlexibleSearchQueryData,
			final IndexedType indexedType, final FacetSearchConfig facetSearchConfig) throws SolrServiceException
	{
		populateRuntimeParameters(indexTypeFlexibleSearchQueryData, indexedType, facetSearchConfig);
		return new IndexTypeFSQSpec<IndexedTypeFlexibleSearchQuery>(indexTypeFlexibleSearchQueryData);
	}

	protected void populateRuntimeParameters(final IndexedTypeFlexibleSearchQuery indexTypeFlexibleSearchQueryData,
			final IndexedType indexedType, final FacetSearchConfig facetSearchConfig) throws SolrServiceException
	{
		Map<String, Object> parameters = indexTypeFlexibleSearchQueryData.getParameters();
		if (parameters == null)
		{
			indexTypeFlexibleSearchQueryData.setParameters(new HashMap<String, Object>());
			parameters = indexTypeFlexibleSearchQueryData.getParameters();
		}
		if (indexTypeFlexibleSearchQueryData.isInjectLastIndexTime())
		{
			parameters.put(LASTINDEXTIME, getLastIndexTime(facetSearchConfig, indexedType));
		}
		if (indexTypeFlexibleSearchQueryData.isInjectCurrentTime())
		{
			parameters.put(CURRENTTIME, getCurrentTime());
		}
		if (indexTypeFlexibleSearchQueryData.isInjectCurrentDate())
		{
			parameters.put(CURRENTDATE, getCurrentDate());
		}
		if (StringUtils.isNotEmpty(indexTypeFlexibleSearchQueryData.getParameterProviderId()))
		{
			populateParametersByProvider(indexTypeFlexibleSearchQueryData.getParameterProviderId(), parameters, indexedType,
					facetSearchConfig.getIndexConfig());
		}
	}

	protected void populateParametersByProvider(final String parameterProviderId, final Map<String, Object> parameters,
			final IndexedType indexedType, final IndexConfig indexConfig)
	{
		try
		{
			final Object parameterProvider = beanFactory.getBean(parameterProviderId);
			if (parameterProvider instanceof ParameterProvider)
			{
				parameters.putAll(((ParameterProvider) parameterProvider).createParameters());
			}
			if (parameterProvider instanceof ContextAwareParameterProvider)
			{
				parameters.putAll(((ContextAwareParameterProvider) parameterProvider).createParameters(indexConfig, indexedType));
			}
		}
		catch (final NoSuchBeanDefinitionException e)
		{
			throw new ParameterProviderException(
					"Could not create flexible search query parameters by [" + parameterProviderId + "] provider", e);
		}
		catch (final RuntimeException e)
		{
			LOG.error("Error creating FSQ parameters using provider : [" + parameterProviderId + "]", e);
		}
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	protected Date getLastIndexTime(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
			throws SolrServiceException
	{
		Date lastIndexTime;

		try
		{
			final SolrIndexModel activeIndex = indexService.getActiveIndex(facetSearchConfig.getName(), indexedType.getIdentifier());
			lastIndexTime = indexOperationService.getLastIndexOperationTime(activeIndex);
		}
		catch (final SolrIndexNotFoundException e)
		{
			LOG.debug(e);
			lastIndexTime = new Date(0);
		}

		return lastIndexTime;
	}

	protected Date getCurrentDate()
	{
		final Calendar now = Calendar.getInstance();
		now.setTime(getCurrentTime());
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);
		return now.getTime();
	}

	protected Date getCurrentTime()
	{
		return timeService.getCurrentTime();
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;

	}

	public SolrIndexService getIndexService()
	{
		return indexService;
	}

	@Required
	public void setIndexService(final SolrIndexService indexService)
	{
		this.indexService = indexService;
	}

	@Required
	public void setIndexOperationService(final SolrIndexOperationService indexOperationService)
	{
		this.indexOperationService = indexOperationService;
	}

	public SolrIndexOperationService getIndexOperationService()
	{
		return indexOperationService;
	}
}
