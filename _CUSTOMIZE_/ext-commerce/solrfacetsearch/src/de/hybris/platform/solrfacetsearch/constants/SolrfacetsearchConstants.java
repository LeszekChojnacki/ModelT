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
package de.hybris.platform.solrfacetsearch.constants;



/**
 * Global class for all Solrfacetsearch constants. You can add global constants for your extension into this class.
 */
public final class SolrfacetsearchConstants extends GeneratedSolrfacetsearchConstants
{
	public static final String EXTENSIONNAME = "solrfacetsearch";

	public static final String EXECUTE_INDEXER_OPERATION = "Solrfacetsearch-IndexerOperation";

	public static final String INDEXER_JOB_SPRING_ID = "solrIndexerJob";
	public static final String INDEXER_HOTUPDATE_JOB_SPRING_ID = "solrIndexerHotUpdateJob";

	public static final String ALL_FIELDS = "*";
	public static final String ID_FIELD = "id";
	public static final String PK_FIELD = "pk";
	public static final String INDEX_OPERATION_ID_FIELD = "indexOperationId";
	public static final String CATALOG_ID_FIELD = "catalogId";
	public static final String CATALOG_VERSION_FIELD = "catalogVersion";
	public static final String SCORE_FIELD = "score";

	public static final String STATUS_ERROR_EXCEPTION = "ERROR_EXCEPTION";
	public static final String STATUS_ERROR = "ERROR";

	public static final String SERVER_MODE_TEMPLATE = "solr.config.%s.mode";
	public static final String SERVER_URLS_TEMPLATE = "solr.config.%s.urls";

	public static final String SERVER_USE_MASTER_NODE_EXCLUSIVELY_FOR_INDEXING = "solr.config.%s.useMasterNodeExclusivelyForIndexing";
	public static final String SERVER_NUM_SHARDS = "solr.config.%s.numShards";
	public static final String SERVER_REPLICATION_FACTOR = "solr.config.%s.replicationFactor";

	public static final String CLIENT_ALIVE_CHECK_INTERVAL_TEMPLATE = "solr.config.%s.client.aliveCheckInterval";
	public static final String CLIENT_CONNECTION_TIMEOUT_TEMPLATE = "solr.config.%s.client.connectionTimeout";
	public static final String CLIENT_SOCKET_TIMEOUT_TEMPLATE = "solr.config.%s.client.socketTimeout";
	public static final String CLIENT_MAX_CONNECTIONS_TEMPLATE = "solr.config.%s.client.maxConnections";
	public static final String CLIENT_MAX_CONNECTIONS_PER_HOST_TEMPLATE = "solr.config.%s.client.maxConnectionsPerHost";
	public static final String CLIENT_USERNAME_TEMPLATE = "solr.config.%s.client.username";
	public static final String CLIENT_PASSWORD_TEMPLATE = "solr.config.%s.client.password";

	public static final String INDEXING_CLIENT_ALIVE_CHECK_INTERVAL_TEMPLATE = "solr.config.%s.indexingclient.aliveCheckInterval";
	public static final String INDEXING_CLIENT_CONNECTION_TIMEOUT_TEMPLATE = "solr.config.%s.indexingclient.connectionTimeout";
	public static final String INDEXING_CLIENT_SOCKET_TIMEOUT_TEMPLATE = "solr.config.%s.indexingclient.socketTimeout";
	public static final String INDEXING_CLIENT_MAX_CONNECTIONS_TEMPLATE = "solr.config.%s.indexingclient.maxConnections";
	public static final String INDEXING_CLIENT_MAX_CONNECTIONS_PER_HOST_TEMPLATE = "solr.config.%s.indexingclient.maxConnectionsPerHost";
	public static final String INDEXING_CLIENT_USERNAME_TEMPLATE = "solr.config.%s.indexingclient.username";
	public static final String INDEXING_CLIENT_PASSWORD_TEMPLATE = "solr.config.%s.indexingclient.password";

	public static final String PROMOTED_TAG = "promoted";
	public static final String EXCLUDED_TAG = "excluded";

	private SolrfacetsearchConstants()
	{
		//empty to avoid instantiating this constant class
	}
}
