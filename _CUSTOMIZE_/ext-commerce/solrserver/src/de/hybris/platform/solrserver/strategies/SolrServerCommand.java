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
 * Solr server commands should implement this interface.
 */
@FunctionalInterface
public interface SolrServerCommand
{
	/**
	 * The command execution logic.
	 *
	 * @param configuration
	 *           - the configuration
	 *
	 * @throws SolrServerException
	 *            if an error occurs
	 */
	void execute(Map<String, String> configuration) throws SolrServerException;
}
