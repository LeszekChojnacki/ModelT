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
package de.hybris.platform.solrserver.strategies.impl;

import de.hybris.platform.solrserver.SolrServerException;
import de.hybris.platform.solrserver.strategies.SolrServerCommandExecutor;
import de.hybris.platform.solrserver.strategies.SolrServerConfigurationProvider;
import de.hybris.platform.solrserver.strategies.SolrServerController;

import java.util.Map;


/**
 * Default implementation of {@link SolrServerController}.
 */
public class DefaultSolrServerController implements SolrServerController
{
	protected static final String START_SERVERS_COMMAND = "startSolrServers";
	protected static final String STOP_SERVERS_COMMAND = "stopSolrServers";

	private SolrServerConfigurationProvider solrServerConfigurationProvider;
	private SolrServerCommandExecutor solrServerCommandExecutor;

	@Override
	public void startServers() throws SolrServerException
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
		solrServerCommandExecutor.executeCommand(START_SERVERS_COMMAND, configuration);
	}

	@Override
	public void stopServers() throws SolrServerException
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
		solrServerCommandExecutor.executeCommand(STOP_SERVERS_COMMAND, configuration);
	}

	public SolrServerConfigurationProvider getSolrServerConfigurationProvider()
	{
		return solrServerConfigurationProvider;
	}

	public void setSolrServerConfigurationProvider(final SolrServerConfigurationProvider solrServerConfigurationProvider)
	{
		this.solrServerConfigurationProvider = solrServerConfigurationProvider;
	}

	public SolrServerCommandExecutor getSolrServerCommandExecutor()
	{
		return solrServerCommandExecutor;
	}

	public void setSolrServerCommandExecutor(final SolrServerCommandExecutor solrServerCommandExecutor)
	{
		this.solrServerCommandExecutor = solrServerCommandExecutor;
	}
}
