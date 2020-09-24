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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.impl.SolrClientBuilder;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.SolrResponseBase;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.Utils;
import org.fest.util.Collections;


/**
 * {@link SolrSearchProvider} implementation for Solr Legacy Distribution and Replication.
 */
public class SolrStandaloneSearchProvider extends AbstractSolrSearchProvider
{
	private static final Logger LOG = Logger.getLogger(SolrStandaloneSearchProvider.class);

	@Override
	public CachedSolrClient getClient(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);
		return getSolrClientPool().getOrCreate(index, SolrClientType.SEARCH, this::createClient, this::closeClient);
	}

	protected SolrClient createClient(final SolrConfig solrConfig)
	{
		validateConfiguration(solrConfig);

		final String[] urls = solrConfig.getEndpointURLs().stream()
				.filter(url -> !solrConfig.isUseMasterNodeExclusivelyForIndexing() || !url.isMaster()).map(EndpointURL::getUrl)
				.toArray(String[]::new);

		final SolrClientConfig clientConfig = solrConfig.getClientConfig();

		final LBHttpSolrClient solrClient = new PatchedLBHttpSolrClient.Builder().withBaseSolrUrls(urls)
				.withHttpClient(createHttpClient(clientConfig))
				.withConnectionTimeout(getIntegerValue(clientConfig.getConnectionTimeout(), DEFAULT_CONNECTION_TIMEOUT))
				.withSocketTimeout(getIntegerValue(clientConfig.getSocketTimeout(), DEFAULT_SOCKET_TIMEOUT)).build();

		solrClient.setAliveCheckInterval(getIntegerValue(clientConfig.getAliveCheckInterval(), DEFAULT_ALIVE_CHECK_INTERVAL));

		return solrClient;
	}

	protected void closeClient(final SolrClient solrClient)
	{
		final LBHttpSolrClient lbHttpSolrClient = (LBHttpSolrClient) solrClient;
		closeHttpClient(lbHttpSolrClient.getHttpClient());
	}

	@Override
	public CachedSolrClient getClientForIndexing(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);
		return getSolrClientPool().getOrCreate(index, SolrClientType.INDEXING, this::createClientForIndexing,
				this::closeClientForIndexing);
	}

	protected SolrClient createClientForIndexing(final SolrConfig solrConfig)
	{
		validateConfiguration(solrConfig);

		final String[] urls = solrConfig.getEndpointURLs().stream().map(EndpointURL::getUrl).toArray(String[]::new);

		final Optional<String> defaultUrl = solrConfig.getEndpointURLs().stream().filter(EndpointURL::isMaster)
				.map(EndpointURL::getUrl).findFirst();

		final SolrClientConfig clientConfig = solrConfig.getIndexingClientConfig();

		return new ClusterSolrClient.Builder().withBaseSolrUrls(urls).withDefaultBaseSolrUrl(defaultUrl.get())
				.withHttpClient(createHttpClient(clientConfig))
				.withConnectionTimeout(getIntegerValue(clientConfig.getConnectionTimeout(), DEFAULT_CONNECTION_TIMEOUT))
				.withSocketTimeout(getIntegerValue(clientConfig.getSocketTimeout(), DEFAULT_SOCKET_TIMEOUT)).build();
	}

	protected void closeClientForIndexing(final SolrClient solrClient)
	{
		final ClusterSolrClient clusterSolrClient = (ClusterSolrClient) solrClient;
		closeHttpClient(clusterSolrClient.getHttpClient());
	}

	@Override
	public void createIndex(final Index index) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);

		final CachedSolrClient solrClient = getClientForIndexing(index);

		try
		{
			final List<ClusterNodeResponse<CoreAdminResponse>> statusResponses = doCheckIndexStatus(index, solrClient, null);
			final List<String> failedStatusNodes = statusResponses.stream().filter(resp -> !resp.isSuccess())
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(failedStatusNodes))
			{
				throw new SolrServiceException(
						"Could not check index status: index=" + index.getName() + ", nodes=" + failedStatusNodes);
			}

			// collects the nodes where the index does not yet exist
			final List<String> createNodes = statusResponses.stream().filter(resp -> !indexExists(index, resp.getResponse()))
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(createNodes))
			{
				return;
			}

			final List<ClusterNodeResponse<CoreAdminResponse>> createResponses = doCreateIndex(index, solrClient, createNodes);
			final List<String> failedCreateNodes = createResponses.stream().filter(resp -> !resp.isSuccess())
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(failedCreateNodes))
			{
				doDeleteIndex(index, solrClient, failedCreateNodes);

				throw new SolrServiceException("Could not create index: index=" + index.getName() + ", nodes=" + failedCreateNodes);
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
			final List<ClusterNodeResponse<CoreAdminResponse>> statusResponses = doCheckIndexStatus(index, solrClient, null);
			final List<String> failedStatusNodes = statusResponses.stream().filter(resp -> !resp.isSuccess())
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(failedStatusNodes))
			{
				throw new SolrServiceException(
						"Could not check index status: index=" + index.getName() + ", nodes=" + failedStatusNodes);
			}

			// collects the nodes where the index exists
			final List<String> deleteNodes = statusResponses.stream().filter(resp -> indexExists(index, resp.getResponse()))
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(deleteNodes))
			{
				return;
			}

			final List<ClusterNodeResponse<CoreAdminResponse>> deleteResponses = doDeleteIndex(index, solrClient, deleteNodes);
			final List<String> failedDeleteNodes = deleteResponses.stream().filter(resp -> !resp.isSuccess())
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(failedDeleteNodes))
			{
				throw new SolrServiceException(
						"Could not delete index for nodes: index=" + index.getName() + ", nodes=" + failedDeleteNodes);
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
			final ClusterSolrClient clusterSolrClient = (ClusterSolrClient) solrClient.getDelegate();

			for (final String node : clusterSolrClient.getNodes())
			{
				try (SolrClient clusterNodeSolrClient = clusterSolrClient.getNodeClient(solrClient, node))
				{
					exportConfig(index, clusterNodeSolrClient);
				}
			}

			// reloads the index
			final List<ClusterNodeResponse<CoreAdminResponse>> reloadResponses = doReloadIndex(index, solrClient, null);
			final List<String> failedReloadNodes = reloadResponses.stream().filter(resp -> !resp.isSuccess())
					.map(ClusterNodeResponse::getNode).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(failedReloadNodes))
			{
				throw new SolrServiceException(
						"Could not reload configuration for index: name=" + index.getName() + ", nodes=" + failedReloadNodes);
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

		// Verify that at least one and only one end-point URL is specified as master
		boolean masterSpecified = false;
		for (final EndpointURL endpointURL : endpointURLs)
		{
			if (endpointURL.isMaster())
			{
				if (masterSpecified)
				{
					throw new SolrServiceRuntimeException("Only one endpoint URL can be specified as master.");
				}
				masterSpecified = true;
			}
		}

		if (!masterSpecified)
		{
			throw new SolrServiceRuntimeException("No endpoint URL's specified as master.");
		}
	}

	protected boolean indexExists(final Index index, final CoreAdminResponse response)
	{
		final Object statusIndexName = Utils.getObjectByPath(response.getCoreStatus(), false,
				Arrays.asList(index.getName(), "name"));

		return Objects.equals(index.getName(), statusIndexName);
	}

	protected List<ClusterNodeResponse<CoreAdminResponse>> doCheckIndexStatus(final Index index, final CachedSolrClient solrClient,
			final List<String> nodes)
	{
		final CoreAdminRequest request = new CoreAdminRequest();
		request.setAction(CoreAdminAction.STATUS);
		request.setCoreName(index.getName());

		return clusterRequest(request, solrClient, null, nodes);
	}

	protected List<ClusterNodeResponse<CoreAdminResponse>> doCreateIndex(final Index index, final CachedSolrClient solrClient,
			final List<String> nodes)
	{
		final String configSet = resolveConfigSet(index);

		final CoreAdminRequest.Create request = new CoreAdminRequest.Create();
		request.setCoreName(index.getName());
		request.setConfigSet(configSet);

		return clusterRequest(request, solrClient, null, nodes);
	}

	protected List<ClusterNodeResponse<CoreAdminResponse>> doDeleteIndex(final Index index, final CachedSolrClient solrClient,
			final List<String> nodes)
	{
		final CoreAdminRequest.Unload request = new CoreAdminRequest.Unload(true);
		request.setCoreName(index.getName());

		return clusterRequest(request, solrClient, null, nodes);
	}

	protected List<ClusterNodeResponse<CoreAdminResponse>> doReloadIndex(final Index index, final CachedSolrClient solrClient,
			final List<String> nodes)
	{
		final CoreAdminRequest request = new CoreAdminRequest();
		request.setAction(CoreAdminAction.RELOAD);
		request.setCoreName(index.getName());

		return clusterRequest(request, solrClient, null, nodes);
	}

	protected <T extends SolrResponseBase> List<ClusterNodeResponse<T>> clusterRequest(final SolrRequest<T> request,
			final CachedSolrClient solrClient, final String collection, final List<String> nodes)
	{
		final ClusterSolrClient clusterSolrClient = (ClusterSolrClient) solrClient.getDelegate();
		final Collection<String> requestNodes = Collections.isEmpty(nodes) ? clusterSolrClient.getNodes() : nodes;
		final List<ClusterNodeResponse<T>> nodeResponses = new ArrayList<>();

		for (final String requestNode : requestNodes)
		{
			final ClusterNodeResponse<T> nodeResponse = new ClusterNodeResponse<>();
			nodeResponse.setNode(requestNode);

			try (SolrClient nodeSolrClient = clusterSolrClient.getNodeClient(solrClient, requestNode))
			{
				final T response = request.process(nodeSolrClient, collection);
				nodeResponse.setResponse(response);

				if (response.getStatus() == 0)
				{
					nodeResponse.setSuccess(true);
				}
				else
				{
					nodeResponse.setSuccess(false);
				}
			}
			catch (final Exception e)
			{
				LOG.error(e);

				nodeResponse.setSuccess(false);
				nodeResponse.setException(e);
			}

			nodeResponses.add(nodeResponse);
		}

		return nodeResponses;
	}

	public static class ClusterNodeResponse<T extends SolrResponse>
	{
		private String node;
		private T response;
		private boolean success;
		private Exception exception;

		public String getNode()
		{
			return node;
		}

		public void setNode(final String node)
		{
			this.node = node;
		}

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

	protected static class ClusterSolrClient extends SolrClient
	{
		private static final long serialVersionUID = 1L;

		public static final String REQUEST_NODE_SEPARATOR = "#";

		private final HttpClient httpClient;
		private final boolean clientIsInternal;
		private final ResponseParser parser;
		private final Integer connectionTimeout;
		private final Integer socketTimeout;

		private final Map<String, HttpSolrClient> solrClients;
		private final String defaultNode;

		protected ClusterSolrClient(final Builder builder)
		{
			if (builder.getHttpClient() != null)
			{
				this.httpClient = builder.getHttpClient();
				this.clientIsInternal = true;
			}
			else
			{
				final ModifiableSolrParams params = new ModifiableSolrParams();
				this.httpClient = HttpClientUtil.createClient(params);
				this.clientIsInternal = true;
			}

			this.parser = builder.getResponseParser();
			this.connectionTimeout = builder.getConnectionTimeoutMillis();
			this.socketTimeout = builder.getSocketTimeoutMillis();

			this.solrClients = createSolrClients(builder.getBaseSolrUrls());
			this.defaultNode = builder.getDefaultBaseSolrUrl();
		}

		protected final Map<String, HttpSolrClient> createSolrClients(final List<String> baseSolrUrls)
		{
			final Map<String, HttpSolrClient> clients = new HashMap<>();

			for (final String baseSolrUrl : baseSolrUrls)
			{
				clients.computeIfAbsent(baseSolrUrl, this::createSolrClient);
			}

			return clients;
		}

		protected final HttpSolrClient createSolrClient(final String baseSolrUrl)
		{
			final HttpSolrClient.Builder clientBuilder = new HttpSolrClient.Builder(baseSolrUrl).withHttpClient(httpClient)
					.withResponseParser(parser).withConnectionTimeout(connectionTimeout).withSocketTimeout(socketTimeout);

			return clientBuilder.build();
		}

		public HttpClient getHttpClient()
		{
			return httpClient;
		}

		public Collection<String> getNodes()
		{
			return solrClients.keySet();
		}

		public SolrClient getNodeClient(final SolrClient solrClient, final String node)
		{
			return new ClusterNodeSolrClient(solrClient, node);
		}

		@Override
		public NamedList<Object> request(final SolrRequest request, final String collection) throws SolrServerException, IOException
		{
			final String[] paths = request.getPath().split(REQUEST_NODE_SEPARATOR);

			if (paths.length == 2)
			{
				final String previousPath = request.getPath();

				try
				{
					final String path = paths[0];
					final String node = paths[1];

					request.setPath(path);
					return solrClients.get(node).request(request, collection);
				}
				finally
				{
					request.setPath(previousPath);
				}
			}
			else
			{
				return solrClients.get(defaultNode).request(request, collection);
			}
		}

		@Override
		public void close()
		{
			if (clientIsInternal)
			{
				HttpClientUtil.close(httpClient);
			}

			for (final SolrClient solrClient : solrClients.values())
			{
				IOUtils.closeQuietly(solrClient);
			}
		}

		protected static class ClusterNodeSolrClient extends SolrClient
		{
			private static final long serialVersionUID = 1L;

			private final SolrClient solrClient;
			private final String node;

			ClusterNodeSolrClient(final SolrClient solrClient, final String node)
			{
				this.solrClient = solrClient;
				this.node = node;
			}

			protected SolrClient getSolrClient()
			{
				return solrClient;
			}

			protected String getNode()
			{
				return node;
			}

			@Override
			public NamedList<Object> request(final SolrRequest request, final String collection)
					throws SolrServerException, IOException
			{
				final String previousPath = request.getPath();

				try
				{
					request.setPath(previousPath + ClusterSolrClient.REQUEST_NODE_SEPARATOR + node);
					return solrClient.request(request, collection);
				}
				finally
				{
					request.setPath(previousPath);
				}
			}

			@Override
			public void close() throws IOException
			{
				// empty
			}
		}

		public static class Builder extends SolrClientBuilder<Builder>
		{
			private final List<String> baseSolrUrls;
			private String defaultBaseSolrUrl;

			public Builder()
			{
				this.baseSolrUrls = new ArrayList<>();
				this.responseParser = new BinaryResponseParser();
			}

			protected List<String> getBaseSolrUrls()
			{
				return baseSolrUrls;
			}

			public String getDefaultBaseSolrUrl()
			{
				return defaultBaseSolrUrl;
			}

			public HttpClient getHttpClient()
			{
				return httpClient;
			}

			public ResponseParser getResponseParser()
			{
				return responseParser;
			}

			public Integer getConnectionTimeoutMillis()
			{
				return connectionTimeoutMillis;
			}

			public Integer getSocketTimeoutMillis()
			{
				return socketTimeoutMillis;
			}

			public Builder withBaseSolrUrls(final String... baseSolrUrls)
			{
				for (final String baseSolrUrl : baseSolrUrls)
				{
					this.baseSolrUrls.add(baseSolrUrl);
				}

				return this;
			}

			public Builder withDefaultBaseSolrUrl(final String defaultBaseSolrUrl)
			{
				this.defaultBaseSolrUrl = defaultBaseSolrUrl;

				return this;
			}

			public ClusterSolrClient build()
			{
				if (CollectionUtils.isEmpty(baseSolrUrls))
				{
					throw new IllegalArgumentException("Cannot create ClusterSolrClient without a valid baseSolrUrl!");
				}

				return new ClusterSolrClient(this);
			}

			@Override
			public Builder getThis()
			{
				return this;
			}
		}
	}
}
