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
package de.hybris.platform.solrserver.strategies;

import de.hybris.platform.solrserver.SolrServerException;


/**
 * Strategy for starting / stopping the Solr servers during hybris startup / shutdown. Only the Solr instances with
 * autostart=true are considered.
 */
public interface SolrServerController
{
	/**
	 * Starts the Solr servers.
	 *
	 * @throws SolrServerException
	 *            if an error occurs
	 */
	void startServers() throws SolrServerException;

	/**
	 * Stops the Solr servers.
	 *
	 * @throws SolrServerException
	 *            if an error occurs
	 */
	void stopServers() throws SolrServerException;
}
