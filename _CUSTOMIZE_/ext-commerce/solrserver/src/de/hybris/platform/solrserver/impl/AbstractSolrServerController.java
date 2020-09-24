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

import de.hybris.platform.solrserver.SolrInstance;
import de.hybris.platform.solrserver.SolrServerController;
import de.hybris.platform.solrserver.SolrServerException;
import de.hybris.platform.solrserver.SolrServerMode;
import de.hybris.platform.solrserver.constants.SolrserverConstants;
import de.hybris.platform.solrserver.impl.AbstractSolrServerController.ServerStatus.Status;
import de.hybris.platform.solrserver.strategies.SolrServerConfigurationProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public abstract class AbstractSolrServerController implements SolrServerController
{
	private static final Logger LOG = Logger.getLogger(AbstractSolrServerController.class.getName());

	public static final String RUNNING_CHECK_REGEX = "Solr process \\d+ running on port (\\d+)";
	public static final String JSON_REGEX = ".*?(\\{.*\\}).*?";

	public static final String SOLR_STATUS_COMMAND = "status";
	public static final String SOLR_START_COMMAND = "start";
	public static final String SOLR_STOP_COMMAND = "stop";
	public static final String SOLR_ZK_CP_COMMAND = "zk cp";
	public static final String SOLR_ZK_UPCONFIG_COMMAND = "zk upconfig";

	public static final String ZK_CLUSTERPROP_COMMAND = "clusterprop";

	public static final String FORCE_RESTART = EXTENSIONNAME + ".forceRestart";
	public static final boolean FORCE_RESTART_DEFAULT_VALUE = true;

	public static final String TIMEOUT = EXTENSIONNAME + ".timeout";
	public static final long TIMEOUT_DEFAULT_VALUE = 20000;

	public static final String MAX_STATUS_RETRIES = EXTENSIONNAME + ".maxStatusRetries";
	public static final int MAX_STATUS_RETRIES_DEFAULT_VALUE = 10;

	public static final String STATUS_INTERVAL = EXTENSIONNAME + ".statusInterval";
	public static final long STATUS_INTERVAL_DEFAULT_VALUE = 2000;

	public static final String CONFIGSETS_PATH = "configsets";
	public static final String CORES_PATH = "cores";
	public static final String ZK_DATA_PATH = "zoo_data";

	public static final String BASIC_AUTH_TYPE = "basic";

	private SolrServerConfigurationProvider solrServerConfigurationProvider;

	@Override
	public void start(final SolrInstance solrInstance) throws SolrServerException
	{
		try
		{
			final String message = "Solr server status unknown for instance " + solrInstance;

			final ServerStatus serverStatus = retryGetStatusUntilConditionIsTrue(solrInstance,
					solrServerStatus -> !solrServerStatus.getStatus().equals(Status.UNKNOWN), message, message);

			if (serverStatus.getStatus().equals(Status.STARTED))
			{
				if (!isCorrespondingServerForInstance(solrInstance, serverStatus))
				{
					throw new SolrServerException(
							"Detected different Solr server running on the same port for instance " + solrInstance);
				}

				LOG.log(Level.INFO, "Solr server is already running for instance {0}", solrInstance);

				if (!isForceRestart())
				{
					return;
				}

				LOG.log(Level.INFO, "Restarting Solr server for instance {0}", solrInstance);
				ensureToStopSolr(solrInstance);
			}

			ensureToStartSolr(solrInstance);

			if (SolrServerMode.CLOUD.equals(solrInstance.getMode()) && solrInstance.isZkUpdateConfig())
			{
				uploadSolrConfig(solrInstance);

				LOG.log(Level.INFO,
						"Some cluster-wide properties need to be set before any Solr node starts up (e.g.: urlScheme), due to this we have to restart the Solr server");

				ensureToStopSolr(solrInstance);
				ensureToStartSolr(solrInstance);
			}
		}
		catch (final InterruptedException e)
		{
			throw new SolrServerException("The start of the Solr server was interrupted for instance " + solrInstance, e);
		}
	}

	protected void ensureToStartSolr(final SolrInstance solrInstance) throws SolrServerException, InterruptedException
	{
		final CommandResult commandResult = callSolrCommand(solrInstance, SOLR_START_COMMAND, this::buildCommonSolrCommandParams);
		if (commandResult.getExitValue() != 0)
		{
			throw new SolrServerException("Error while executing Solr start command for instance " + solrInstance);
		}

		final String retryMessage = "Solr server not yet started for instance " + solrInstance;
		final String errorMessage = "Solr server is still not running after calling start command for instance " + solrInstance;

		retryGetStatusUntilConditionIsTrue(solrInstance, serverStatus -> serverStatus.getStatus().equals(Status.STARTED),
				retryMessage, errorMessage);
	}

	@Override
	public void uploadSolrConfig(final SolrInstance solrInstance) throws SolrServerException
	{
		if (SolrServerMode.CLOUD.equals(solrInstance.getMode()))
		{
			try
			{
				final String zkHost = resolveZkHost(solrInstance);

				LOG.log(Level.INFO, "Uploading configuration to ZK host {0}", zkHost);

				uploadSolrCloudConfigFiles(solrInstance, zkHost);
				uploadSolrCloudConfigSets(solrInstance, zkHost);
				setSolrCloudConfigProperties(solrInstance, zkHost);
			}
			catch (final InterruptedException e)
			{
				throw new SolrServerException("The upload of the configuration was interrupted for instance " + solrInstance, e);
			}
		}
		else
		{
			LOG.log(Level.WARNING, "Uploading configuration failed. You are running solr server in standalone mode.");
		}
	}

	protected void uploadSolrCloudConfigFiles(final SolrInstance solrInstance, final String zkHost)
			throws SolrServerException, InterruptedException
	{
		// config files
		final List<String> configFiles = Arrays.asList("security.json");

		for (final String configFile : configFiles)
		{
			final Path configFilePath = Paths.get(solrInstance.getConfigDir(), configFile);

			final CommandResult result = callSolrCommand(solrInstance, SOLR_ZK_CP_COMMAND, (solrInst, processBuilder) -> {
				final List<String> commandParams = new ArrayList<>();
				commandParams.add("file:" + configFilePath.toString());
				commandParams.add("zk:" + configFile);
				commandParams.add("-z");
				commandParams.add(zkHost);

				processBuilder.command().addAll(commandParams);
			});

			if (result.getExitValue() != 0)
			{
				throw new SolrServerException("Failed to upload config file to ZK: file=" + configFile + ", zk=" + zkHost);
			}
		}
	}

	protected void uploadSolrCloudConfigSets(final SolrInstance solrInstance, final String zkHost)
			throws SolrServerException, InterruptedException
	{
		final File configSets = new File(Paths.get(solrInstance.getConfigDir(), CONFIGSETS_PATH).toString());

		for (final File configSet : configSets.listFiles())
		{
			final Path configSetPath = Paths.get(solrInstance.getConfigDir(), CONFIGSETS_PATH, configSet.getName(), "conf");

			final CommandResult result = callSolrCommand(solrInstance, SOLR_ZK_UPCONFIG_COMMAND, (solrInst, processBuilder) -> {
				final List<String> commandParams = new ArrayList<>();
				commandParams.add("-d");
				commandParams.add(configSetPath.toString());
				commandParams.add("-n");
				commandParams.add(configSet.getName());
				commandParams.add("-z");
				commandParams.add(zkHost);

				processBuilder.command().addAll(commandParams);
			});

			if (result.getExitValue() != 0)
			{
				throw new SolrServerException("Failed to upload config set to ZK: file=" + configSet.getName() + ", zk=" + zkHost);
			}
		}
	}

	protected void setSolrCloudConfigProperties(final SolrInstance solrInstance, final String zkHost)
			throws SolrServerException, InterruptedException
	{
		final Map<String, String> zkProperties = solrInstance.getZkProperties();
		if (zkProperties == null || zkProperties.isEmpty())
		{
			return;
		}

		for (final Entry<String, String> entry : zkProperties.entrySet())
		{
			final CommandResult result = callZKCommand(solrInstance, ZK_CLUSTERPROP_COMMAND, (solrInst, processBuilder) -> {
				final List<String> commandParams = new ArrayList<>();
				commandParams.add("-name");
				commandParams.add(entry.getKey());
				commandParams.add("-val");
				commandParams.add(entry.getValue());
				commandParams.add("-z");
				commandParams.add(zkHost);

				processBuilder.command().addAll(commandParams);
			});

			if (result.getExitValue() != 0)
			{
				throw new SolrServerException(
						"Failed to set ZK property: name=" + entry.getKey() + ", value=" + entry.getValue() + ", zk=" + zkHost);
			}
		}
	}

	@Override
	public void stop(final SolrInstance solrInstance) throws SolrServerException
	{
		try
		{
			final String message = "Solr server status unknown for instance " + solrInstance;

			final ServerStatus serverStatus = retryGetStatusUntilConditionIsTrue(solrInstance,
					solrServerStatus -> !solrServerStatus.getStatus().equals(Status.UNKNOWN), message, message);

			if (serverStatus.getStatus().equals(Status.STOPPED))
			{
				return;
			}

			ensureToStopSolr(solrInstance);
		}
		catch (final InterruptedException e)
		{
			throw new SolrServerException("The stop of the Solr server was interrupted for instance " + solrInstance, e);
		}
	}

	protected void ensureToStopSolr(final SolrInstance solrInstance) throws SolrServerException, InterruptedException
	{
		final CommandResult commandResult = callSolrCommand(solrInstance, SOLR_STOP_COMMAND, this::buildCommonSolrCommandParams);
		if (commandResult.getExitValue() != 0)
		{
			throw new SolrServerException("Error while executing Solr stop command for instance " + solrInstance);
		}

		final String retryMessage = "Solr server not yet stopped for instance " + solrInstance;
		final String errorMessage = "Solr server is still running after calling stop command for instance " + solrInstance;

		retryGetStatusUntilConditionIsTrue(solrInstance, serverStatus -> serverStatus.getStatus().equals(Status.STOPPED),
				retryMessage, errorMessage);
	}

	protected CommandResult callSolrCommand(final SolrInstance solrInstance, final String command,
			final CommandParamsBuilder commandParamsBuilder) throws SolrServerException, InterruptedException
	{
		LOG.log(Level.FINEST, "Running solr command '{0}' on Solr server for instance {1}", new Object[]
		{ command, solrInstance });

		return callCommand(solrInstance, command, this::configureSolrCommandInvocation, commandParamsBuilder);
	}

	protected CommandResult callZKCommand(final SolrInstance solrInstance, final String command,
			final CommandParamsBuilder commandParamsBuilder) throws SolrServerException, InterruptedException
	{
		LOG.log(Level.FINEST, "Running zk command '{0}' on Solr server for instance {1}", new Object[]
		{ command, solrInstance });

		return callCommand(solrInstance, command, this::configureZKCommandInvocation, commandParamsBuilder);
	}

	protected CommandResult callCommand(final SolrInstance solrInstance, final String command, final CommandBuilder commandBuilder,
			final CommandParamsBuilder commandParamsBuilder) throws SolrServerException, InterruptedException
	{
		final ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);

		commandBuilder.apply(solrInstance, processBuilder, command);
		commandParamsBuilder.apply(solrInstance, processBuilder);

		try (OutputStream outputStream = new ByteArrayOutputStream())
		{
			// workaround for windows machines, it was hanging while starting the Solr server
			if (!Objects.equals(SOLR_STATUS_COMMAND, command))
			{
				processBuilder.redirectOutput(Redirect.INHERIT);
			}

			final Process process = processBuilder.start();

			// workaround for windows machines, it was hanging while starting the Solr server
			if (Objects.equals(SOLR_STATUS_COMMAND, command))
			{
				final Thread streamCopyThread = new Thread(new StreamCopyRunnable(process.getInputStream(), outputStream));
				streamCopyThread.start();
				streamCopyThread.join();
			}

			process.waitFor();
			outputStream.flush();

			final String output = outputStream.toString();

			LOG.log(Level.FINEST, output);

			final CommandResult commandResult = new CommandResult();
			commandResult.setOutput(output);
			commandResult.setExitValue(process.exitValue());

			return commandResult;
		}
		catch (final IOException e)
		{
			throw new SolrServerException("Error while executing command '" + command + "' for instance " + solrInstance, e);
		}
	}

	protected void addCommand(final List<String> commandParams, final String command)
	{
		final String[] commandParts = command.split("\\s+");

		for (final String commandPart : commandParts)
		{
			commandParams.add(commandPart);
		}
	}

	protected abstract void configureSolrCommandInvocation(SolrInstance solrInstance, ProcessBuilder processBuilder,
			String command);

	protected abstract void configureZKCommandInvocation(SolrInstance solrInstance, ProcessBuilder processBuilder, String command);

	protected void buildCommonSolrCommandParams(final SolrInstance solrInstance, final ProcessBuilder processBuilder)
	{
		final List<String> commandParams = new ArrayList<>();

		if (SolrServerMode.CLOUD.equals(solrInstance.getMode()))
		{
			commandParams.add("-c");
			if (StringUtils.isNotBlank(solrInstance.getZkHost()))
			{
				commandParams.add("-z");
				commandParams.add(solrInstance.getZkHost());
			}
		}

		commandParams.add("-h");
		commandParams.add(solrInstance.getHostName());
		commandParams.add("-p");
		commandParams.add(Integer.toString(solrInstance.getPort()));
		commandParams.add("-m");
		commandParams.add(solrInstance.getMemory());
		commandParams.add("-DzkServerDataDir=" + Paths.get(solrInstance.getDataDir(), ZK_DATA_PATH).toString());
		commandParams.add("-a");
		commandParams.add(solrInstance.getJavaOptions());

		processBuilder.command().addAll(commandParams);

		// authentication related environment

		final String authType = solrInstance.getAuthType();
		final String user = solrInstance.getUser();
		final String password = solrInstance.getPassword();

		if (BASIC_AUTH_TYPE.equalsIgnoreCase(authType) && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password))
		{
			processBuilder.environment().put("SOLR_AUTH_TYPE", authType);
			processBuilder.environment().put("SOLR_AUTHENTICATION_OPTS", "-Dbasicauth=" + user + ":" + password);
		}
		else
		{
			processBuilder.environment().put("SOLR_AUTH_TYPE", StringUtils.EMPTY);
			processBuilder.environment().put("SOLR_AUTHENTICATION_OPTS", StringUtils.EMPTY);
		}

		// ssl related environment

		final boolean sslEnabled = solrInstance.isSSLEnabled();

		if (sslEnabled)
		{
			processBuilder.environment().put("SOLR_SSL_ENABLED", Boolean.TRUE.toString());
			processBuilder.environment().put("SOLR_SSL_KEY_STORE_TYPE", solrInstance.getSSLKeyStoreType());
			processBuilder.environment().put("SOLR_SSL_KEY_STORE", solrInstance.getSSLKeyStore());
			processBuilder.environment().put("SOLR_SSL_KEY_STORE_PASSWORD", solrInstance.getSSLKeyStorePassword());
			processBuilder.environment().put("SOLR_SSL_TRUST_STORE_TYPE", solrInstance.getSSLTrustStoreType());
			processBuilder.environment().put("SOLR_SSL_TRUST_STORE", solrInstance.getSSLTrustStore());
			processBuilder.environment().put("SOLR_SSL_TRUST_STORE_PASSWORD", solrInstance.getSSLTrustStorePassword());
			processBuilder.environment().put("SOLR_SSL_NEED_CLIENT_AUTH", Boolean.toString(solrInstance.isSSLNeedClientAuth()));
			processBuilder.environment().put("SOLR_SSL_WANT_CLIENT_AUTH", Boolean.toString(solrInstance.isSSLWantClientAuth()));
		}
		else
		{
			processBuilder.environment().put("SOLR_SSL_ENABLED", Boolean.FALSE.toString());
		}
	}

	protected String resolveZkHost(final SolrInstance solrInstance)
	{
		if (StringUtils.isNotBlank(solrInstance.getZkHost()))
		{
			return solrInstance.getZkHost();
		}

		final int port = solrInstance.getPort() + 1000;
		return "localhost:" + port;
	}

	protected ServerStatus getSolrServerStatus(final SolrInstance solrInstance) throws SolrServerException, InterruptedException
	{
		final ServerStatus serverStatus = new ServerStatus();
		serverStatus.setPort(solrInstance.getPort());

		final CommandResult commandResult = callSolrCommand(solrInstance, SOLR_STATUS_COMMAND, this::buildCommonSolrCommandParams);

		final Integer port = Integer.valueOf(solrInstance.getPort());
		final ServerStatusOutput serverStatusOutput = collectServerStatusOutput(commandResult.getOutput()).get(port);

		if (serverStatusOutput == null)
		{
			serverStatus.setStatus(Status.STOPPED);
		}
		else
		{
			serverStatus.setStatus(Status.UNKNOWN);

			final Pattern jsonPattern = Pattern.compile(JSON_REGEX, Pattern.DOTALL);
			final Matcher jsonMatcher = jsonPattern.matcher(serverStatusOutput.getOutput());

			if (jsonMatcher.find())
			{
				final String serverStatusJson = jsonMatcher.group(1);

				final int solrHomeStart = serverStatusJson.indexOf("\"solr_home\":\"");
				final int solrHomeEnd = serverStatusJson.indexOf("\",", solrHomeStart);
				final String solrHome = solrHomeStart == -1 || solrHomeEnd == -1 ? null
						: serverStatusJson.substring(solrHomeStart + 13, solrHomeEnd);

				final int versionStart = serverStatusJson.indexOf("\"version\":\"");
				final int versionEnd = serverStatusJson.indexOf("\",", versionStart);
				final String version = versionStart == -1 || versionEnd == -1 ? null
						: serverStatusJson.substring(versionStart + 11, versionEnd);

				if (solrHome != null && version != null)
				{
					serverStatus.setStatus(Status.STARTED);
					serverStatus.setSolrHome(solrHome);
					serverStatus.setVersion(version);
				}
			}
		}

		return serverStatus;
	}

	protected Map<Integer, ServerStatusOutput> collectServerStatusOutput(final String commandOutput)
	{
		final Map<Integer, ServerStatusOutput> serversStatusOutput = new LinkedHashMap<>();

		final Pattern pattern = Pattern.compile(RUNNING_CHECK_REGEX, Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(commandOutput);

		ServerStatusOutput previousStatusOutput = null;

		while (matcher.find())
		{
			final Integer port = Integer.valueOf(matcher.group(1));

			final ServerStatusOutput statusOutput = new ServerStatusOutput();
			statusOutput.setPort(port.intValue());
			statusOutput.setStartIndex(matcher.end());
			statusOutput.setEndIndex(commandOutput.length());

			serversStatusOutput.put(port, statusOutput);

			if (previousStatusOutput != null)
			{
				previousStatusOutput.setEndIndex(matcher.start());
			}

			previousStatusOutput = statusOutput;
		}

		for (final ServerStatusOutput statusOutput : serversStatusOutput.values())
		{
			final String output = commandOutput.substring(statusOutput.getStartIndex(), statusOutput.getEndIndex());
			statusOutput.setOutput(output);
		}

		return serversStatusOutput;
	}

	protected ServerStatus retryGetStatusUntilConditionIsTrue(final SolrInstance solrInstance,
			final ServerStatusFunction condition, final String retryMessage, final String errorMessage)
			throws SolrServerException, InterruptedException
	{
		final int maxRetries = getMaxStatusRetries();
		final long interval = getStatusInterval();

		for (int i = 0; i <= maxRetries; i++)
		{
			if (i != 0)
			{
				LOG.log(Level.INFO, "{0} [retry: {1}, interval: {2}ms]", new Object[]
				{ retryMessage, i, interval });
			}

			final ServerStatus serverStatus = getSolrServerStatus(solrInstance);
			final boolean result = condition.apply(serverStatus);
			if (result)
			{
				return serverStatus;
			}

			try
			{
				Thread.sleep(interval);
			}
			catch (final InterruptedException e)
			{
				throw new SolrServerException("Solr server get status was interrupted for instance " + solrInstance, e);
			}
		}

		LOG.log(Level.SEVERE, errorMessage);
		throw new SolrServerException(errorMessage);
	}

	protected boolean isCorrespondingServerForInstance(final SolrInstance solrInstance, final ServerStatus serverStatus)
			throws SolrServerException
	{
		try
		{
			final String expectedSolrHome = solrInstance.getConfigDir();
			final Path expectedSolrHomePath = Paths.get(expectedSolrHome);

			final String solrHome = serverStatus.getSolrHome();
			final Path solrHomePath = Paths.get(solrHome);

			return Files.isSameFile(expectedSolrHomePath, solrHomePath);
		}
		catch (final IOException e)
		{
			throw new SolrServerException("Failed to check running Solr server for instance " + solrInstance, e);
		}
	}

	protected boolean isForceRestart()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();

		final String forceRestart = configuration.get(FORCE_RESTART);
		if (forceRestart != null && !forceRestart.isEmpty())
		{
			return Boolean.parseBoolean(forceRestart);
		}

		return FORCE_RESTART_DEFAULT_VALUE;
	}

	protected long getTimeout()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();

		final String timeout = configuration.get(TIMEOUT);
		if (timeout != null && !timeout.isEmpty())
		{
			return Long.parseLong(timeout);
		}

		return TIMEOUT_DEFAULT_VALUE;
	}

	protected int getMaxStatusRetries()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();

		final String maxStatusRetries = configuration.get(MAX_STATUS_RETRIES);
		if (maxStatusRetries != null && !maxStatusRetries.isEmpty())
		{
			return Integer.parseInt(maxStatusRetries);
		}

		return MAX_STATUS_RETRIES_DEFAULT_VALUE;
	}

	protected long getStatusInterval()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();

		final String statusInterval = configuration.get(STATUS_INTERVAL);
		if (statusInterval != null && !statusInterval.isEmpty())
		{
			return Long.parseLong(statusInterval);
		}

		return STATUS_INTERVAL_DEFAULT_VALUE;
	}

	protected String getSolrServerPath()
	{
		final Map<String, String> configuration = solrServerConfigurationProvider.getConfiguration();
		return configuration.get(SolrserverConstants.SOLR_SERVER_PATH_PROPERTY);
	}

	public SolrServerConfigurationProvider getSolrServerConfigurationProvider()
	{
		return solrServerConfigurationProvider;
	}

	public void setSolrServerConfigurationProvider(final SolrServerConfigurationProvider solrServerConfigurationProvider)
	{
		this.solrServerConfigurationProvider = solrServerConfigurationProvider;
	}

	protected static class CommandResult
	{
		private String output;
		private int exitValue;

		public String getOutput()
		{
			return output;
		}

		public void setOutput(final String output)
		{
			this.output = output;
		}

		public int getExitValue()
		{
			return exitValue;
		}

		public void setExitValue(final int exitValue)
		{
			this.exitValue = exitValue;
		}
	}

	protected static class ServerStatusOutput
	{
		private int port;
		private int startIndex;
		private int endIndex;
		private String output;

		public int getPort()
		{
			return port;
		}

		public void setPort(final int port)
		{
			this.port = port;
		}

		public int getStartIndex()
		{
			return startIndex;
		}

		public void setStartIndex(final int startIndex)
		{
			this.startIndex = startIndex;
		}

		public int getEndIndex()
		{
			return endIndex;
		}

		public void setEndIndex(final int endIndex)
		{
			this.endIndex = endIndex;
		}

		public String getOutput()
		{
			return output;
		}

		public void setOutput(final String output)
		{
			this.output = output;
		}
	}

	protected static class ServerStatus
	{
		public enum Status
		{
			STARTED, UNKNOWN, STOPPED
		}

		private Status status;
		private int port;
		private String solrHome;
		private String version;

		public Status getStatus()
		{
			return status;
		}

		public void setStatus(final Status status)
		{
			this.status = status;
		}

		public int getPort()
		{
			return port;
		}

		public void setPort(final int port)
		{
			this.port = port;
		}

		public String getSolrHome()
		{
			return solrHome;
		}

		public void setSolrHome(final String solrHome)
		{
			this.solrHome = solrHome;
		}

		public String getVersion()
		{
			return version;
		}

		public void setVersion(final String version)
		{
			this.version = version;
		}
	}

	@FunctionalInterface
	protected interface ServerStatusFunction
	{
		boolean apply(ServerStatus serverStatus);
	}

	@FunctionalInterface
	protected interface CommandBuilder
	{
		void apply(final SolrInstance solrInstance, final ProcessBuilder processBuilder, String command);
	}

	@FunctionalInterface
	protected interface CommandParamsBuilder
	{
		void apply(final SolrInstance solrInstance, final ProcessBuilder processBuilder);
	}
}
