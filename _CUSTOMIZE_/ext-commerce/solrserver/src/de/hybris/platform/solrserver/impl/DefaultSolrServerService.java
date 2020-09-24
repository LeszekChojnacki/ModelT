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
package de.hybris.platform.solrserver.impl;

import static de.hybris.platform.solrserver.constants.SolrserverConstants.EXTENSIONNAME;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_CONFIG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_DATA_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.HYBRIS_LOG_PATH_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.INSTANCE_NAME_PROPERTY;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.INSTANCE_REGEX;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.SOLRSERVER_INSTANCES_REGEX;
import static de.hybris.platform.solrserver.constants.SolrserverConstants.SOLR_SERVER_PATH_PROPERTY;

import de.hybris.bootstrap.util.ConfigParameterHelper;
import de.hybris.platform.solrserver.SolrInstance;
import de.hybris.platform.solrserver.SolrInstanceNotFoundException;
import de.hybris.platform.solrserver.SolrServerController;
import de.hybris.platform.solrserver.SolrServerControllerFactory;
import de.hybris.platform.solrserver.SolrServerException;
import de.hybris.platform.solrserver.SolrServerService;
import de.hybris.platform.solrserver.strategies.SolrServerConfigurationProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Default implementation of {@link SolrServerService}.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class DefaultSolrServerService implements SolrServerService
{
	private static final Logger LOG = Logger.getLogger(DefaultSolrServerService.class.getName());

	public static final String SOLR_INSTANCES_PATH = "/solr/instances";

	public static final String FAIL_ON_ERROR = EXTENSIONNAME + ".failOnError";
	public static final boolean FAIL_ON_ERROR_DEFAULT_VALUE = true;

	private SolrServerConfigurationProvider solrServerConfigurationProvider;
	private SolrServerControllerFactory solrServerControllerFactory;

	private final Map<String, SolrInstance> instances = new HashMap<>();

	@Override
	public void init() throws SolrServerException
	{
		initializeInstances();
		startServers();
	}

	@Override
	public void destroy() throws SolrServerException
	{
		stopServers();
	}

	@Override
	public Map<String, SolrInstance> getInstances()
	{
		return instances;
	}

	@Override
	public SolrInstance getInstanceForName(final String instanceName) throws SolrInstanceNotFoundException
	{
		final SolrInstance solrInstance = instances.get(instanceName);
		if (solrInstance == null)
		{
			throw new SolrInstanceNotFoundException("Solr instance '" + instanceName + "' does not exist!");
		}

		return solrInstance;
	}

	@Override
	public void createInstance(final SolrInstance solrInstance) throws SolrServerException
	{
		try
		{
			createInstanceConfigDirectory(solrInstance);
			createInstanceDataDirectory(solrInstance);
			createInstanceLogDirectory(solrInstance);
		}
		catch (final IOException e)
		{
			throw new SolrServerException("Failed to create Solr instance", e);
		}
	}

	protected void createInstanceConfigDirectory(final SolrInstance solrInstance) throws IOException
	{
		final Path instanceConfigDirectory = Paths.get(solrInstance.getConfigDir());
		if (!Files.exists(instanceConfigDirectory))
		{
			Files.createDirectories(instanceConfigDirectory);

			final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
			final String solrServerPath = configuration.get(SOLR_SERVER_PATH_PROPERTY);

			// config files
			final List<String> configFiles = Arrays.asList("/server/resources/log4j.properties",
					"/server/solr/security.json.example", "/server/solr/solr.jks", "/server/solr/solr.xml", "/server/solr/zoo.cfg");

			for (final String configFile : configFiles)
			{
				final Path sourceConfigFilePath = Paths.get(solrServerPath, configFile);
				final Path configFilePath = instanceConfigDirectory
						.resolve(Paths.get(StringUtils.removeEnd(configFile, ".example")).getFileName());
				Files.copy(sourceConfigFilePath, configFilePath);
			}

			// config sets
			final Path sourceConfigSetsDirectory = Paths.get(solrServerPath, "/server/solr/configsets");
			final Path configSetsDirectory = instanceConfigDirectory.resolve("configsets");
			FileUtils.copyDirectory(sourceConfigSetsDirectory.toFile(), configSetsDirectory.toFile());
		}
	}

	protected void createInstanceDataDirectory(final SolrInstance solrInstance) throws IOException
	{
		final Path instanceDataDirectory = Paths.get(solrInstance.getDataDir());
		Files.createDirectories(instanceDataDirectory);
	}

	protected void createInstanceLogDirectory(final SolrInstance solrInstance) throws IOException
	{
		final Path instanceLogDirectory = Paths.get(solrInstance.getLogDir());
		Files.createDirectories(instanceLogDirectory);
	}

	public void deleteInstance(final SolrInstance solrInstance) throws SolrServerException
	{
		try
		{
			final Path instanceConfigDirectory = Paths.get(solrInstance.getConfigDir());
			final Path instanceDataDirectory = Paths.get(solrInstance.getDataDir());
			final Path instanceLogDirectory = Paths.get(solrInstance.getLogDir());

			FileUtils.deleteDirectory(instanceConfigDirectory.toFile());
			FileUtils.deleteDirectory(instanceDataDirectory.toFile());
			FileUtils.deleteDirectory(instanceLogDirectory.toFile());
		}
		catch (final IOException e)
		{
			throw new SolrServerException("Failed to delete Solr instance", e);
		}
	}

	@Override
	public void startServer(final SolrInstance solrInstance) throws SolrServerException
	{
		createInstance(solrInstance);

		LOG.log(Level.INFO, "Starting Solr server for instance {0}", solrInstance);

		final SolrServerController controller = solrServerControllerFactory.getController();
		controller.start(solrInstance);
	}

	@Override
	public void stopServer(final SolrInstance solrInstance) throws SolrServerException
	{
		LOG.log(Level.INFO, "Stopping Solr server for instance {0}", solrInstance);

		final SolrServerController controller = solrServerControllerFactory.getController();
		controller.stop(solrInstance);
	}

	@Override
	public void startServers() throws SolrServerException
	{
		LOG.info("Starting Solr servers ...");

		final List<SolrInstance> startedSolrInstances = new ArrayList<>();

		for (final SolrInstance solrInstance : getInstances().values())
		{
			try
			{
				if (solrInstance.isAutostart())
				{
					startServer(solrInstance);
					startedSolrInstances.add(solrInstance);
				}
			}
			catch (final SolrServerException e)
			{
				LOG.log(Level.SEVERE, e, e::getMessage);

				final boolean failOnError = isFailOnError();
				if (failOnError)
				{
					stopServers(startedSolrInstances);
					throw e;
				}
			}
		}
	}

	protected void stopServers(final List<SolrInstance> solrInstances) throws SolrServerException
	{
		for (final SolrInstance solrInstance : solrInstances)
		{
			try
			{
				stopServer(solrInstance);
			}
			catch (final SolrServerException e)
			{
				// should not abort execution if one server fails to stop
				LOG.log(Level.SEVERE, e, e::getMessage);
			}
		}
	}

	@Override
	public void stopServers() throws SolrServerException
	{
		LOG.info("Stopping Solr servers ...");

		for (final SolrInstance solrInstance : getInstances().values())
		{
			try
			{
				if (solrInstance.isAutostart())
				{
					stopServer(solrInstance);
				}
			}
			catch (final SolrServerException e)
			{
				// should not abort execution if one server fails to stop
				LOG.log(Level.SEVERE, e, e::getMessage);
			}
		}
	}

	@Override
	public void uploadConfig(final SolrInstance solrInstance) throws SolrServerException
	{
		createInstance(solrInstance);

		LOG.log(Level.INFO, "Uploading Solr server configuration for instance {0}", solrInstance);

		final SolrServerController controller = solrServerControllerFactory.getController();
		controller.uploadSolrConfig(solrInstance);
	}

	@Override
	public void initializeInstances()
	{
		final Map<String, String> instancesConfiguration = buildInstancesConfigurations();

		instancesConfiguration.forEach((key, value) -> {
			final int instanceNameEndIndex = key.indexOf('.');

			final String instanceName = key.substring(0, instanceNameEndIndex);
			final String instanceConfigKey = key.substring(instanceNameEndIndex + 1);

			SolrInstance instance = instances.get(instanceName);
			if (instance == null)
			{
				instance = initializeInstance(instanceName);
				instances.put(instanceName, instance);
			}

			instance.getConfiguration().put(instanceConfigKey, value);
		});
	}

	protected Map<String, String> buildInstancesConfigurations()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
		final Map<String, String> instancesConfiguration = ConfigParameterHelper.getParametersMatching(configuration,
				SOLRSERVER_INSTANCES_REGEX, true);

		final String instanceName = configuration.get(INSTANCE_NAME_PROPERTY);
		if (instanceName != null)
		{
			final Map<String, String> instanceConfiguration = ConfigParameterHelper.getParametersMatching(configuration,
					INSTANCE_REGEX, true);

			instanceConfiguration.forEach((key, value) -> instancesConfiguration.put(instanceName + "." + key, value));
		}

		return instancesConfiguration;
	}

	protected SolrInstance initializeInstance(final String instanceName)
	{
		final SolrInstance instance = new DefaultSolrInstance(instanceName);
		final Map<String, String> instanceConfiguration = instance.getConfiguration();

		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
		final Path configPath = Paths.get(configuration.get(HYBRIS_CONFIG_PATH_PROPERTY), SOLR_INSTANCES_PATH, instanceName);
		final Path dataPath = Paths.get(configuration.get(HYBRIS_DATA_PATH_PROPERTY), SOLR_INSTANCES_PATH, instanceName);
		final Path logPath = Paths.get(configuration.get(HYBRIS_LOG_PATH_PROPERTY), SOLR_INSTANCES_PATH, instanceName);
		final Path sslStorePath = configPath.resolve("solr.jks");

		instanceConfiguration.put(DefaultSolrInstance.CONFIG_DIR_PROPERTY, configPath.toString());
		instanceConfiguration.put(DefaultSolrInstance.DATA_DIR_PROPERTY, dataPath.toString());
		instanceConfiguration.put(DefaultSolrInstance.LOG_DIR_PROPERTY, logPath.toString());

		instanceConfiguration.put(DefaultSolrInstance.SSL_KEY_STORE, sslStorePath.toString());
		instanceConfiguration.put(DefaultSolrInstance.SSL_TRUST_STORE, sslStorePath.toString());

		return instance;
	}

	protected boolean isFailOnError()
	{
		final Map<String, String> failOnError = solrServerConfigurationProvider.getConfiguration();

		final String forceRestart = failOnError.get(FAIL_ON_ERROR);
		if (forceRestart != null && !forceRestart.isEmpty())
		{
			return Boolean.parseBoolean(forceRestart);
		}

		return FAIL_ON_ERROR_DEFAULT_VALUE;
	}

	public SolrServerConfigurationProvider getSolrServerConfigurationProvider()
	{
		return solrServerConfigurationProvider;
	}

	public void setSolrServerConfigurationProvider(final SolrServerConfigurationProvider solrServerConfigurationProvider)
	{
		this.solrServerConfigurationProvider = solrServerConfigurationProvider;
	}

	public SolrServerControllerFactory getSolrServerControllerFactory()
	{
		return solrServerControllerFactory;
	}

	public void setSolrServerControllerFactory(final SolrServerControllerFactory solrServerControllerFactory)
	{
		this.solrServerControllerFactory = solrServerControllerFactory;
	}
}
