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
package de.hybris.platform.solrserver.ant;

import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_CONFIG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_DATA_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_LOG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.INSTANCE_PREFIX;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.SOLRSERVER_CONFIGURATION_PREFIX;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.SOLR_SERVER_PATH_PROPERTY;

import de.hybris.platform.solrserver.SolrServerException;
import de.hybris.platform.solrserver.constants.SolrserverConstants;
import de.hybris.platform.solrserver.strategies.SolrServerConfigurationProvider;
import de.hybris.platform.solrserver.strategies.impl.DefaultSolrServerCommandExecutor;
import de.hybris.platform.solrserver.util.VersionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;


/**
 * Ant Task that allows execution of commands on a Solr server.
 */
public class SolrServerCommandTask extends org.apache.tools.ant.Task
{
	private String command;

	public String getCommand()
	{
		return command;
	}

	public void setCommand(final String command)
	{
		this.command = command;
	}

	@Override
	public void execute()
	{
		if (command == null || command.isEmpty())
		{
			throw new BuildException("Unknown command");
		}

		final Map<String, String> configuration = new HashMap<>();

		for (final Entry<String, Object> entry : getProject().getProperties().entrySet())
		{
			if ((entry.getKey().startsWith(SOLRSERVER_CONFIGURATION_PREFIX) || entry.getKey().startsWith(INSTANCE_PREFIX))
					&& entry.getValue() instanceof String)
			{
				configuration.put(entry.getKey(), (String) entry.getValue());
			}
		}

		configuration.put(HYBRIS_CONFIG_PATH_PROPERTY, getProject().getProperty("HYBRIS_CONFIG_DIR"));
		configuration.put(HYBRIS_DATA_PATH_PROPERTY, getProject().getProperty("HYBRIS_DATA_DIR"));
		configuration.put(HYBRIS_LOG_PATH_PROPERTY, getProject().getProperty("HYBRIS_LOG_DIR"));

		final String extDir = getProject().getProperty("ext.solrserver.path");
		final String solrServerVersion = configuration.get(SolrserverConstants.SOLR_SERVER_VERSION_PROPERTY);
		final String versionPath = VersionUtils.getVersionPath(solrServerVersion);
		final Path solrServerPath = Paths.get(extDir, "resources", "solr", versionPath, "server");

		configuration.put(SOLR_SERVER_PATH_PROPERTY, solrServerPath.toString());

		final SolrServerConfigurationProvider solrServerConfigurationProvider = new SolrServerConfigurationProvider()
		{
			@Override
			public Map<String, String> getConfiguration()
			{
				return configuration;
			}
		};

		final DefaultSolrServerCommandExecutor solrServerCommandExecutor = new DefaultSolrServerCommandExecutor();

		try
		{
			solrServerCommandExecutor.executeCommand(command, solrServerConfigurationProvider.getConfiguration());
		}
		catch (final SolrServerException e)
		{
			throw new BuildException(e);
		}
	}
}
