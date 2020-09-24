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
package de.hybris.platform.solrfacetsearch.indexer.strategies.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RegistrableThread;
import de.hybris.platform.core.threadregistry.RevertibleUpdate;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.tenant.TenantService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FlexibleSearchQuerySpec;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;
import de.hybris.platform.solrfacetsearch.config.factories.FlexibleSearchQuerySpecFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueriesExecutor;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.UndefinedIndexerQuery;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexOperationIdGenerator;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;


/**
 * Base implementation for indexer strategies.
 */
public abstract class AbstractIndexerStrategy implements IndexerStrategy
{
	private static final Logger LOG = Logger.getLogger(AbstractIndexerStrategy.class);

	// dependencies
	private SessionService sessionService;
	private UserService userService;
	private CommonI18NService commonI18NService;
	private TenantService tenantService;

	private FlexibleSearchQuerySpecFactory flexibleSearchQuerySpecFactory;
	private IndexerQueriesExecutor indexerQueriesExecutor;
	private IndexOperationIdGenerator indexOperationIdGenerator;
	private IndexerContextFactory<?> indexerContextFactory;
	private SolrIndexService solrIndexService;
	private SolrSearchProviderFactory solrSearchProviderFactory;

	// state
	private IndexOperation indexOperation;
	private FacetSearchConfig facetSearchConfig;
	private IndexedType indexedType;
	private Collection<IndexedProperty> indexedProperties;
	private List<PK> pks;
	private Index index;
	private Map<String, String> indexerHints;

	@Override
	public void execute() throws IndexerException
	{
		validateRequiredFields();

		RevertibleUpdate revertibleInfo = null;

		final StopWatch operationTimer = new StopWatch();
		operationTimer.start();

		logStrategyStart();

		try
		{
			// make the thread non-suspendable
			revertibleInfo = registerOrUpdateNonSuspendableThread();

			createLocalSessionContext();

			final Index resolvedIndex = resolveIndex();

			if (resolvedIndex == null && indexOperation != IndexOperation.FULL)
			{
				LOG.info("No active index found, FULL indexer operation must be performed before any other operation");
				return;
			}

			final long indexOperationId = indexOperationIdGenerator.generate(facetSearchConfig, indexedType, resolvedIndex);
			boolean isExternalIndexOperation = true;

			if (pks == null)
			{
				isExternalIndexOperation = false;
			}

			doExecute(resolvedIndex, indexOperationId, isExternalIndexOperation);

			operationTimer.stop();
			logStrategySuccess(operationTimer);
		}
		catch (final IndexerException | RuntimeException e)
		{
			operationTimer.stop();
			logStrategyError(operationTimer);
			throw e;
		}
		finally
		{
			revertOperationInfo(revertibleInfo);
			removeLocalSessionContext();
		}
	}

	protected void doExecute(final Index resolvedIndex, final long indexOperationId, final boolean isExternalIndexOperation)
			throws IndexerException
	{
		try
		{
			final Collection<IndexedProperty> resolveIndexedProperties = resolveIndexedProperties();
			final Map<String, String> resolveIndexerHints = resolveIndexerHints();

			final IndexerContext context = getIndexerContextFactory().createContext(indexOperationId, getIndexOperation(),
					isExternalIndexOperation, getFacetSearchConfig(), getIndexedType(), resolveIndexedProperties);

			context.setIndex(resolvedIndex);
			context.getIndexerHints().putAll(resolveIndexerHints);

			getIndexerContextFactory().prepareContext();

			context.setPks(resolvePks());

			getIndexerContextFactory().initializeContext();

			// skip execution if there is nothing to index/delete
			if (CollectionUtils.isNotEmpty(context.getPks()))
			{
				doExecute(context);
			}

			getIndexerContextFactory().destroyContext();
		}
		catch (final IndexerException | RuntimeException e)
		{
			getIndexerContextFactory().destroyContext(e);
			throw e;
		}
	}

	protected abstract void doExecute(final IndexerContext indexerContext) throws IndexerException;

