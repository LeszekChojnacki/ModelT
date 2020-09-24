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
package de.hybris.platform.solrfacetsearch.config.factories;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FlexibleSearchQuerySpec;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;


/**
 * Factory used to create an implementation of {@link FlexibleSearchQuerySpec}.
 */
public interface FlexibleSearchQuerySpecFactory
{
	/**
	 * Creates instance of {@link FlexibleSearchQuerySpec} based on the {@link IndexedTypeFlexibleSearchQuery}
	 */
	FlexibleSearchQuerySpec createIndexQuery(IndexedTypeFlexibleSearchQuery indexTypeFlexibleSearchQueryData,
			IndexedType indexedType, FacetSearchConfig facetSearchConfig) throws SolrServiceException;
}
