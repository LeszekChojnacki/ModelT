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
package de.hybris.platform.solrfacetsearch.indexer.impl;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.common.ListenersFactory;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContext.Status;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link IndexerQueryContextFactory}. It uses a thread local variable for storing the
 * context.
 */
public class DefaultIndexerQueryContextFactory implements IndexerQueryContextFactory<DefaultIndexerQueryContext>
{
	private static final Logger LOG = Logger.getLogger(DefaultIndexerQueryContextFactory.class);

	public static final String INDEXER_QUERY_LISTENERS_KEY = "solrfacetsearch.indexerQueryListeners";

	private final ThreadLocal<DefaultIndexerQueryContext> indexerQueryContext = new ThreadLocal<DefaultIndexerQueryContext>();

	private SessionService sessionService;
	private ListenersFactory listenersFactory;

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public ListenersFactory getListenersFactory()
	{
		return listenersFactory;
	}

	@Required
	public void setListenersFactory(final ListenersFactory listenersFactory)
	{
		this.listenersFactory = listenersFactory;
	}

	@Override
	public DefaultIndexerQueryContext createContext(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final String query, final Map<String, Object> queryParameters) throws IndexerException
	{
		final DefaultIndexerQueryContext context = new DefaultIndexerQueryContext();
		context.setFacetSearchConfig(facetSearchConfig);
		context.setIndexedType(indexedType);
		context.setQuery(query);
		context.setQueryParameters(queryParameters);
		context.setStatus(Status.CREATED);

		createLocalSessionContext();
		indexerQueryContext.set(context);

		return context;
	}

	@Override
	public void initializeContext() throws IndexerException
	{
		final DefaultIndexerQueryContext context = getContext();

		if (Status.CREATED != context.getStatus())
		{
			throw new IllegalStateException("Context not in status CREATED");
		}

		context.setStatus(Status.STARTING);
		executeBeforeQueryListeners(context);
		context.setStatus(Status.EXECUTING);
	}

	@Override
	public DefaultIndexerQueryContext getContext()
	{
		final DefaultIndexerQueryContext context = indexerQueryContext.get();

		if (context == null)
		{
			throw new IllegalStateException("There is no current context");
		}

		return context;
	}

	@Override
	public void destroyContext() throws IndexerException
	{
		final DefaultIndexerQueryContext context = getContext();

		context.setStatus(Status.STOPPING);
		executeAfterQueryListeners(context);
		context.setStatus(Status.COMPLETED);

		indexerQueryContext.remove();
		removeLocalSessionContext();
	}

	@Override
	public void destroyContext(final Exception failureException)
	{
		try
		{
			final DefaultIndexerQueryContext context = getContext();

			context.addFailureException(failureException);
			context.setStatus(Status.FAILED);
			executeAfterQueryErrorListeners(context);
		}
		finally
		{
			indexerQueryContext.remove();
			removeLocalSessionContext();
		}
	}

	protected void executeBeforeQueryListeners(final DefaultIndexerQueryContext context) throws IndexerException
	{
		final List<IndexerQueryListener> listeners = getListeners(context);
		for (final IndexerQueryListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running beforeQuery listener for: " + listener.getClass().getCanonicalName());
			}

			listener.beforeQuery(context);
		}
	}

	protected void executeAfterQueryListeners(final DefaultIndexerQueryContext context) throws IndexerException
	{
		final List<IndexerQueryListener> listeners = getListeners(context);
		for (final IndexerQueryListener listener : Lists.reverse(listeners))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterQuery listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterQuery(context);
		}
	}

	protected void executeAfterQueryErrorListeners(final DefaultIndexerQueryContext context)
	{
		final List<IndexerQueryListener> listeners = getListeners(context);
		for (final IndexerQueryListener listener : Lists.reverse(listeners))
		{
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Running afterQueryError listener for: " + listener.getClass().getCanonicalName());
				}

				listener.afterQueryError(context);
			}
			catch (final Exception exception)
			{
				context.addFailureException(exception);
			}
		}
	}

	protected List<IndexerQueryListener> getListeners(final DefaultIndexerQueryContext context)
	{
		List<IndexerQueryListener> listeners = (List<IndexerQueryListener>) context.getAttributes()
				.get(INDEXER_QUERY_LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig facetSearchConfig = context.getFacetSearchConfig();
			final IndexedType indexedType = context.getIndexedType();
			listeners = listenersFactory.getListeners(facetSearchConfig, indexedType, IndexerQueryListener.class);

			context.getAttributes().put(INDEXER_QUERY_LISTENERS_KEY, listeners);
		}

		return listeners;
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
}
