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

/**
 * Implementation of this interface are responsible for starting / stopping the Solr server for a given instance.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public interface SolrServerController
{
	/**
	 * Starts the Solr server for a given instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            - if an error occurs while starting the Solr server
	 */
	void start(final SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Stops the Solr server for a given instance.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            - if an error occurs while stopping the Solr server
	 */
	void stop(final SolrInstance solrInstance) throws SolrServerException;

	/**
	 * Uploads the Solr server configuration.
	 *
	 * @param solrInstance
	 *           - the Solr instance
	 *
	 * @throws SolrServerException
	 *            - if an error occurs while uploading the configuration
	 */
	void uploadSolrConfig(SolrInstance solrInstance) throws SolrServerException;
}
