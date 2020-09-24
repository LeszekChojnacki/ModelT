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
import de.hybris.platform.solrserver.constants.SolrserverConstants;
import de.hybris.platform.solrserver.strategies.SolrServerCommand;
import de.hybris.platform.solrserver.strategies.SolrServerCommandExecutor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;


/**
 * Default implementation of {@link SolrServerCommandExecutor}.
 */
public class DefaultSolrServerCommandExecutor implements SolrServerCommandExecutor
{
	private Map<String, SolrServerCommand> internalCommands;

	public Map<String, SolrServerCommand> getInternalCommands()
	{
		return internalCommands;
	}

	public void setInternalCommands(final Map<String, SolrServerCommand> internalCommands)
	{
		this.internalCommands = internalCommands;
	}

	@Override
	public void executeCommand(final String command, final Map<String, String> configuration) throws SolrServerException
	{
		if (command == null || command.isEmpty() || command.contains("."))
		{
			throw new SolrServerException(MessageFormat.format("Invalid command name for command ''''", command));
		}

		final SolrServerCommand internalCommand = internalCommands != null ? internalCommands.get(command) : null;

		if (internalCommand != null)
		{
			internalCommand.execute(configuration);
		}
		else
		{
			executeExternalCommand(command, configuration);
		}
	}

	protected void executeExternalCommand(final String command, final Map<String, String> configuration) throws SolrServerException
	{
		URLClassLoader classLoader = null;

		try
		{
			final String solrServerPath = configuration.get(SolrserverConstants.SOLR_SERVER_PATH_PROPERTY);
			final Path libsPath = Paths.get(solrServerPath, "contrib", "hybris", "controller-libs");

			final File[] libs = libsPath.toFile().listFiles(file -> file.getName().toLowerCase(Locale.ROOT).endsWith(".jar"));
			final URL[] urls = new URL[libs.length];

			for (int index = 0; index < libs.length; index++)
			{
				urls[index] = libs[index].toURI().toURL();
			}

			classLoader = new URLClassLoader(urls, getClass().getClassLoader());

			final String commandClassName = buildExternalCommandClassName(command);
			final Class<?> commandClass = classLoader.loadClass(commandClassName);

			final Function<Map<String, String>, Integer> solrCommand = (Function<Map<String, String>, Integer>) commandClass
					.newInstance();
			solrCommand.apply(configuration);
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | MalformedURLException e)
		{
			throw new SolrServerException(e);
		}
		finally
		{
			if (classLoader != null)
			{
				IOUtils.closeQuietly(classLoader);
			}
		}
	}

	protected String buildExternalCommandClassName(final String command)
	{
		return "de.hybris.platform.solr.controller.commands." + command.substring(0, 1).toUpperCase(Locale.ROOT)
				+ command.substring(1) + "Command";
	}
}
