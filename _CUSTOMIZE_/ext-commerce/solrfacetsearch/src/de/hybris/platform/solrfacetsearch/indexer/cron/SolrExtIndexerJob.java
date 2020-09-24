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
package de.hybris.platform.solrfacetsearch.indexer.cron;

import de.hybris.platform.core.PK;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueriesExecutor;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerJobException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrExtIndexerCronJobModel;
import de.hybris.platform.solrfacetsearch.provider.ContextAwareParameterProvider;
import de.hybris.platform.solrfacetsearch.provider.CronJobAwareParameterProvider;
import de.hybris.platform.solrfacetsearch.provider.ParameterProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;


/**
 * Job performable that triggers indexer operations. It allows you to have a separate query per
 * {@link SolrExtIndexerCronJobModel} instance. It also allows you to perform partial updates.
 */
public class SolrExtIndexerJob<T extends SolrExtIndexerCronJobModel> extends AbstractJobPerformable<T> implements BeanFactoryAware
{
	private static final Logger LOG = Logger.getLogger(SolrExtIndexerJob.class);

	private FacetSearchConfigService facetSearchConfigService;
	private IndexerService indexerService;
	private IndexerQueriesExecutor indexerQueriesExecutor;

	private BeanFactory beanFactory;

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	public FacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setIndexerService(final IndexerService indexerService)
	{
		this.indexerService = indexerService;
	}

	public IndexerService getIndexerService()
	{
		return indexerService;
	}

	@Required
	public void setIndexerQueriesExecutor(final IndexerQueriesExecutor indexerQueriesExecutor)
	{
		this.indexerQueriesExecutor = indexerQueriesExecutor;
	}

	public IndexerQueriesExecutor getIndexerQueriesExecutor()
	{
		return indexerQueriesExecutor;
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory()
	{
		return beanFactory;
	}

	@Override
	public PerformResult perform(final T cronJob)
	{
		LOG.info("Started ext indexer cronjob.");

		final SolrFacetSearchConfigModel facetSearchConfigModel = cronJob.getFacetSearchConfig();
		final String facetSearchConfigName = facetSearchConfigModel.getName();

		try
		{
			validateCronJobParameters(cronJob);

			final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getConfiguration(facetSearchConfigName);
			final IndexConfig indexConfig = facetSearchConfig.getIndexConfig();
			final IndexedType indexedType = resolveIndexedType(cronJob, indexConfig);
			final List<IndexedProperty> indexedProperties = resolveIndexedProperties(cronJob, indexedType);

			final String query = cronJob.getQuery();
			final Map<String, Object> queryParameters = createQueryParameters(cronJob, indexConfig, indexedType);
			final List<PK> pks = indexerQueriesExecutor.getPks(facetSearchConfig, indexedType, query, queryParameters);

			final Map<String, String> indexerHints = cronJob.getIndexerHints();

			performIndexing(cronJob, facetSearchConfig, indexedType, indexedProperties, pks, indexerHints);
		}
		catch (final FacetConfigServiceException e)
		{
			LOG.error("Error loading configuration with name: " + facetSearchConfigName, e);
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
		}
		catch (final IndexerJobException | IndexerException e)
		{
			LOG.error("Error running indexer job for configuration with name: " + facetSearchConfigName, e);
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	protected void validateCronJobParameters(final T cronJob) throws IndexerJobException
	{
		if (StringUtils.isBlank(cronJob.getIndexedType()))
		{
			throw new IndexerJobException("Indexed type must be defined");
		}

		if (StringUtils.isBlank(cronJob.getQuery()))
		{
			throw new IndexerJobException("Query must be defined");
		}
	}

	protected IndexedType resolveIndexedType(final T cronJob, final IndexConfig indexConfig) throws IndexerJobException
	{
		final String indexedTypeName = cronJob.getIndexedType();
		IndexedType indexedType = null;

		if (indexConfig.getIndexedTypes() != null)
		{
			indexedType = indexConfig.getIndexedTypes().get(indexedTypeName);
		}

		if (indexedType == null)
		{
			throw new IndexerJobException("Indexed type " + indexedTypeName + " not found");
		}

		return indexedType;
	}

	protected List<IndexedProperty> resolveIndexedProperties(final T cronJob, final IndexedType indexedType)
			throws IndexerJobException
	{
		final List<IndexedProperty> indexedProperties = new ArrayList<>();

		final Collection<String> indexedPropertiesNames = cronJob.getIndexedProperties();
		if (indexedPropertiesNames != null)
		{
			for (final String indexedPropertyName : indexedPropertiesNames)
			{
				IndexedProperty indexedProperty = null;

				if (indexedType.getIndexedProperties() != null)
				{
					indexedProperty = indexedType.getIndexedProperties().get(indexedPropertyName);
				}

				if (indexedProperty == null)
				{
					throw new IndexerJobException("Indexed property " + indexedPropertyName + " not found");
				}

				indexedProperties.add(indexedProperty);
			}
		}

		return indexedProperties;
	}

	protected Map<String, Object> createQueryParameters(final T cronJob, final IndexConfig indexConfig,
			final IndexedType indexedType) throws IndexerJobException
	{
		final Map<String, Object> parameters = new HashMap<>();

		final String parameterProviderId = cronJob.getQueryParameterProvider();
		if (!StringUtils.isBlank(parameterProviderId))
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
				if (parameterProvider instanceof CronJobAwareParameterProvider)
				{
					parameters.putAll(
							((CronJobAwareParameterProvider) parameterProvider).createParameters(cronJob, indexConfig, indexedType));
				}
			}
			catch (final NoSuchBeanDefinitionException e)
			{
				throw new IndexerJobException("Could not instantiate parameter provider with id: " + parameterProviderId, e);
			}
		}

		return parameters;
	}

	protected void performIndexing(final T cronJob, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final List<IndexedProperty> indexedProperties, final List<PK> pks, final Map<String, String> indexerHints)
			throws IndexerJobException, IndexerException
	{
		final IndexerOperationValues indexerOperation = cronJob.getIndexerOperation();
		switch (indexerOperation)
		{
			case UPDATE:
				indexerService.updateTypeIndex(facetSearchConfig, indexedType, pks, indexerHints);
				break;
			case PARTIAL_UPDATE:
				indexerService.updatePartialTypeIndex(facetSearchConfig, indexedType, indexedProperties, pks, indexerHints);
				break;
			case DELETE:
				indexerService.deleteTypeIndex(facetSearchConfig, indexedType, pks, indexerHints);
				break;
			default:
				throw new IndexerJobException("Unsupported indexer operation: " + indexerOperation);
		}
	}
}
