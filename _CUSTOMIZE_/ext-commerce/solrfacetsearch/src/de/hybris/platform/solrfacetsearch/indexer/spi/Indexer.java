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
package de.hybris.platform.solrfacetsearch.indexer.spi;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;

import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;


/**
 * Indexer delivers functionality for storing and removing hybris items in indexer.
 */
public interface Indexer
{
	/**
	 * Method send items to indexer.
	 *
	 * @param items
	 *           items for send to indexer in one transaction
	 * @param facetSearchConfig
	 *           configuration of facet search
	 * @param indexedType
	 *           indexed type definition
	 *
	 * @return list of items converted to solrDocuments
	 *
	 * @throws IndexerException
	 *            if an error occurs during indexing
	 * @throws InterruptedException
	 *            if any thread interrupted the current thread before before it completed indexing. The interrupted
	 *            status of the current thread is cleared when this exception is thrown.
	 */
	Collection<SolrInputDocument> indexItems(final Collection<ItemModel> items, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws IndexerException, InterruptedException;

	/**
	 * Method send items to indexer.
	 *
	 * @param items
	 *           items for send to indexer in one transaction
	 * @param facetSearchConfig
	 *           configuration of facet search
	 * @param indexedType
	 *           indexed type definition
	 * @param indexedProperties
	 *           the properties to index
	 *
	 * @return list of items converted to solrDocuments
	 *
	 * @throws IndexerException
	 *            if an error occurs during indexing
	 * @throws InterruptedException
	 *            if any thread interrupted the current thread before before it completed indexing. The interrupted
	 *            status of the current thread is cleared when this exception is thrown.
	 *
	 */
	Collection<SolrInputDocument> indexItems(final Collection<ItemModel> items, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType, Collection<IndexedProperty> indexedProperties)
			throws IndexerException, InterruptedException;

	/**
	 *
	 *
	 * @param pks
	 *           - list of pks to be removed
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @throws InterruptedException
	 *            if any thread interrupted the current thread before before it completed indexing. The interrupted status
	 *            of the current thread is cleared when this exception is thrown.
	 */
	void removeItemsByPk(Collection<PK> pks, FacetSearchConfig facetSearchConfig, IndexedType indexedType, Index index) throws IndexerException, InterruptedException;

	/**
	 * Remove from index all items by indexer identifiers.
	 *
	 * @deprecated since 6.3, use {@link SolrSearchProvider#deleteDocumentsByPK(Index, List)} instead
	 *
	 * @param items
	 *           items for send to indexer in one transaction
	 * @param facetSearchConfig
	 *           configuration of facet search
	 * @param indexedType
	 *           indexed type definition
	 *
	 * @return collection of solrDocumentIds
	 *
	 * @throws IndexerException
	 *            if an error occurs during indexing
	 * @throws InterruptedException
	 *            if any thread interrupted the current thread before before it completed indexing. The interrupted
	 *            status of the current thread is cleared when this exception is thrown.
	 */
	@Deprecated
	Collection<String> removeItems(final Collection<ItemModel> items, final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType) throws IndexerException, InterruptedException;
}