	protected void validateRequiredFields()
	{
		if (indexOperation == null)
		{
			throw new IllegalStateException("indexOperation field not set");
		}

		if (facetSearchConfig == null)
		{
			throw new IllegalStateException("facetSearchConfig field not set");
		}

		if (indexedType == null)
		{
			throw new IllegalStateException("indexedType field not set");
		}
	}

	protected Index resolveIndex() throws IndexerException
	{
		if (index != null)
		{
			return index;
		}

		try
		{
			final SolrIndexModel activeIndex = solrIndexService.getActiveIndex(facetSearchConfig.getName(),
					indexedType.getIdentifier());
			final SolrSearchProvider searchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig, indexedType);
			return searchProvider.resolveIndex(facetSearchConfig, indexedType, activeIndex.getQualifier());
		}
		catch (final SolrIndexNotFoundException e)
		{
			LOG.debug(e);
			return null;
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	protected FlexibleSearchQuerySpec createIndexerQuery() throws IndexerException
	{
		final IndexedTypeFlexibleSearchQuery query = indexedType.getFlexibleSearchQueries().get(indexOperation);

		if (query == null)
		{
			throw new UndefinedIndexerQuery(indexOperation + " query not defined in configuration.");
		}

		try
		{
			return flexibleSearchQuerySpecFactory.createIndexQuery(query, indexedType, facetSearchConfig);
		}
		catch (final SolrServiceException e)
		{
			throw new IndexerException(e);
		}
	}

	protected List<PK> executeIndexerQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final String query, final Map<String, Object> queryParameters) throws IndexerException
	{
		return indexerQueriesExecutor.getPks(facetSearchConfig, indexedType, query, queryParameters);
	}

	protected void createLocalSessionContext()
	{
		final Session session = sessionService.getCurrentSession();
		final JaloSession jaloSession = (JaloSession) sessionService.getRawSession(session);
		jaloSession.createLocalSessionContext();
	}

	protected void removeLocalSessionContext()
	{
		final Session session = sessionService.getCurrentSession();
		final JaloSession jaloSession = (JaloSession) sessionService.getRawSession(session);
		jaloSession.removeLocalSessionContext();
	}

	protected RevertibleUpdate registerOrUpdateNonSuspendableThread()
	{
		RevertibleUpdate revertibleInfo = null;

		final OperationInfo operationInfo = OperationInfo.builder().withTenant(resolveTenantId())
				.withStatusInfo("Creating a context for indexing as non suspendable...").asNotSuspendableOperation().build();

		try
		{
			RegistrableThread.registerThread(operationInfo);
		}
		catch (final IllegalStateException e)
		{
			LOG.debug("Thread has already been registered. Updating operation info...", e);

			revertibleInfo = OperationInfo.updateThread(operationInfo);
		}

		return revertibleInfo;
	}

	protected void revertOperationInfo(final RevertibleUpdate revertibleInfo)
	{
		if (revertibleInfo == null)
		{
			RegistrableThread.unregisterThread();
		}
		else
		{
			revertibleInfo.revert();
		}
	}

