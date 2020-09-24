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

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.solrfacetsearch.config.EndpointURL;
import de.hybris.platform.solrfacetsearch.config.SolrClientConfig;
import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrClientType;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceRuntimeException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;


/**
 * {@link SolrSearchProvider} implementation for Solr Cloud.
 */
public class SolrCloudSearchProvider extends AbstractSolrSearchProvider
{
	private static final Logger LOG = Logger.getLogger(SolrCloudSearchProvider.class);

	protected static final String NUMBER_OF_SHARDS_PARAM = "solr.collection.numShards";
	protected static final int DEFAULT_NUMBER_OF_SHARDS = 1;

	protected static final String REPLICATION_FACTOR_PARAM = "solr.collection.replicationFactor";
	protected static final int DEFAULT_REPLICATION_FACTOR = 1;

	@Override
	public CachedSolrClient getClient(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		return getSolrClientPool().getOrCreate(index, SolrClientType.SEARCH,
				solrConfig -> createClient(solrConfig, solrConfig.getClientConfig()), this::closeClient);
	}

	@Override
	public CachedSolrClient getClientForIndexing(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		return getSolrClientPool().getOrCreate(index, SolrClientType.INDEXING,
				solrConfig -> createClient(solrConfig, solrConfig.getIndexingClientConfig()), this::closeClient);
	}

	protected CloudSolrClient createClient(final SolrConfig solrConfig, final SolrClientConfig solrClientConfig)
	{
		validateConfiguration(solrConfig);

		final List<String> zkHosts = solrConfig.getEndpointURLs().stream().map(EndpointURL::getUrl).collect(Collectors.toList());

		final CloudSolrClient.Builder cloudClientBuilder = new CloudSolrClient.Builder().withZkHost(zkHosts)
				.withHttpClient(createHttpClient(solrClientConfig))
				.withConnectionTimeout(getIntegerValue(solrClientConfig.getConnectionTimeout(), DEFAULT_CONNECTION_TIMEOUT))
				.withSocketTimeout(getIntegerValue(solrClientConfig.getSocketTimeout(), DEFAULT_SOCKET_TIMEOUT))
				.sendUpdatesOnlyToShardLeaders();

		final CloudSolrClient solrClient = cloudClientBuilder.build();
		final LBHttpSolrClient lbHttpSolrClient = solrClient.getLbClient();

		lbHttpSolrClient
				.setAliveCheckInterval(getIntegerValue(solrClientConfig.getAliveCheckInterval(), DEFAULT_ALIVE_CHECK_INTERVAL));

		solrClient.connect();

		return solrClient;
	}

	protected void closeClient(final SolrClient solrClient)
	{
		final CloudSolrClient cloudSolrClient = (CloudSolrClient) solrClient;
		closeHttpClient(cloudSolrClient.getHttpClient());
	}

