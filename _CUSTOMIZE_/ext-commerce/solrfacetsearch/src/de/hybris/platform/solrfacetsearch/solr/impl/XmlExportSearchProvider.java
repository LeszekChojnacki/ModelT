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

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.IndexNameResolver;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Required;


/**
 * {@link SolrSearchProvider} implementation for XML_EXPORT mode.
 */
public class XmlExportSearchProvider implements SolrSearchProvider
{
	private IndexNameResolver indexNameResolver;

	@Override
	public Index resolveIndex(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final String qualifier)
	{
		validateParameterNotNullStandardMessage("facetSearchConfig", facetSearchConfig);
		validateParameterNotNullStandardMessage("indexType", indexedType);
		validateParameterNotNullStandardMessage("qualifier", indexedType);

		final String indexName = indexNameResolver.resolve(facetSearchConfig, indexedType, qualifier);

		final DefaultIndex index = new DefaultIndex();
		index.setName(indexName);
		index.setFacetSearchConfig(facetSearchConfig);
		index.setIndexedType(indexedType);
		index.setQualifier(qualifier);

		return index;
	}

	@Override
	public SolrClient getClient(final Index index) throws SolrServiceException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SolrClient getClientForIndexing(final Index index) throws SolrServiceException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void createIndex(final Index index) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void deleteIndex(final Index index) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void exportConfig(final Index index) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void commit(final Index index, final CommitType commitType) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void optimize(final Index index) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void deleteAllDocuments(final Index index) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void deleteOldDocuments(final Index index, final long indexOperationId) throws SolrServiceException
	{
		// NOOP
	}

	@Override
	public void deleteDocumentsByPk(final Index index, final Collection<PK> pks)
	{
		// NOOP
	}

	public IndexNameResolver getIndexNameResolver()
	{
		return indexNameResolver;
	}

	@Required
	public void setIndexNameResolver(final IndexNameResolver indexNameResolver)
	{
		this.indexNameResolver = indexNameResolver;
	}
}