	protected void logStrategyStart()
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug(indexOperation + " index operation started on " + facetSearchConfig.getName() + "/"
					+ indexedType.getUniqueIndexedTypeCode() + ": " + new Date());
		}
	}

	protected void logStrategySuccess(final StopWatch operationTimer)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug(indexOperation + " index operation finished on " + facetSearchConfig.getName() + "/"
					+ indexedType.getUniqueIndexedTypeCode() + ": " + new Date() + ", total time: "
					+ operationTimer.getTotalTimeSeconds() + "s.");
		}
	}

	protected void logStrategyError(final StopWatch operationTimer)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug(indexOperation + " index operation failed on " + facetSearchConfig.getName() + "/"
					+ indexedType.getUniqueIndexedTypeCode() + ": " + new Date() + ", total time: "
					+ operationTimer.getTotalTimeSeconds() + "s.");
		}
	}

	protected List<PK> resolvePks() throws IndexerException
	{
		if (pks == null)
		{
			final FlexibleSearchQuerySpec querySpec = createIndexerQuery();

			final String userUID = querySpec.getUser();
			final UserModel user = userService.getUserForUID(userUID);

			userService.setCurrentUser(user);

			final String query = querySpec.getQuery();
			final Map<String, Object> queryParameters = querySpec.createParameters();
			return executeIndexerQuery(facetSearchConfig, indexedType, query, queryParameters);
		}

		return pks;
	}

	protected Collection<IndexedProperty> resolveIndexedProperties()
	{
		if (indexedProperties == null)
		{
			return indexedType.getIndexedProperties().values();
		}

		return indexedProperties;
	}

	protected Map<String, String> resolveIndexerHints()
	{
		if (indexerHints == null)
		{
			return new HashMap<>();
		}

		return indexerHints;
	}

	protected String resolveTenantId()
	{
		return tenantService.getCurrentTenantId();
	}

	protected UserModel resolveSessionUser()
	{
		return userService.getCurrentUser();
	}

	protected LanguageModel resolveSessionLanguage()
	{
		return commonI18NService.getCurrentLanguage();
	}

	protected CurrencyModel resolveSessionCurrency()
	{
		return commonI18NService.getCurrentCurrency();
	}

	public IndexOperation getIndexOperation()
	{
		return indexOperation;
	}

	@Override
	public void setIndexOperation(final IndexOperation indexOperation)
	{
		this.indexOperation = indexOperation;
	}

	public FacetSearchConfig getFacetSearchConfig()
	{
		return facetSearchConfig;
	}

	@Override
	public void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig)
	{
		this.facetSearchConfig = facetSearchConfig;
	}

	public IndexedType getIndexedType()
	{
		return indexedType;
	}

	@Override
	public void setIndexedType(final IndexedType indexedType)
	{
		this.indexedType = indexedType;
	}

	public Collection<IndexedProperty> getIndexedProperties()
	{
		return indexedProperties;
	}

	@Override
	public void setIndexedProperties(final Collection<IndexedProperty> indexedProperties)
	{
		this.indexedProperties = indexedProperties;
	}

	public List<PK> getPks()
	{
		return pks;
	}

	@Override
	public void setPks(final List<PK> pks)
	{
		this.pks = pks;
	}

	public Index getIndex()
	{
		return index;
	}

	@Override
	public void setIndex(final Index index)
	{
		this.index = index;
	}

	public Map<String, String> getIndexerHints()
	{
		return indexerHints;
	}

	@Override
	public void setIndexerHints(final Map<String, String> indexerHints)
	{
		this.indexerHints = indexerHints;
	}

	//dependencies

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

	public FlexibleSearchQuerySpecFactory getFlexibleSearchQuerySpecFactory()
	{
		return flexibleSearchQuerySpecFactory;
	}

	@Required
	public void setFlexibleSearchQuerySpecFactory(final FlexibleSearchQuerySpecFactory flexibleSearchQuerySpecFactory)
	{
		this.flexibleSearchQuerySpecFactory = flexibleSearchQuerySpecFactory;
	}

	public IndexerQueriesExecutor getIndexerQueriesExecutor()
	{
		return indexerQueriesExecutor;
	}

	@Required
	public void setIndexerQueriesExecutor(final IndexerQueriesExecutor indexerQueriesExecutor)
	{
		this.indexerQueriesExecutor = indexerQueriesExecutor;
	}

	public IndexOperationIdGenerator getIndexOperationIdGenerator()
	{
		return indexOperationIdGenerator;
	}

	@Required
	public void setIndexOperationIdGenerator(final IndexOperationIdGenerator indexOperationIdGenerator)
	{
		this.indexOperationIdGenerator = indexOperationIdGenerator;
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

	public TenantService getTenantService()
	{
		return tenantService;
	}

	@Required
	public void setTenantService(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public IndexerContextFactory getIndexerContextFactory()
	{
		return indexerContextFactory;
	}

	@Required
	public void setIndexerContextFactory(final IndexerContextFactory<?> indexerContextFactory)
	{
		this.indexerContextFactory = indexerContextFactory;
	}

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}
}
