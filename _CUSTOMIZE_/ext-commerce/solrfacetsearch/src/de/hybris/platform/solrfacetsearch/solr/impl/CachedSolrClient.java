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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.Credentials;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The cached {@link SolrClient} instance. This is a wrapper around the real {@link SolrClient}.
 */
public class CachedSolrClient extends SolrClient
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(CachedSolrClient.class);

	private final SolrClient delegate;
	private final Credentials credentials;
	private final Consumer<SolrClient> closeMethod;
	private final AtomicInteger consumers;

	public CachedSolrClient(final SolrClient delegate)
	{
		this(delegate, null, null);
	}

	public CachedSolrClient(final SolrClient delegate, final Credentials credentials)
	{
		this(delegate, null, credentials);
	}

	public CachedSolrClient(final SolrClient delegate, final Consumer<SolrClient> closeMethod, final Credentials credentials)
	{
		this.delegate = delegate;
		this.closeMethod = closeMethod;
		this.credentials = credentials;
		this.consumers = new AtomicInteger();
	}

	@Override
	public NamedList<Object> request(final SolrRequest solrRequest, final String collection)
			throws SolrServerException, IOException
	{
		if (credentials != null)
		{
			final String previousUser = solrRequest.getBasicAuthUser();
			final String previousPassword = solrRequest.getBasicAuthPassword();

			try
			{
				solrRequest.setBasicAuthCredentials(credentials.getUserPrincipal().getName(), credentials.getPassword());
				return delegate.request(solrRequest, collection);
			}
			finally
			{
				// restore previous credentials
				solrRequest.setBasicAuthCredentials(previousUser, previousPassword);
			}
		}
		else
		{
			return delegate.request(solrRequest, collection);
		}
	}

	/**
	 * Notifies this client to remove one consumer. If this client is no longer required (consumersNumber < 0),
	 * {@link SolrClient#close()} is called on the delegate client.
	 */
	@Override
	public void close()
	{
		final int consumersNumber = consumers.decrementAndGet();
		if (consumersNumber < 0)
		{
			IOUtils.closeQuietly(delegate);

			try
			{
				if (closeMethod != null)
				{
					closeMethod.accept(delegate);
				}
			}
			catch (final Exception e)
			{
				LOG.error("An error ocurred while closing a Solr client", e);
			}
		}
	}

	/**
	 * Notifies this client that there is a new consumer.
	 */
	public void addConsumer()
	{
		consumers.incrementAndGet();
	}

	/**
	 * Returns the delegate Solr client.
	 *
	 * @return the delegate Solr client
	 */
	public SolrClient getDelegate()
	{
		return delegate;
	}

	/**
	 * Returns the method used to release resources associated with the delegate Solr client.
	 *
	 * @return the close method
	 */
	public Consumer<SolrClient> getCloseMethod()
	{
		return closeMethod;
	}

	/**
	 * Returns the credentials that will be used for authentication.
	 *
	 * @return the credentials
	 */
	protected Credentials getCredentials()
	{
		return credentials;
	}
}