	@Override
	public void createIndex(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		final CachedSolrClient solrClient = getClientForIndexing(index);

		try
		{
			final CloudResponse<CollectionAdminResponse> listResponse = doListIndexes(solrClient);
			if (!listResponse.isSuccess())
			{
				throw new SolrServiceException("Could not check index status: name=" + index.getName(), listResponse.getException());
			}

			if (indexExists(index, listResponse.getResponse()))
			{
				return;
			}

			final CloudResponse<CollectionAdminResponse> createResponse = doCreateIndex(index, solrClient);
			if (!createResponse.isSuccess())
			{
				throw new SolrServiceException("Could not create index: name=" + index.getName(), createResponse.getException());
			}
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void deleteIndex(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		final CachedSolrClient solrClient = getClientForIndexing(index);

		try
		{
			final CloudResponse<CollectionAdminResponse> listResponse = doListIndexes(solrClient);
			if (!listResponse.isSuccess())
			{
				throw new SolrServiceException("Could not check index status: name=" + index.getName(), listResponse.getException());
			}

			if (!indexExists(index, listResponse.getResponse()))
			{
				return;
			}

			final CloudResponse<CollectionAdminResponse> deleteResponse = doDeleteIndex(index, solrClient);
			if (!deleteResponse.isSuccess())
			{
				throw new SolrServiceException("Could not create index: name=" + index.getName(), deleteResponse.getException());
			}
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}
	}

	@Override
	public void exportConfig(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		try (final CachedSolrClient solrClient = getClientForIndexing(index))
		{
			// exports the configuration
			exportConfig(index, solrClient);

			// reloads the index
			final CloudResponse<CollectionAdminResponse> reloadResponse = doReloadIndex(index, solrClient);
			if (!reloadResponse.isSuccess())
			{
				throw new SolrServiceException("Could not reload configuration for index: name=" + index.getName(),
						reloadResponse.getException());
			}
		}
		catch (final Exception e)
		{
			throw new SolrServiceException("Could not export configuration for index: name=" + index.getName(), e);
		}
	}

	protected void validateConfiguration(final SolrConfig solrConfig)
	{
		validateParameterNotNullStandardMessage("configModel", solrConfig);

		// Verify that there is at least one end-point URL specified
		final List<EndpointURL> endpointURLs = solrConfig.getEndpointURLs();
		if (CollectionUtils.isEmpty(endpointURLs))
		{
			throw new SolrServiceRuntimeException("No endpoint URL's defined in solr configuration.");
		}
	}

	protected boolean indexExists(final Index index, final CollectionAdminResponse response)
	{
		final List<String> collections = (List<String>) response.getResponse().get("collections");
		return collections.contains(index.getName());
	}

	protected CloudResponse<CollectionAdminResponse> doListIndexes(final CachedSolrClient solrClient)
	{
		final CollectionAdminRequest.List request = new CollectionAdminRequest.List();
		return cloudRequest(request, solrClient, null);
	}

	protected CloudResponse<CollectionAdminResponse> doCreateIndex(final Index index, final CachedSolrClient solrClient)
	{
		final String configSet = resolveConfigSet(index);
		final Integer numShards = resolveNumShards(index);
		final Integer replicationFactor = resolveReplicationFactor(index);

		final CollectionAdminRequest.Create request = CollectionAdminRequest.createCollection(index.getName(), configSet, numShards,
				replicationFactor);

		return cloudRequest(request, solrClient, null);
	}

	protected CloudResponse<CollectionAdminResponse> doDeleteIndex(final Index index, final CachedSolrClient solrClient)
	{
		final CollectionAdminRequest.Delete request = CollectionAdminRequest.deleteCollection(index.getName());
		return cloudRequest(request, solrClient, null);
	}

	protected CloudResponse<CollectionAdminResponse> doReloadIndex(final Index index, final CachedSolrClient solrClient)
	{
		final CollectionAdminRequest.Reload request = CollectionAdminRequest.reloadCollection(index.getName());
		return cloudRequest(request, solrClient, null);
	}

	protected <T extends SolrResponseBase> CloudResponse<T> cloudRequest(final SolrRequest<T> request,
			final CachedSolrClient solrClient, final String collection)
	{
		final CloudResponse<T> cloudResponse = new CloudResponse<>();

		try
		{
			final T response = request.process(solrClient, collection);
			cloudResponse.setResponse(response);

			if (response.getStatus() == 0)
			{
				cloudResponse.setSuccess(true);
			}
			else
			{
				cloudResponse.setSuccess(false);
			}
		}
		catch (final Exception e)
		{
			LOG.error(e);

			cloudResponse.setSuccess(false);
			cloudResponse.setException(e);
		}

		return cloudResponse;
	}

	protected Integer resolveNumShards(final Index index)
	{
		final Map<String, String> additionalParameters = index.getIndexedType().getAdditionalParameters();
		final String numShardsParam = additionalParameters.get(NUMBER_OF_SHARDS_PARAM);

		if (StringUtils.isNotBlank(numShardsParam))
		{
			try
			{
				return Integer.parseInt(numShardsParam);
			}
			catch (final NumberFormatException e)
			{
				LOG.error("Additional parameter 'number of shards' is not a number");
			}
		}

		final SolrConfig config = index.getFacetSearchConfig().getSolrConfig();
		final Integer numShards = config.getNumShards() == null ? DEFAULT_NUMBER_OF_SHARDS : config.getNumShards();
		LOG.debug("Number of shards " + numShards);

		return numShards;
	}

	protected Integer resolveReplicationFactor(final Index index)
	{
		final Map<String, String> additionalParameters = index.getIndexedType().getAdditionalParameters();
		final String replicationFactorParam = additionalParameters.get(REPLICATION_FACTOR_PARAM);

		if (StringUtils.isNotBlank(replicationFactorParam))
		{
			try
			{
				return Integer.parseInt(replicationFactorParam);
			}
			catch (final NumberFormatException e)
			{
				LOG.error("Additional parameter 'replication factor' is not a number");
			}
		}

		final SolrConfig config = index.getFacetSearchConfig().getSolrConfig();
		final Integer replicationFactor = config.getNumShards() == null ? DEFAULT_REPLICATION_FACTOR
				: config.getReplicationFactor();
		LOG.debug("Replication factor " + replicationFactor);

		return replicationFactor;
	}

	protected static class CloudResponse<T extends SolrResponse>
	{
		private T response;
		private boolean success;
		private Exception exception;

		public T getResponse()
		{
			return response;
		}

		public void setResponse(final T response)
		{
			this.response = response;
		}

		public boolean isSuccess()
		{
			return success;
		}

		public void setSuccess(final boolean success)
		{
			this.success = success;
		}

		public Exception getException()
		{
			return exception;
		}

		public void setException(final Exception exception)
		{
			this.exception = exception;
		}
	}
}
