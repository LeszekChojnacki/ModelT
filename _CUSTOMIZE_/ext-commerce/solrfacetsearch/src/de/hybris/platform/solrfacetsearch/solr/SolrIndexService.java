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

import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.List;


/**
 * Service for managing indexes.
 */
public interface SolrIndexService
{
	/**
	 * Creates an index.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexModel createIndex(String facetSearchConfig, String indexedType, String qualifier) throws SolrServiceException;

	/**
	 * Returns all indexes.
	 *
	 * @return the indexes
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	List<SolrIndexModel> getAllIndexes() throws SolrServiceException;

	/**
	 * Returns all indexes for a specific facet search configuration and indexed type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 *
	 * @return the indexes
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	List<SolrIndexModel> getIndexesForConfigAndType(String facetSearchConfig, String indexedType) throws SolrServiceException;

	/**
	 * Finds an index by facet search configuration, indexed type and qualifier.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the index
	 *
	 * @throws SolrIndexNotFoundException
	 *            if an index cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexModel getIndex(String facetSearchConfig, String indexedType, String qualifier) throws SolrServiceException;

	/**
	 * Gets or creates an index (if it does not exist yet).
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the index
	 *
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexModel getOrCreateIndex(String facetSearchConfig, String indexedType, String qualifier) throws SolrServiceException;

	/**
	 * Deletes an index.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @throws SolrIndexNotFoundException
	 *            if an index cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	void deleteIndex(String facetSearchConfig, String indexedType, String qualifier) throws SolrServiceException;

	/**
	 * Activates an index. All other indexes within facet search configuration and indexed type combination will be
	 * deactivated.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 * @param qualifier
	 *           - the qualifier
	 * 
	 * @return the active index
	 *
	 * @throws SolrIndexNotFoundException
	 *            if an index cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexModel activateIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException;

	/**
	 * Returns the active index for a specific facet search configuration and indexed type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration name
	 * @param indexedType
	 *           - the indexed type name
	 *
	 * @return the active index
	 *
	 * @throws SolrIndexNotFoundException
	 *            if an index cannot be found
	 * @throws SolrServiceException
	 *            if an error occurs
	 */
	SolrIndexModel getActiveIndex(String facetSearchConfig, String indexedType) throws SolrServiceException;
}
