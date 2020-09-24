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
package de.hybris.platform.solrfacetsearch.search.context.impl;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.common.ListenersFactory;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext.Status;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContextFactory;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link FacetSearchContextFactory}.
 */
public class DefaultFacetSearchContextFactory implements FacetSearchContextFactory<DefaultFacetSearchContext>
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSearchContextFactory.class);

	public static final String FACET_SEARCH_LISTENERS_KEY = "solrfacetsearch.facetSearchListeners";

	private final ThreadLocal<DefaultFacetSearchContext> facetSearchContext = new ThreadLocal<DefaultFacetSearchContext>();

	private SessionService sessionService;
	private CatalogVersionService catalogVersionService;
	private ListenersFactory listenersFactory;

	@Override
	public DefaultFacetSearchContext createContext(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		final Collection<CatalogVersionModel> catalogVersions = catalogVersionService.getSessionCatalogVersions();

		final DefaultFacetSearchContext context = new DefaultFacetSearchContext();
		context.setFacetSearchConfig(facetSearchConfig);
		context.setIndexedType(indexedType);
		context.setSearchQuery(searchQuery);
		context.setParentSessionCatalogVersions(catalogVersions);
		context.setStatus(Status.CREATED);

		if(CollectionUtils.isNotEmpty(indexedType.getSorts()))
		{
			context.setAvailableNamedSorts(new ArrayList<>(indexedType.getSorts()));
		}
		else
		{
			context.setAvailableNamedSorts(new ArrayList<>());
		}

		createLocalSessionContext();
		facetSearchContext.set(context);

		return context;
	}

	@Override
	public void initializeContext() throws FacetSearchException
	{
		final DefaultFacetSearchContext context = getContext();

		if (Status.CREATED != context.getStatus())
		{
			throw new IllegalStateException("Context not in status CREATED");
		}

		context.setStatus(Status.STARTING);
		executeBeforeFacetSearchListeners(context);
		context.setStatus(Status.EXECUTING);
	}

	@Override
	public DefaultFacetSearchContext getContext()
	{
		final DefaultFacetSearchContext context = facetSearchContext.get();

		if (context == null)
		{
			throw new IllegalStateException("There is no current context");
		}

		return context;
	}

	@Override
	public void destroyContext() throws FacetSearchException
	{
		final DefaultFacetSearchContext context = getContext();

		context.setStatus(Status.STOPPING);
		executeAfterFacetSearchListeners(context);
		context.setStatus(Status.COMPLETED);

		facetSearchContext.remove();
		removeLocalSessionContext();
	}

	@Override
	public void destroyContext(final Exception failureException)
	{
		try
		{
			final DefaultFacetSearchContext context = getContext();

			context.addFailureException(failureException);
			context.setStatus(Status.FAILED);
			executeAfterFacetSearchErrorListeners(context);
		}
		finally
		{
			facetSearchContext.remove();
			removeLocalSessionContext();
		}
	}

	protected void executeBeforeFacetSearchListeners(final DefaultFacetSearchContext context) throws FacetSearchException
	{
		final List<FacetSearchListener> listeners = getListeners(context);
		for (final FacetSearchListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running beforeFacetSearch listener for: " + listener.getClass().getCanonicalName());
			}

			listener.beforeSearch(context);
		}
	}

	protected void executeAfterFacetSearchListeners(final DefaultFacetSearchContext context) throws FacetSearchException
	{
		final List<FacetSearchListener> listeners = getListeners(context);
		for (final FacetSearchListener listener : Lists.reverse(listeners))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterFacetSearch listener for: " + listener.getClass().getCanonicalName());
			}

			listener.afterSearch(context);
		}
	}

	protected void executeAfterFacetSearchErrorListeners(final DefaultFacetSearchContext context)
	{
		final List<FacetSearchListener> listeners = getListeners(context);
		for (final FacetSearchListener listener : Lists.reverse(listeners))
		{
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Running afterFacetSearchError listener for: " + listener.getClass().getCanonicalName());
				}

				listener.afterSearchError(context);
			}
			catch (final Exception exception)
			{
				context.addFailureException(exception);
			}
		}
	}

	protected List<FacetSearchListener> getListeners(final DefaultFacetSearchContext context)
	{
		List<FacetSearchListener> listeners = (List<FacetSearchListener>) context.getAttributes().get(FACET_SEARCH_LISTENERS_KEY);

		if (listeners == null)
		{
			final FacetSearchConfig facetSearchConfig = context.getFacetSearchConfig();
			final IndexedType indexedType = context.getIndexedType();
			listeners = listenersFactory.getListeners(facetSearchConfig, indexedType, FacetSearchListener.class);

			context.getAttributes().put(FACET_SEARCH_LISTENERS_KEY, listeners);
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

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
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
}
