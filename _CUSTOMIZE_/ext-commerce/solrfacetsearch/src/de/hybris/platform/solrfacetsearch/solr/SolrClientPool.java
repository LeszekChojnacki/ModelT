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
package de.hybris.platform.solrfacetsearch.solr;

import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.solrfacetsearch.solr.impl.CachedSolrClient;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.solr.client.solrj.SolrClient;


/**
 * Pool for {@link SolrClient} instances. All communications with Solr should use clients returned from methods in this
 * class.
 */
public interface SolrClientPool
{
	/**
	 * Returns a {@link SolrClient} from the pool or creates it if it does not exist.
	 *
	 * @param index
	 *           - {@link Index}
	 * @param solrClientType
	 *           - {@link SolrClientType}
	 * @param createMethod
	 *           - method used to create a new instance of {@link SolrClient}
	 *
	 * @return {@link SolrClient}
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	CachedSolrClient getOrCreate(Index index, SolrClientType solrClientType, Function<SolrConfig, SolrClient> createMethod)
			throws SolrServiceException;

	/**
	 * Returns a {@link SolrClient} from the pool or creates it if it does not exist.
	 *
	 * @param index
	 *           - {@link Index}
	 * @param solrClientType
	 *           - {@link SolrClientType}
	 * @param createMethod
	 *           - method used to create a new instance of {@link SolrClient}
	 * @param closeMethod
	 *           - method used to release resources associated with the created instance of {@link SolrClient}
	 *
	 * @return {@link SolrClient}
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	CachedSolrClient getOrCreate(Index index, SolrClientType solrClientType, Function<SolrConfig, SolrClient> createMethod,
			Consumer<SolrClient> closeMethod) throws SolrServiceException;

	/**
	 * Invalidates the clients pool.
	 */
	void invalidateAll();
}
