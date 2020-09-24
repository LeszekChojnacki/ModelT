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
import de.hybris.platform.solrfacetsearch.indexer.ExtendedIndexerBatchListener;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext.Status;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link IndexerBatchContextFactory}. It uses a thread local variable for storing the
 * context.
 */
public class DefaultIndexerBatchContextFactory implements IndexerBatchContextFactory<DefaultIndexerBatchContext>
{
	private static final Logger LOG = Logger.getLogger(DefaultIndexerBatchContextFactory.class);

	public static final String LISTENERS_KEY = "solrfacetsearch.indexerBatchListeners";
	public static final String EXTENDED_LISTENERS_KEY = "solrfacetsearch.extendedIndexerListener";

	private final ThreadLocal<DefaultIndexerBatchContext> indexerBatchContext = new ThreadLocal<>();

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
	public DefaultIndexerBatchContext createContext(final long indexOperationId, final IndexOperation indexOperation,
			final boolean externalIndexOperation, final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<IndexedProperty> indexedProperties)
	{
		final DefaultIndexerBatchContext context = new DefaultIndexerBatchContext();
		context.setIndexOperationId(indexOperationId);
		context.setIndexOperation(indexOperation);
		context.setExternalIndexOperation(externalIndexOperation);
		context.setFacetSearchConfig(facetSearchConfig);
		context.setIndexedType(indexedType);
		context.setIndexedProperties(indexedProperties);

		createLocalSessionContext();
		indexerBatchContext.set(context);

		return context;
	}

	@Override
	public void prepareContext() throws IndexerException
	{
		final DefaultIndexerBatchContext context = indexerBatchContext.get();
		context.setStatus(Status.CREATED);
		executeAfterPrepareListeners(context);
	}

	@Override
	public void initializeContext() throws IndexerException
	{
		final DefaultIndexerBatchContext context = getContext();
		if (Status.CREATED != context.getStatus())
		{
			throw new IllegalStateException("Context not in status CREATED");
		}

		context.setStatus(Status.STARTING);
		executeBeforeBatchListeners(context);
		context.setStatus(Status.EXECUTING);
	}

	@Override
	public DefaultIndexerBatchContext getContext()
	{
		final DefaultIndexerBatchContext context = indexerBatchContext.get();

		if (context == null)
		{
			throw new IllegalStateException("There is no current context");
		}

		return context;
	}

	@Override
	public void destroyContext() throws IndexerException
	{
		final DefaultIndexerBatchContext context = getContext();

		context.setStatus(Status.STOPPING);
		executeAfterBatchListeners(context);
		context.setStatus(Status.COMPLETED);

		indexerBatchContext.remove();
		removeLocalSessionContext();
	}

	@Override
	public void destroyContext(final Exception failureException)
	{
		try
		{
			final DefaultIndexerBatchContext context = getContext();

			context.addFailureException(failureException);
			context.setStatus(Status.FAILED);
			executeAfterBatchErrorListeners(context);
		}
		finally
		{
			indexerBatchContext.remove();
			removeLocalSessionContext();
		}
	}

	protected void executeAfterPrepareListeners(final DefaultIndexerBatchContext context) throws IndexerException
	{
		final List<ExtendedIndexerBatchListener> listeners = getExtendedListeners(context);
		for (final ExtendedIndexerBatchListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterPrepareContext listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterPrepareContext(context);
		}
	}

	protected void executeBeforeBatchListeners(final DefaultIndexerBatchContext batchContext) throws IndexerException
	{
		final List<IndexerBatchListener> listeners = getListeners(batchContext);
		for (final IndexerBatchListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running beforeBatch listener for: " + listener.getClass().getCanonicalName());
			}

			listener.beforeBatch(batchContext);
		}
	}

	protected void executeAfterBatchListeners(final DefaultIndexerBatchContext batchContext) throws IndexerException
	{
		final List<IndexerBatchListener> listeners = getListeners(batchContext);
		for (final IndexerBatchListener listener : Lists.reverse(listeners))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterBatch listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterBatch(batchContext);
		}
	}

	protected void executeAfterBatchErrorListeners(final DefaultIndexerBatchContext batchContext)
	{
		final List<IndexerBatchListener> listeners = getListeners(batchContext);
		for (final IndexerBatchListener listener : Lists.reverse(listeners))
		{
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Running afterBatchError listener for: " + listener.getClass().getCanonicalName());
				}

				listener.afterBatchError(batchContext);
			}
			catch (final Exception exception)
			{
				batchContext.addFailureException(exception);
			}
		}
	}

	protected List<ExtendedIndexerBatchListener> getExtendedListeners(final DefaultIndexerBatchContext context)
	{
		List<ExtendedIndexerBatchListener> listeners = (List<ExtendedIndexerBatchListener>) context.getAttributes().get(EXTENDED_LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig contextFacetSearchConfig = context.getFacetSearchConfig();
			final IndexedType contextIndexedType = context.getIndexedType();
			listeners = listenersFactory.getListeners(contextFacetSearchConfig, contextIndexedType, ExtendedIndexerBatchListener.class);

			context.getAttributes().put(EXTENDED_LISTENERS_KEY, listeners);
		}

		return listeners;
	}

	protected List<IndexerBatchListener> getListeners(final DefaultIndexerBatchContext batchContext)
	{
		List<IndexerBatchListener> listeners = (List<IndexerBatchListener>) batchContext.getAttributes()
				.get(LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();
			final IndexedType indexedType = batchContext.getIndexedType();
			listeners = listenersFactory.getListeners(facetSearchConfig, indexedType, IndexerBatchListener.class);

			batchContext.getAttributes().put(LISTENERS_KEY, listeners);
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
