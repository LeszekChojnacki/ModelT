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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.ExporterException;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;


/**
 * Bean sends solrDocuments to solrServer. Particular implementation are designed for different server modes.
 */
public interface Exporter
{

	/**
	 * Method exports solrDocuments to indexer for update.
	 *
	 * @param solrDocuments
	 * @param facetSearchConfig
	 * @param indexedType
	 * @throws ExporterException
	 */
	void exportToUpdateIndex(Collection<SolrInputDocument> solrDocuments, FacetSearchConfig facetSearchConfig,
			IndexedType indexedType) throws ExporterException;

	/**
	 * Methods delete solr documents from solr server.
	 *
	 * @param idsToDelete
	 *           solrD
	 * @param facetSearchConfig
	 * @param indexedType
	 * @throws ExporterException
	 */
	void exportToDeleteFromIndex(Collection<String> idsToDelete, FacetSearchConfig facetSearchConfig, IndexedType indexedType)
			throws ExporterException;
}
