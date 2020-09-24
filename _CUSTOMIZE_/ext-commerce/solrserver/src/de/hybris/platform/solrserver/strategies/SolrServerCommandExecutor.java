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

import java.util.Map;


/**
 * Strategy for executing the Solr server commands.
 */
public interface SolrServerCommandExecutor
{
	/**
	 * Executes a command.
	 *
	 * @param command
	 *           - the command
	 * @param configuration
	 *           - the configuration
	 *
	 * @throws SolrServerException
	 *            if an error occurs during shutdown
	 */
	void executeCommand(String command, Map<String, String> configuration) throws SolrServerException;
}
