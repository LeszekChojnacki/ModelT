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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;


/**
 * Implementations of this interface are responsible for getting the correct instance of {@link SolrSearchProvider}
 * according to specific configuration.
 */
public interface SolrSearchProviderFactory
{
	/**
	 * Returns a {@link SolrSearchProvider} instance.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @return {@link SolrSearchProvider} instance
	 */
	SolrSearchProvider getSearchProvider(FacetSearchConfig facetSearchConfig, IndexedType indexedType) throws SolrServiceException;
}
