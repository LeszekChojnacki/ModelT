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
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.ExtendedIndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext.Status;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link IndexerContextFactory}. It uses a thread local variable for storing the context.
 */
public class DefaultIndexerContextFactory implements IndexerContextFactory<DefaultIndexerContext>
{
	private static final Logger LOG = Logger.getLogger(DefaultIndexerContextFactory.class);

	public static final String LISTENERS_KEY = "solrfacetsearch.indexerListeners";
	public static final String EXTENDED_LISTENERS_KEY = "solrfacetsearch.extendedIndexerListener";

	private final ThreadLocal<DefaultIndexerContext> indexerContext = new ThreadLocal<>();

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
	public DefaultIndexerContext createContext(final long indexOperationId, final IndexOperation indexOperation,
			final boolean externalIndexOperation, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties)
	{
		final DefaultIndexerContext context = new DefaultIndexerContext();
		context.setIndexOperationId(indexOperationId);
		context.setIndexOperation(indexOperation);
		context.setExternalIndexOperation(externalIndexOperation);
		context.setFacetSearchConfig(facetSearchConfig);
		context.setIndexedType(indexedType);
		context.setIndexedProperties(indexedProperties);

		createLocalSessionContext();
		indexerContext.set(context);

		return context;
	}

	@Override
	public void prepareContext() throws IndexerException
	{
		final DefaultIndexerContext context = getContext();
		context.setStatus(Status.CREATED);
		executeAfterPrepareListeners(context);
	}

	@Override
	public void initializeContext() throws IndexerException
	{
		final DefaultIndexerContext context = getContext();
		if (Status.CREATED != context.getStatus())
		{
			throw new IllegalStateException("Context not in status CREATED");
		}

		context.setStatus(Status.STARTING);
		executeBeforeIndexListeners(context);
		context.setStatus(Status.EXECUTING);
	}

	@Override
	public DefaultIndexerContext getContext()
	{
		final DefaultIndexerContext context = indexerContext.get();

		if (context == null)
		{
			throw new IllegalStateException("There is no current indexer context");
		}

		return context;
	}

	@Override
	public void destroyContext() throws IndexerException
	{
		final DefaultIndexerContext context = getContext();

		context.setStatus(Status.STOPPING);
		executeAfterIndexListeners(context);
		context.setStatus(Status.COMPLETED);

		indexerContext.remove();
		removeLocalSessionContext();
	}

	@Override
	public void destroyContext(final Exception failureException)
	{
		try
		{
			final DefaultIndexerContext context = getContext();

			context.addFailureException(failureException);
			context.setStatus(Status.FAILED);
			executeAfterIndexErrorListeners(context);
		}
		finally
		{
			indexerContext.remove();
			removeLocalSessionContext();
		}
	}

	protected void executeAfterPrepareListeners(final DefaultIndexerContext context) throws IndexerException
	{
		final List<ExtendedIndexerListener> listeners = getExtendedListeners(context);
		for (final ExtendedIndexerListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterPrepareContext listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterPrepareContext(context);
		}
	}

	protected void executeBeforeIndexListeners(final DefaultIndexerContext context) throws IndexerException
	{
		final List<IndexerListener> listeners = getListeners(context);
		for (final IndexerListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running beforeIndex listener for: " + listener.getClass().getCanonicalName());
			}

			listener.beforeIndex(context);
		}
	}

	protected void executeAfterIndexListeners(final DefaultIndexerContext context) throws IndexerException
	{
		final List<IndexerListener> listeners = getListeners(context);
		for (final IndexerListener listener : Lists.reverse(listeners))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterIndex listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterIndex(context);
		}
	}

	protected void executeAfterIndexErrorListeners(final DefaultIndexerContext context)
	{
		final List<IndexerListener> listeners = getListeners(context);
		for (final IndexerListener listener : Lists.reverse(listeners))
		{
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Running afterIndexError listener for: " + listener.getClass().getCanonicalName());
				}

				listener.afterIndexError(context);
			}
			catch (final Exception exception)
			{
				context.addFailureException(exception);
			}
		}
	}

	protected List<ExtendedIndexerListener> getExtendedListeners(final DefaultIndexerContext context)
	{
		List<ExtendedIndexerListener> listeners = (List<ExtendedIndexerListener>) context.getAttributes().get(EXTENDED_LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig contextFacetSearchConfig = context.getFacetSearchConfig();
			final IndexedType contextIndexedType = context.getIndexedType();
			listeners = listenersFactory.getListeners(contextFacetSearchConfig, contextIndexedType, ExtendedIndexerListener.class);

			context.getAttributes().put(EXTENDED_LISTENERS_KEY, listeners);
		}

		return listeners;
	}

	protected List<IndexerListener> getListeners(final DefaultIndexerContext context)
	{
		List<IndexerListener> listeners = (List<IndexerListener>) context.getAttributes().get(LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig facetSearchConfig = context.getFacetSearchConfig();
			final IndexedType indexedType = context.getIndexedType();
			listeners = listenersFactory.getListeners(facetSearchConfig, indexedType, IndexerListener.class);

			context.getAttributes().put(LISTENERS_KEY, listeners);
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
