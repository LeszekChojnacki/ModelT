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
package de.hybris.platform.solrserver;

import java.util.Map;


/**
 * This represents the main entry point for all operations regarding Solr instances and servers.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public interface SolrServerService
{
	/**
	 * Performs the initialization of this service.
	 *
	 * @throws SolrServerException
	 *            if an error occurs during initialization
	 */
	void init() throws SolrServerException;

	/**
	 * Performs the shutdown of this service.
	 *
	 * @throws SolrServerException
	 *            if an error occurs during shutdown
	 */
	void destroy() throws SolrServerException;

	/**
	 * Initializes the Solr server instances.
	 */
	void initializeInstances();

	/**
	 * Returns all configured Solr instances. The key of the map is the name of the instance.
	 *
	 * @return the configured Solr instances
	 */
	Map<String, SolrInstance> getInstances();

	/**
	 * Returns the configured Solr instance for a given name.
	 *
	 * @param instanceName
	 *           - the instance name
	 *
	 * @return the configured Solr instance
	 *
	 * @throws SolrInstanceNotFoundException
	 *            if an instance is not found for the given name
	 */
	SolrInstance getInstanceForName(final String instanceName) throws SolrInstanceNotFoundException;

	/**
	 * Creates a Solr instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            if an error occurs while creating the Solr instance.
	 */
	void createInstance(SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Deletes a Solr instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            if an error occurs while deleting the Solr instance.
	 */
	void deleteInstance(SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Starts the Solr server for a given instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            if an error occurs while starting the server.
	 */
	void startServer(SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Stops the Solr server for a given instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            if an error occurs while stopping the server.
	 */
	void stopServer(SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Starts all Solr servers that are configured to be automatically started/stopped.
	 *
	 * @see SolrInstance#isAutostart()
	 *
	 * @throws SolrServerException
	 *            if an error occurs while starting the servers, depending on configuration the errors might be ignored and
	 *            the exception not thrown
	 */
	void startServers() throws SolrServerException;

	/**
	 * Stops all Solr servers that are configured to be automatically started/stopped.
	 *
	 * @see SolrInstance#isAutostart()
	 *
	 * @throws SolrServerException
	 *            if an error occurs while stopping the servers, depending on configuration the errors might be ignored and
	 *            the exception not thrown
	 */
	void stopServers() throws SolrServerException;

	/**
	 * Uploads the Solr server configuration.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            - if an error occurs while uploading the configuration
	 */
	void uploadConfig(SolrInstance solrInstance) throws SolrServerException;
}
