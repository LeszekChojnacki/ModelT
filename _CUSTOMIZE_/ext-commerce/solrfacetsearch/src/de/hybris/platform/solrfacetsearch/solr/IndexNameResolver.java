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


/**
 * Resolves index names.
 */
public interface IndexNameResolver
{
	/**
	 * Resolves an index name based on a facet search configuration, indexed type and qualifier.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param qualifier
	 *           - the qualifier
	 *
	 * @return the index name
	 */
	String resolve(FacetSearchConfig facetSearchConfig, IndexedType indexedType, String qualifier);
}
