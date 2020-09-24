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
package de.hybris.platform.solrfacetsearch.indexer.workers.impl;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.suspend.SystemIsSuspendedException;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RegistrableThread;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerRuntimeException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerBatchStrategyFactory;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorker;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerParameters;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link IndexerWorker}.
 */
public class DefaultIndexerWorker implements IndexerWorker
{
	private static final Logger LOG = Logger.getLogger(DefaultIndexerWorker.class);

	// dependencies
	private SessionService sessionService;
	private UserService userService;
	private CommonI18NService commonI18NService;
	private FacetSearchConfigService facetSearchConfigService;
	private IndexerBatchStrategyFactory indexerBatchStrategyFactory;
	private SolrSearchProviderFactory solrSearchProviderFactory;

	// state
	private IndexerWorkerParameters workerParameters;

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public FacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	public IndexerBatchStrategyFactory getIndexerBatchStrategyFactory()
	{
		return indexerBatchStrategyFactory;
	}

	@Required
	public void setIndexerBatchStrategyFactory(final IndexerBatchStrategyFactory indexerBatchStrategyFactory)
	{
		this.indexerBatchStrategyFactory = indexerBatchStrategyFactory;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	@Override
	public void initialize(final IndexerWorkerParameters workerParameters)
	{
		ServicesUtil.validateParameterNotNull(workerParameters, "workerParameters must not be null");
		this.workerParameters = workerParameters;
	}

	@Override
	public boolean isInitialized()
	{
		return workerParameters != null;
	}

	@Override
	public void run()
	{
		if (!isInitialized())
		{
			throw new IllegalStateException("Indexer worker was not initialized");
		}

		try
		{
			registerNonSuspendableThread();

			initializeSession();
			logWorkerStart();

			doRun();

			logWorkerSuccess();
		}
		catch (final IndexerException | FacetConfigServiceException | SolrServiceException e)
		{
			logWorkerError(e);
			throw new IndexerRuntimeException(e);
		}
		catch (final InterruptedException e)
		{
			logWorkerInterrupted();
			Thread.currentThread().interrupt();
		}
		finally
		{
			RegistrableThread.unregisterThread();
			destroySession();
		}
	}

	protected void doRun() throws IndexerException, FacetConfigServiceException, SolrServiceException, InterruptedException
	{
		final FacetSearchConfig facetSearchConfig = facetSearchConfigService
				.getConfiguration(workerParameters.getFacetSearchConfig());
		final IndexedType indexedType = facetSearchConfigService.resolveIndexedType(facetSearchConfig,
				workerParameters.getIndexedType());
		final List<IndexedProperty> indexedProperties = facetSearchConfigService.resolveIndexedProperties(facetSearchConfig,
				indexedType, workerParameters.getIndexedProperties());
		final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig, indexedType);
		final Index index = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, workerParameters.getIndex());

		final IndexerBatchStrategy indexerBatchStrategy = indexerBatchStrategyFactory.createIndexerBatchStrategy(facetSearchConfig);

		indexerBatchStrategy.setIndexOperationId(workerParameters.getIndexOperationId());
		indexerBatchStrategy.setIndexOperation(workerParameters.getIndexOperation());
		indexerBatchStrategy.setExternalIndexOperation(workerParameters.isExternalIndexOperation());
		indexerBatchStrategy.setFacetSearchConfig(facetSearchConfig);
		indexerBatchStrategy.setIndexedType(indexedType);
		indexerBatchStrategy.setIndexedProperties(indexedProperties);
		indexerBatchStrategy.setIndex(index);
		indexerBatchStrategy.setIndexerHints(workerParameters.getIndexerHints());
		indexerBatchStrategy.setPks(workerParameters.getPks());

		indexerBatchStrategy.execute();
	}

	protected void initializeSession()
	{
		final Tenant tenant = Registry.getTenantByID(workerParameters.getTenant());
		Registry.setCurrentTenant(tenant);

		sessionService.createNewSession();

		final UserModel user = userService.getUserForUID(workerParameters.getSessionUser());
		userService.setCurrentUser(user);

		final LanguageModel language = commonI18NService.getLanguage(workerParameters.getSessionLanguage());
		commonI18NService.setCurrentLanguage(language);

		final CurrencyModel currency = commonI18NService.getCurrency(workerParameters.getSessionCurrency());
		commonI18NService.setCurrentCurrency(currency);
	}

	protected void destroySession()
	{
		sessionService.closeCurrentSession();
		Registry.unsetCurrentTenant();
	}

	protected void registerNonSuspendableThread() throws InterruptedException
	{
		final OperationInfo operationInfo = OperationInfo.builder().withTenant(workerParameters.getTenant())
				.withStatusInfo("Starting indexer worker " + workerParameters.getWorkerNumber()).asNotSuspendableOperation().build();

		do
		{
			try
			{
				RegistrableThread.registerThread(operationInfo);
				return;
			}
			catch (final SystemIsSuspendedException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(
							getTaskName()
									+ " - System is suspended. The worker cannot be registered as non suspendable at the moment. Retrying in 5 sec...",
							e);
				}

				Thread.sleep(5000);
			}
		}
		while (true);
	}

	protected String getTaskName()
	{
		return "Indexer worker " + workerParameters.getWorkerNumber() + " (" + workerParameters.getIndexOperation()
				+ " index operation on " + workerParameters.getFacetSearchConfig() + "/" + workerParameters.getIndexedType() + ")";
	}

	protected void logWorkerStart()
	{
		if (LOG.isDebugEnabled())
		{
			final String taskName = getTaskName();
			LOG.debug("[" + taskName + "] started");
			LOG.debug("[" + taskName + "] tenant:" + Registry.getCurrentTenant());
			LOG.debug("[" + taskName + "] session ID: " + sessionService.getCurrentSession().getSessionId());
			LOG.debug("[" + taskName + "] session user: " + userService.getCurrentUser().getUid());
			LOG.debug("[" + taskName + "] session language: " + commonI18NService.getCurrentLanguage().getIsocode());
			LOG.debug("[" + taskName + "] session currency: " + commonI18NService.getCurrentCurrency().getIsocode());
			LOG.debug("[" + taskName + "] items count: " + workerParameters.getPks().size());
		}
	}

	protected void logWorkerSuccess()
	{
		if (LOG.isDebugEnabled())
		{
			final String taskName = getTaskName();
			LOG.debug("[" + taskName + "] completed");
		}
	}

	protected void logWorkerError(final Exception exception)
	{
		final String taskName = getTaskName();
		LOG.error("[" + taskName + "] failed to process index items due to " + exception.getMessage(), exception);
	}

	protected void logWorkerInterrupted()
	{
		if (LOG.isDebugEnabled())
		{
			final String taskName = getTaskName();
			LOG.debug("[" + taskName + "] interrupted");
		}
	}
}
