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
package de.hybris.platform.solrfacetsearch.solr.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.TenantListener;
import de.hybris.platform.jalo.JaloConnection;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.tenant.TenantService;
import de.hybris.platform.solrfacetsearch.config.SolrClientConfig;
import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.daos.SolrServerConfigDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrClientPool;
import de.hybris.platform.solrfacetsearch.solr.SolrClientType;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.util.RedeployUtilities;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link SolrClientPool}.
 */
public class DefaultSolrClientPool implements SolrClientPool, InitializingBean
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSolrClientPool.class);

	protected static final String INDEX_PARAM = "index";
	protected static final String SOLR_CLIENT_PARAM = "solrClientType";
	protected static final String CREATE_METHOD_PARAM = "createMethod";

	protected static final String CHECK_INTERVAL_PROPERTY = "solrfacetsearch.solrClientPool.checkInterval";
	protected static final long CHECK_INTERVAL_DEFAULT_VALUE = 300000;

	protected static final String CHECK_THREAD_NAME_PREFIX = "solrclient-cleanup-";

	protected static final long SCHEDULER_TERMINATION_TIMEOUT = 10000;

	private ModelService modelService;
	private ConfigurationService configurationService;
	private TenantService tenantService;
	private SessionService sessionService;
	private SolrServerConfigDao solrServerConfigDao;
	private Converter<SolrServerConfigModel, SolrConfig> solrServerConfigConverter;

	private String tenantId;
	private ConcurrentHashMap<String, SolrClientsWrapper> solrClients;
	private ScheduledExecutorService scheduler;

	protected String getTenantId()
	{
		return tenantId;
	}

	protected ScheduledExecutorService getScheduler()
	{
		return scheduler;
	}

	protected ConcurrentMap<String, SolrClientsWrapper> getSolrClients()
	{
		return solrClients;
	}

	@Override
	public void afterPropertiesSet()
	{
		tenantId = tenantService.getCurrentTenantId();
		solrClients = new ConcurrentHashMap<>();

		Registry.registerTenantListener(createTenantListener());
	}

	@Override
	public CachedSolrClient getOrCreate(final Index index, final SolrClientType clientType,
			final Function<SolrConfig, SolrClient> createMethod) throws SolrServiceException
	{
		return getOrCreate(index, clientType, createMethod, null);
	}

	@Override
	public CachedSolrClient getOrCreate(final Index index, final SolrClientType clientType,
			final Function<SolrConfig, SolrClient> createMethod, final Consumer<SolrClient> closeMethod) throws SolrServiceException
	{
		validateParameterNotNullStandardMessage(INDEX_PARAM, index);
		validateParameterNotNullStandardMessage(SOLR_CLIENT_PARAM, clientType);
		validateParameterNotNullStandardMessage(CREATE_METHOD_PARAM, createMethod);

		final SolrConfig solrConfig = index.getFacetSearchConfig().getSolrConfig();

		final SolrClientsWrapper clientsWrapper = solrClients.compute(solrConfig.getName(), (solrConfigName,
				solrClientsWrapper) -> doGetOrCreate(solrConfig, solrClientsWrapper, clientType, createMethod, closeMethod));

		return resolveSolrClient(clientsWrapper, clientType);
	}

	@Override
	public void invalidateAll()
	{
		LOG.debug("Invalidating pooled Solr clients ...");

		for (final String key : solrClients.keySet())
		{
			solrClients.computeIfPresent(key, (solrConfigName, clientsWrapper) -> {
				clientsWrapper.close();
				return null;
			});
		}
	}

	protected CachedSolrClient resolveSolrClient(final SolrClientsWrapper clientsWrapper, final SolrClientType solrClientType)
	{
		return solrClientType == SolrClientType.INDEXING ? clientsWrapper.getIndexClient() : clientsWrapper.getSearchClient();
	}

	protected SolrClientsWrapper doGetOrCreate(final SolrConfig solrConfig, final SolrClientsWrapper clientsWrapper,
			final SolrClientType clientType, final Function<SolrConfig, SolrClient> createMethod,
			final Consumer<SolrClient> closeMethod)
	{
		// compares with the version from the cached configuration
		if (clientsWrapper != null && Objects.equals(clientsWrapper.getConfigVersion(), solrConfig.getVersion()))
		{
			final CachedSolrClient solrClient = resolveSolrClient(clientsWrapper, clientType);
			if (solrClient != null)
			{
				solrClient.addConsumer();
				return clientsWrapper;
			}
		}

		final SolrServerConfigModel solrServerConfig = loadSolrServerConfig(solrConfig.getName());
		final SolrConfig newSolrConfig = solrServerConfigConverter.convert(solrServerConfig);
		final SolrClientsWrapper newClientsWrapper;

		// compares with the most up to date version
		if (clientsWrapper != null && Objects.equals(clientsWrapper.getConfigVersion(), newSolrConfig.getVersion()))
		{
			newClientsWrapper = clientsWrapper;
			final CachedSolrClient solrClient = resolveSolrClient(clientsWrapper, clientType);
			if (solrClient != null)
			{
				solrClient.addConsumer();
				return clientsWrapper;
			}
		}
		else
		{
			if (clientsWrapper != null)
			{
				LOG.info("New Solr config detected [config={}, newVersion={}, oldVersion={}]", solrConfig.getName(),
						newSolrConfig.getVersion(), clientsWrapper.getConfigVersion());
				clientsWrapper.close();
			}

			newClientsWrapper = new SolrClientsWrapper(newSolrConfig);
		}

		final CachedSolrClient newSolrClient;

		if (clientType == SolrClientType.INDEXING)
		{
			newSolrClient = createSolrClient(newSolrConfig, newSolrConfig.getIndexingClientConfig(), createMethod, closeMethod);
			newClientsWrapper.setIndexClient(newSolrClient);
		}
		else
		{
			newSolrClient = createSolrClient(newSolrConfig, newSolrConfig.getClientConfig(), createMethod, closeMethod);
			newClientsWrapper.setSearchClient(newSolrClient);
		}

		newSolrClient.addConsumer();
		return newClientsWrapper;
	}

	protected CachedSolrClient createSolrClient(final SolrConfig solrConfig, final SolrClientConfig solrClientConfig,
			final Function<SolrConfig, SolrClient> createMethod, final Consumer<SolrClient> closeMethod)
	{
		final SolrClient solrClient = createMethod.apply(solrConfig);

		Credentials credentials = null;
		if (StringUtils.isNotBlank(solrClientConfig.getUsername()) && StringUtils.isNotBlank(solrClientConfig.getPassword()))
		{
			credentials = new UsernamePasswordCredentials(solrClientConfig.getUsername(), solrClientConfig.getPassword());
		}

		return new CachedSolrClient(solrClient, closeMethod, credentials);
	}

	protected TenantListener createTenantListener()
	{
		return new SolrClientPoolTenantListener();
	}

	protected SolrServerConfigModel loadSolrServerConfig(final String name)
	{
		final SolrServerConfigModel solrServerConfig = solrServerConfigDao.findSolrServerConfigByName(name);

		// makes sure we get the most recent version of the configuration from the database
		modelService.refresh(solrServerConfig);

		return solrServerConfig;
	}

	protected void startCleanUpThread()
	{
		if (!JaloConnection.getInstance().isSystemInitialized() || RedeployUtilities.isShutdownInProgress())
		{
			return;
		}

		final long checkInterval = configurationService.getConfiguration().getLong(CHECK_INTERVAL_PROPERTY,
				CHECK_INTERVAL_DEFAULT_VALUE);

		if (checkInterval <= 0)
		{
			return;
		}

		LOG.info("Starting Solr clients clean-up thread for tenant {}", tenantId);

		final ThreadFactory threadFactory = new ThreadFactory()
		{
			private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(final Runnable runnable)
			{
				final Thread thread = defaultFactory.newThread(runnable);
				thread.setName(CHECK_THREAD_NAME_PREFIX + tenantId);
				return thread;
			}
		};

		scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		scheduler.scheduleAtFixedRate(() -> {
			try
			{
				try
				{
					initializeSession();
					checkAll();
				}
				finally
				{
					destroySession();
				}

			}
			catch (final Exception e)
			{
				// swallow exceptions, otherwise the thread will be stopped
				LOG.warn(e.getMessage(), e);
			}
		}, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
	}

	protected void stopCleanUpThread()
	{
		if (scheduler != null)
		{
			LOG.info("Stopping Solr clients clean-up thread for tenant {}", tenantId);

			scheduler.shutdownNow();

			try
			{
				scheduler.awaitTermination(SCHEDULER_TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
				if (!scheduler.isShutdown())
				{
					LOG.warn("Failed to stop Solr clients clean-up thread for tenant {}", tenantId);
				}
			}
			catch (final InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	protected void initializeSession()
	{
		final Tenant tenant = Registry.getTenantByID(tenantId);
		Registry.setCurrentTenant(tenant);
		sessionService.createNewSession();
	}

	protected void checkAll()
	{
		LOG.debug("Checking pooled Solr clients ...");

		for (final String key : solrClients.keySet())
		{
			solrClients.computeIfPresent(key, this::check);
		}
	}

	protected SolrClientsWrapper check(final String solrServerConfigName, final SolrClientsWrapper clientsWrapper)
	{
		try
		{
			LOG.info("Checking pooled Solr client [config={}]", solrServerConfigName);

			final SolrServerConfigModel solrServerConfig = loadSolrServerConfig(solrServerConfigName);

			if (!StringUtils.equals(clientsWrapper.getConfigVersion(), solrServerConfig.getVersion()))
			{
				LOG.info("New Solr config detected [config={}, newVersion={}, oldVersion={}]", solrServerConfigName,
						solrServerConfig.getVersion(), clientsWrapper.getConfigVersion());

				clientsWrapper.close();
				return null;
			}
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.info("Solr config not found [config={}]", solrServerConfigName);

			clientsWrapper.close();
			return null;
		}
		catch (final Exception e)
		{
			LOG.error("Error checking Solr config [config={}, error={}]", solrServerConfigName, e);
		}

		return clientsWrapper;
	}

	protected void destroySession()
	{
		sessionService.closeCurrentSession();
		Registry.unsetCurrentTenant();
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public TenantService getTenantService()
	{
		return tenantService;
	}

	@Required
	public void setTenantService(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public SolrServerConfigDao getSolrServerConfigDao()
	{
		return solrServerConfigDao;
	}

	@Required
	public void setSolrServerConfigDao(final SolrServerConfigDao solrServerConfigDao)
	{
		this.solrServerConfigDao = solrServerConfigDao;
	}

	public Converter<SolrServerConfigModel, SolrConfig> getSolrServerConfigConverter()
	{
		return solrServerConfigConverter;
	}

	@Required
	public void setSolrServerConfigConverter(final Converter<SolrServerConfigModel, SolrConfig> solrServerConfigConverter)
	{
		this.solrServerConfigConverter = solrServerConfigConverter;
	}

	protected class SolrClientPoolTenantListener implements TenantListener
	{
		@Override
		public void afterTenantStartUp(final Tenant tenant)
		{
			if (Objects.equals(getTenantId(), tenant.getTenantID()))
			{
				startCleanUpThread();
			}
		}

		@Override
		public void beforeTenantShutDown(final Tenant tenant)
		{
			if (Objects.equals(tenantId, tenant.getTenantID()))
			{
				stopCleanUpThread();
				invalidateAll();
			}
		}

		@Override
		public void afterSetActivateSession(final Tenant tenant)
		{
			// NOOP
		}

		@Override
		public void beforeUnsetActivateSession(final Tenant tenant)
		{
			// NOOP
		}
	}
}
